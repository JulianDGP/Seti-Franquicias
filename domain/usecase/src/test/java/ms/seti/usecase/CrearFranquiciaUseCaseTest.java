package ms.seti.usecase;

import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Cobertura:
 *  - Flujo feliz: normaliza, valida unicidad y persiste.
 *  - Nombre nulo/blanco: IllegalArgumentException.
 *  - Duplicado: IllegalStateException.
 *  - existsByNombre() vacío: se interpreta como false (pasa).
 */
@ExtendWith(MockitoExtension.class)
class CrearFranquiciaUseCaseTest {

    private final FranquiciaRepository repo = mock(FranquiciaRepository.class);
    private final CrearFranquiciaUseCase useCase = new CrearFranquiciaUseCase(repo);

    @Test
    void crea_ok_normalizaYPersiste() {
        // given
        String nombreEntrada = "  Acme  ";
        String nombreNormalizado = "Acme";

        when(repo.existsByNombre(nombreNormalizado)).thenReturn(Mono.just(false));

        var guardada = Franquicia.builder().id(10L).nombre(nombreNormalizado).build();
        // Capturamos el objeto que se envía a create() para verificar normalización e id null
        ArgumentCaptor<Franquicia> captor = ArgumentCaptor.forClass(Franquicia.class);
        when(repo.create(captor.capture())).thenReturn(Mono.just(guardada));

        // when + then
        StepVerifier.create(useCase.execute(nombreEntrada))
                .expectNext(guardada)
                .verifyComplete();

        verify(repo).existsByNombre(nombreNormalizado);
        verify(repo).create(any(Franquicia.class));
        verifyNoMoreInteractions(repo);

        Franquicia enviado = captor.getValue();
        assertThat(enviado.id()).isNull();                 // create-only
        assertThat(enviado.nombre()).isEqualTo("Acme");    // nombre normalizado
    }

    @Test
    void nombre_nulo_lanzaBadRequest() {
        // when + then
        StepVerifier.create(useCase.execute(null))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(IllegalArgumentException.class);
                    assertThat(e.getMessage()).contains("El nombre es requerido");
                })
                .verify();

        verifyNoInteractions(repo);
    }

    @Test
    void nombre_blanco_lanzaBadRequest() {
        // when + then
        StepVerifier.create(useCase.execute("   "))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(IllegalArgumentException.class);
                    assertThat(e.getMessage()).contains("El nombre es requerido");
                })
                .verify();

        verifyNoInteractions(repo);
    }

    @Test
    void nombre_duplicado_lanzaConflict() {
        // given
        when(repo.existsByNombre("Acme")).thenReturn(Mono.just(true));

        // when + then
        StepVerifier.create(useCase.execute("Acme"))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(IllegalStateException.class);
                    assertThat(e.getMessage()).contains("La franquicia ya existe");
                })
                .verify();

        verify(repo).existsByNombre("Acme");
        verify(repo, never()).create(any());
        verifyNoMoreInteractions(repo);
    }

    @Test
    void existsByNombre_empty_se_trataComoFalse_yCrea() {
        // given
        when(repo.existsByNombre("Nova")).thenReturn(Mono.empty()); // defaultIfEmpty(false)
        var guardada = Franquicia.builder().id(7L).nombre("Nova").build();
        when(repo.create(any())).thenReturn(Mono.just(guardada));

        // when + then
        StepVerifier.create(useCase.execute("Nova"))
                .expectNext(guardada)
                .verifyComplete();

        verify(repo).existsByNombre("Nova");
        verify(repo).create(any());
        verifyNoMoreInteractions(repo);
    }
}