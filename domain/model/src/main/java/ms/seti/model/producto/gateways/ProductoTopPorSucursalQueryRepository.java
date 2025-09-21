package ms.seti.model.producto.gateways;

import ms.seti.model.producto.projections.ProductoTopPorSucursal;
import reactor.core.publisher.Flux;

public interface ProductoTopPorSucursalQueryRepository {
    Flux<ProductoTopPorSucursal> findByFranquiciaId(Long franquiciaId);
}
