package ms.seti.model.sucursal.gateways;

import ms.seti.model.sucursal.Sucursal;
import reactor.core.publisher.Mono;

public interface SucursalRepository {
    Mono<Boolean> existsByFranquiciaIdAndNombre(Long franquiciaId, String nombre);
    Mono<Sucursal> save(Sucursal sucursal);
    Mono<Sucursal> findById(Long id);
}
