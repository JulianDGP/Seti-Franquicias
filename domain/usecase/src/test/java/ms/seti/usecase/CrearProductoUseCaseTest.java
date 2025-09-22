package ms.seti.usecase;

import ms.seti.model.producto.Producto;
import ms.seti.model.producto.gateways.ProductoRepository;
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

/**
 * Tests de unidad para CrearProductoUseCase.
 * Cubre: validación de nombre y stock, existencia de sucursal,
 * unicidad por (sucursalId, nombre) y persistencia.
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CrearProductoUseCaseTest {

    @Mock
    ProductoRepository productoRepository;

    @Mock
    SucursalRepository sucursalRepository;

    @InjectMocks
    CrearProductoUseCase useCase;

    @Captor
    ArgumentCaptor<Producto> productoCaptor;

    @BeforeEach
    void setup() {
        useCase = new CrearProductoUseCase(productoRepository, sucursalRepository);
    }

    @Test
    void nombre_invalido_lanzaBadRequest() {
        StepVerifier.create(useCase.execute(10L, "   ", 5))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("El nombre es requerido"))
                .verify();

        verifyNoInteractions(sucursalRepository, productoRepository);
    }

    @Test
    void sucursal_no_encontrada_lanzaNotFound() {
        Long sucursalId = 99L;
        when(sucursalRepository.findById(sucursalId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(sucursalId, "Prod", 1))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(NoSuchElementException.class)
                        .hasMessage("Sucursal no encontrada"))
                .verify();

        verify(sucursalRepository).findById(sucursalId);
        verifyNoMoreInteractions(sucursalRepository);
        verifyNoInteractions(productoRepository);
    }

    @Test
    void producto_duplicado_en_sucursal_lanzaConflict() {
        Long sucursalId = 7L;
        String nombre = "Prod A";

        when(sucursalRepository.findById(sucursalId))
                .thenReturn(Mono.just(Sucursal.builder().id(sucursalId).franquiciaId(1L).nombre("S1").build())); // existe
        when(productoRepository.existsBySucursalIdAndNombre(sucursalId, nombre)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(sucursalId, nombre, 3))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("El producto ya existe para esta sucursal"))
                .verify();

        verify(sucursalRepository).findById(sucursalId);
        verify(productoRepository).existsBySucursalIdAndNombre(sucursalId, nombre);
        verify(productoRepository, never()).create(any());
    }

    @Test
    void stock_negativo_lanzaBadRequest() {
        StepVerifier.create(useCase.execute(1L, "X", -10))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("El stock no puede ser negativo"))
                .verify();

        verifyNoInteractions(sucursalRepository, productoRepository);
    }

    @Test
    void stock_null_se_normaliza_a_cero_y_persiste() {
        Long sucursalId = 5L;
        String nombreRaw = "   Producto Z   ";
        String nombreNormalizado = "Producto Z";

        when(sucursalRepository.findById(sucursalId))
                .thenReturn(Mono.just(Sucursal.builder().id(sucursalId).franquiciaId(1L).nombre("S2").build())); // existe
        // Deja que defaultIfEmpty(false) tome el camino "no existe duplicado"
        when(productoRepository.existsBySucursalIdAndNombre(sucursalId, nombreNormalizado)).thenReturn(Mono.empty());

        Producto result = Producto.builder()
                .id(123L)
                .sucursalId(sucursalId)
                .nombre(nombreNormalizado)
                .stock(0)
                .build();

        when(productoRepository.create(any()))
                .thenAnswer(inv -> {
                    Producto p = inv.getArgument(0);
                    return Mono.just(result.toBuilder()
                            .sucursalId(p.sucursalId())
                            .nombre(p.nombre())
                            .stock(p.stock())
                            .build());
                });

        StepVerifier.create(useCase.execute(sucursalId, nombreRaw, null))
                .assertNext(prod -> {
                    assertThat(prod.id()).isEqualTo(123L);
                    assertThat(prod.sucursalId()).isEqualTo(sucursalId);
                    assertThat(prod.nombre()).isEqualTo(nombreNormalizado);
                    assertThat(prod.stock()).isZero();
                })
                .verifyComplete();

        verify(sucursalRepository).findById(sucursalId);
        verify(productoRepository).existsBySucursalIdAndNombre(sucursalId, nombreNormalizado);
        verify(productoRepository).create(productoCaptor.capture());

        Producto enviado = productoCaptor.getValue();
        assertThat(enviado.id()).isNull();
        assertThat(enviado.sucursalId()).isEqualTo(sucursalId);
        assertThat(enviado.nombre()).isEqualTo(nombreNormalizado);
        assertThat(enviado.stock()).isZero();
    }

    @Test
    void camino_feliz_crea_producto_con_stock_y_nombre_normalizados() {
        Long sucursalId = 2L;
        String nombreRaw = "  Aaa  ";
        String nombreNormalizado = "Aaa";
        int stock = 9;

        when(sucursalRepository.findById(sucursalId))
                .thenReturn(Mono.just(Sucursal.builder().id(sucursalId).franquiciaId(1L).nombre("S3").build())); // existe
        when(productoRepository.existsBySucursalIdAndNombre(sucursalId, nombreNormalizado)).thenReturn(Mono.empty());

        Producto persisted = Producto.builder()
                .id(42L)
                .sucursalId(sucursalId)
                .nombre(nombreNormalizado)
                .stock(stock)
                .build();

        when(productoRepository.create(any())).thenReturn(Mono.just(persisted));

        StepVerifier.create(useCase.execute(sucursalId, nombreRaw, stock))
                .expectNext(persisted)
                .verifyComplete();

        verify(productoRepository).create(productoCaptor.capture());
        Producto enviado = productoCaptor.getValue();
        assertThat(enviado.id()).isNull();
        assertThat(enviado.nombre()).isEqualTo(nombreNormalizado);
        assertThat(enviado.stock()).isEqualTo(stock);
    }
}