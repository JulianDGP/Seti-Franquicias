package ms.seti.usecase;

import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ActualizarNombreFranquiciaUseCaseTest {

    @Mock
    FranquiciaRepository repo;

    @InjectMocks
    ActualizarNombreFranquiciaUseCase useCase;

    @BeforeEach
    void init() {
        useCase = new ActualizarNombreFranquiciaUseCase(repo);
    }

    @Test
    void nombre_invalido_lanzaBadRequest() {
        StepVerifier.create(useCase.execute(1L, "   "))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("El nombre es requerido"))
                .verify();

        verifyNoInteractions(repo);
    }

    @Test
    void franquicia_no_encontrada_lanzaNotFound() {
        Long id = 99L;
        when(repo.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(id, "Nuevo"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(NoSuchElementException.class)
                        .hasMessage("Franquicia no encontrada"))
                .verify();

        verify(repo).findById(id);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void idempotente_cuando_nombre_igual() {
        Long id = 7L;
        Franquicia actual = Franquicia.builder().id(id).nombre("Acme").build();

        when(repo.findById(id)).thenReturn(Mono.just(actual));

        StepVerifier.create(useCase.execute(id, "  Acme "))
                .expectNext(actual)
                .verifyComplete();

        verify(repo).findById(id);
        verify(repo, never()).existsByNombre(anyString());
        verify(repo, never()).updateNombre(anyLong(), anyString());
    }

    @Test
    void nombre_duplicado_lanzaConflict() {
        Long id = 5L;
        Franquicia actual = Franquicia.builder().id(id).nombre("Viejo").build();

        when(repo.findById(id)).thenReturn(Mono.just(actual));
        when(repo.existsByNombre("Nuevo")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(id, "Nuevo"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("La franquicia ya existe"))
                .verify();

        verify(repo).findById(id);
        verify(repo).existsByNombre("Nuevo");
        verify(repo, never()).updateNombre(anyLong(), anyString());
    }

    @Test
    void camino_feliz_actualiza_nombre() {
        Long id = 11L;
        Franquicia actual = Franquicia.builder().id(id).nombre("Old").build();
        Franquicia actualizado = actual.toBuilder().nombre("New").build();

        when(repo.findById(id)).thenReturn(Mono.just(actual));
        // defaultIfEmpty(false) → si no hay valor, se interpreta como false (no existe duplicado)
        when(repo.existsByNombre("New")).thenReturn(Mono.empty());
        when(repo.updateNombre(id, "New")).thenReturn(Mono.just(actualizado));

        StepVerifier.create(useCase.execute(id, "  New "))
                .expectNext(actualizado)
                .verifyComplete();

        verify(repo).findById(id);
        verify(repo).existsByNombre("New");
        verify(repo).updateNombre(id, "New");
    }
}
