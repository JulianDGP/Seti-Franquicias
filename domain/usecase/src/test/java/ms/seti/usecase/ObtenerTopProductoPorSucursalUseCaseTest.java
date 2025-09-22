package ms.seti.usecase;

import org.junit.jupiter.api.Test;

import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import ms.seti.model.producto.gateways.ProductoTopPorSucursalQueryRepository;
import ms.seti.model.producto.projections.ProductoTopPorSucursal;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObtenerTopProductoPorSucursalUseCaseTest {

    private final FranquiciaRepository franquiciaRepo = Mockito.mock(FranquiciaRepository.class);
    private final ProductoTopPorSucursalQueryRepository queryRepo = Mockito.mock(ProductoTopPorSucursalQueryRepository.class);
    private final ObtenerTopProductoPorSucursalUseCase useCase =
            new ObtenerTopProductoPorSucursalUseCase(franquiciaRepo, queryRepo);

    @Test
    void franquiciaExiste_emiteTopPorSucursal() {
        // given
        long franquiciaId = 1L;
        var franquicia = Franquicia.builder().id(franquiciaId).nombre("F1").build();
        when(franquiciaRepo.findById(franquiciaId)).thenReturn(Mono.just(franquicia));

        // dos filas mock de la proyección
        ProductoTopPorSucursal row1 = mock(ProductoTopPorSucursal.class);
        ProductoTopPorSucursal row2 = mock(ProductoTopPorSucursal.class);
        when(queryRepo.findByFranquiciaId(franquiciaId)).thenReturn(Flux.just(row1, row2));

        // when + then
        StepVerifier.create(useCase.execute(franquiciaId))
                .expectNext(row1, row2)
                .verifyComplete();

        verify(franquiciaRepo).findById(franquiciaId);
        verify(queryRepo).findByFranquiciaId(franquiciaId);
        verifyNoMoreInteractions(franquiciaRepo, queryRepo);
    }

    @Test
    void franquiciaNoExiste_lanza404_yNoConsultaVista() {
        // given
        long franquiciaId = 999L;
        when(franquiciaRepo.findById(franquiciaId)).thenReturn(Mono.empty());

        // when + then
        StepVerifier.create(useCase.execute(franquiciaId))
                .expectErrorMatches(e -> e instanceof NoSuchElementException
                        && e.getMessage().contains("Franquicia no encontrada"))
                .verify();

        verify(franquiciaRepo).findById(franquiciaId);
        verify(queryRepo, never()).findByFranquiciaId(anyLong());
        verifyNoMoreInteractions(franquiciaRepo, queryRepo);
    }
}