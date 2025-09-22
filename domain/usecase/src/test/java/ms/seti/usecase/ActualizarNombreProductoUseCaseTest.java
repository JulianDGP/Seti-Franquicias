package ms.seti.usecase;

import ms.seti.model.producto.Producto;
import ms.seti.model.producto.gateways.ProductoRepository;
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
class ActualizarNombreProductoUseCaseTest {

    @Mock
    ProductoRepository productoRepository;

    @InjectMocks
    ActualizarNombreProductoUseCase useCase;

    @BeforeEach
    void init() {
        useCase = new ActualizarNombreProductoUseCase(productoRepository);
    }

    @Test
    void nombre_invalido_lanzaBadRequest() {
        StepVerifier.create(useCase.execute(10L, "   "))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("El nombre es requerido"))
                .verify();

        verifyNoInteractions(productoRepository);
    }

    @Test
    void producto_no_encontrado_lanzaNotFound() {
        Long productoId = 99L;
        when(productoRepository.findById(productoId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productoId, "Nuevo"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(NoSuchElementException.class)
                        .hasMessage("Producto no encontrado"))
                .verify();

        verify(productoRepository).findById(productoId);
        verifyNoMoreInteractions(productoRepository);
    }

    @Test
    void idempotente_cuando_nombre_igual() {
        Long productoId = 7L;
        Producto actual = Producto.builder()
                .id(productoId).sucursalId(5L).nombre("Pan").stock(1).build();

        when(productoRepository.findById(productoId)).thenReturn(Mono.just(actual));

        StepVerifier.create(useCase.execute(productoId, "  Pan "))
                .expectNext(actual)
                .verifyComplete();

        verify(productoRepository).findById(productoId);
        verify(productoRepository, never()).existsBySucursalIdAndNombre(anyLong(), anyString());
        verify(productoRepository, never()).updateNombre(anyLong(), anyString());
    }

    @Test
    void nombre_duplicado_en_sucursal_lanzaConflict() {
        Long productoId = 11L;
        Producto actual = Producto.builder()
                .id(productoId).sucursalId(3L).nombre("Cola").stock(2).build();

        when(productoRepository.findById(productoId)).thenReturn(Mono.just(actual));
        when(productoRepository.existsBySucursalIdAndNombre(3L, "Pepsi")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(productoId, "Pepsi"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("El producto ya existe para esta sucursal"))
                .verify();

        verify(productoRepository).findById(productoId);
        verify(productoRepository).existsBySucursalIdAndNombre(3L, "Pepsi");
        verify(productoRepository, never()).updateNombre(anyLong(), anyString());
    }

    @Test
    void camino_feliz_actualiza_nombre() {
        Long productoId = 21L;
        Long sucursalId = 8L;
        Producto actual = Producto.builder()
                .id(productoId).sucursalId(sucursalId).nombre("Viejo").stock(3).build();
        Producto actualizado = actual.toBuilder().nombre("Nuevo").build();

        when(productoRepository.findById(productoId)).thenReturn(Mono.just(actual));
        // defaultIfEmpty(false) → si no hay valor, se interpreta como false (no existe duplicado)
        when(productoRepository.existsBySucursalIdAndNombre(sucursalId, "Nuevo")).thenReturn(Mono.empty());
        when(productoRepository.updateNombre(productoId, "Nuevo")).thenReturn(Mono.just(actualizado));

        StepVerifier.create(useCase.execute(productoId, "  Nuevo "))
                .expectNext(actualizado)
                .verifyComplete();

        verify(productoRepository).findById(productoId);
        verify(productoRepository).existsBySucursalIdAndNombre(sucursalId, "Nuevo");
        verify(productoRepository).updateNombre(productoId, "Nuevo");
    }
}
