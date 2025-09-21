package ms.seti.model.producto.gateways;

import ms.seti.model.producto.Producto;
import reactor.core.publisher.Mono;

public interface ProductoRepository {
    Mono<Boolean> existsBySucursalIdAndNombre(Long sucursalId, String nombre);
    Mono<Producto> create(Producto producto);
    Mono<Producto> findById(Long id);
    Mono<Void> deleteById(Long id);
}
