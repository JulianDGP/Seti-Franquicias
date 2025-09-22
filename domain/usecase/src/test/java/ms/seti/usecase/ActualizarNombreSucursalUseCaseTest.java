package ms.seti.usecase;

import ms.seti.model.sucursal.Sucursal;
import ms.seti.model.sucursal.gateways.SucursalRepository;
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
class ActualizarNombreSucursalUseCaseTest {

    @Mock
    SucursalRepository sucursalRepository;

    @InjectMocks
    ActualizarNombreSucursalUseCase useCase;

    @Captor
    ArgumentCaptor<Long> idCaptor;

    @Captor
    ArgumentCaptor<String> nombreCaptor;

    @BeforeEach
    void setup() {
        useCase = new ActualizarNombreSucursalUseCase(sucursalRepository);
    }

    @Test
    void nombre_invalido_lanzaBadRequest() {
        Long sucursalId = 10L;
        // No debería consultar la base si el nombre es inválido
        StepVerifier.create(useCase.execute(sucursalId, "   "))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("El nombre es requerido"))
                .verify();

        verifyNoInteractions(sucursalRepository);
    }

    @Test
    void sucursal_no_encontrada_lanzaNotFound() {
        Long sucursalId = 999L;
        when(sucursalRepository.findById(sucursalId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(sucursalId, "Nueva"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(NoSuchElementException.class)
                        .hasMessage("Sucursal no encontrada"))
                .verify();

        verify(sucursalRepository).findById(sucursalId);
        verifyNoMoreInteractions(sucursalRepository);
    }

    @Test
    void idempotente_mismo_nombre_devuelve_actual_sin_actualizar() {
        Long sucursalId = 7L;
        Sucursal actual = Sucursal.builder()
                .id(sucursalId).franquiciaId(1L).nombre("Alpha").build();

        when(sucursalRepository.findById(sucursalId)).thenReturn(Mono.just(actual));

        StepVerifier.create(useCase.execute(sucursalId, "  Alpha  "))
                .expectNext(actual)
                .verifyComplete();

        verify(sucursalRepository).findById(sucursalId);
        verify(sucursalRepository, never()).existsByFranquiciaIdAndNombre(anyLong(), anyString());
        verify(sucursalRepository, never()).updateNombre(anyLong(), anyString());
    }

    @Test
    void nombre_duplicado_en_franquicia_lanzaConflict() {
        Long sucursalId = 5L;
        Sucursal actual = Sucursal.builder()
                .id(sucursalId).franquiciaId(22L).nombre("Old").build();

        when(sucursalRepository.findById(sucursalId)).thenReturn(Mono.just(actual));
        when(sucursalRepository.existsByFranquiciaIdAndNombre(22L, "Nuevo"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(sucursalId, "Nuevo"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("El nombre de la sucursal ya existe para esta franquicia"))
                .verify();

        verify(sucursalRepository).findById(sucursalId);
        verify(sucursalRepository).existsByFranquiciaIdAndNombre(22L, "Nuevo");
        verify(sucursalRepository, never()).updateNombre(anyLong(), anyString());
    }

    @Test
    void camino_feliz_actualiza_nombre() {
        Long sucursalId = 3L;
        Sucursal actual = Sucursal.builder()
                .id(sucursalId).franquiciaId(9L).nombre("Viejo").build();
        String nuevoNombreRaw = "  Nuevo  ";
        String nuevoNombreNormalizado = "Nuevo";

        when(sucursalRepository.findById(sucursalId)).thenReturn(Mono.just(actual));
        // defaultIfEmpty(false) → probamos explícitamente el false
        when(sucursalRepository.existsByFranquiciaIdAndNombre(9L, nuevoNombreNormalizado))
                .thenReturn(Mono.just(false));

        Sucursal actualizado = actual.toBuilder().nombre(nuevoNombreNormalizado).build();
        when(sucursalRepository.updateNombre(eq(sucursalId), eq(nuevoNombreNormalizado)))
                .thenReturn(Mono.just(actualizado));

        StepVerifier.create(useCase.execute(sucursalId, nuevoNombreRaw))
                .expectNext(actualizado)
                .verifyComplete();

        verify(sucursalRepository).findById(sucursalId);
        verify(sucursalRepository).existsByFranquiciaIdAndNombre(9L, nuevoNombreNormalizado);
        verify(sucursalRepository).updateNombre(idCaptor.capture(), nombreCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(sucursalId);
        assertThat(nombreCaptor.getValue()).isEqualTo(nuevoNombreNormalizado);
    }
}
