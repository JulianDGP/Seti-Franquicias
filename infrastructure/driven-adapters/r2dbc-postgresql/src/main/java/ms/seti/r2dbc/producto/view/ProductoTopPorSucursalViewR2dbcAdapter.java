package ms.seti.r2dbc.producto.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.model.producto.gateways.ProductoTopPorSucursalQueryRepository;
import ms.seti.model.producto.projections.ProductoTopPorSucursal;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoTopPorSucursalViewR2dbcAdapter implements ProductoTopPorSucursalQueryRepository {

    private final ProductoTopPorSucursalViewDataRepository reactiveRepository;

    @Override
    public Flux<ProductoTopPorSucursal> findByFranquiciaId(Long franquiciaId) {
        return reactiveRepository.findByFranquiciaId(franquiciaId)
                .doOnSubscribe(s -> log.info("Consultando productos top por sucursal para franquiciaId={}", franquiciaId))
                .map(ProductoTopPorSucursalViewR2dbcAdapter::toDomain)
                .doOnComplete(() -> log.info("Consulta finalizada para franquiciaId={}", franquiciaId));
    }

    private static ProductoTopPorSucursal toDomain(ProductoTopPorSucursalViewData data) {
        return ProductoTopPorSucursal.builder()
                .franquiciaId(data.franquiciaId)
                .franquiciaNombre(data.franquiciaNombre)
                .sucursalId(data.sucursalId)
                .sucursalNombre(data.sucursalNombre)
                .productoId(data.productoId)
                .productoNombre(data.productoNombre)
                .stock(data.stock)
                .build();
    }
}
