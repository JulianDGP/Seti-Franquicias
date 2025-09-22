package ms.seti.usecase;


import ms.seti.model.producto.Producto;
import ms.seti.model.producto.gateways.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModificarStockProductoUseCaseTest {

    private ProductoRepository productoRepository;
    private ModificarStockProductoUseCase useCase;

    @BeforeEach
    void setUp() {
        productoRepository = mock(ProductoRepository.class);
        useCase = new ModificarStockProductoUseCase(productoRepository);
    }

    @Test
    @DisplayName("Actualiza stock cuando el producto existe y el stock es válido")
    void actualiza_ok() {
        Long productoId = 10L;
        Producto existente = Producto.builder()
                .id(productoId).sucursalId(1L).nombre("Lapicero").stock(5).build();
        Producto actualizado = existente.toBuilder().stock(7).build();

        when(productoRepository.findById(productoId)).thenReturn(Mono.just(existente));
        when(productoRepository.updateStock(productoId, 7)).thenReturn(Mono.just(actualizado));

        StepVerifier.create(useCase.execute(productoId, 7))
                .expectNextMatches(p -> p.id().equals(productoId) && p.stock().equals(7))
                .verifyComplete();

        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, times(1)).updateStock(productoId, 7);
    }

    @Test
    @DisplayName("Lanza 400 si el stock es negativo y no toca el repositorio")
    void stock_negativo_badRequest() {
        Long productoId = 11L;

        StepVerifier.create(useCase.execute(productoId, -1))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(IllegalArgumentException.class);
                    assertThat(e).hasMessage("El stock no puede ser negativo");
                })
                .verify();

        verify(productoRepository, never()).findById(any());
        verify(productoRepository, never()).updateStock(any(), any());
    }

    @Test
    @DisplayName("Interpreta stock null como 0 y actualiza a 0")
    void stock_null_se_trata_como_cero() {
        Long productoId = 12L;
        Producto existente = Producto.builder()
                .id(productoId).sucursalId(2L).nombre("Cuaderno").stock(3).build();
        Producto actualizado = existente.toBuilder().stock(0).build();

        when(productoRepository.findById(productoId)).thenReturn(Mono.just(existente));
        when(productoRepository.updateStock(productoId, 0)).thenReturn(Mono.just(actualizado));

        StepVerifier.create(useCase.execute(productoId, null))
                .expectNextMatches(p -> p.id().equals(productoId) && p.stock().equals(0))
                .verifyComplete();

        // También validamos que el 0 haya viajado al repo
        ArgumentCaptor<Integer> stockCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(productoRepository).updateStock(eq(productoId), stockCaptor.capture());
        assertThat(stockCaptor.getValue()).isZero();
    }

    @Test
    @DisplayName("Lanza 404 si el producto no existe y no intenta actualizar")
    void producto_no_encontrado_notFound() {
        Long productoId = 13L;

        when(productoRepository.findById(productoId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productoId, 5))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NoSuchElementException.class);
                    assertThat(e).hasMessage("Producto no encontrado");
                })
                .verify();

        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, never()).updateStock(any(), any());
    }
}