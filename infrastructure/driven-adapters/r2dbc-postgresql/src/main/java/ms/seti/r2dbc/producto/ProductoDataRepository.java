package ms.seti.r2dbc.producto;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductoDataRepository  extends ReactiveCrudRepository<ProductoData, Long> {
    Mono<Boolean> existsBySucursalIdAndNombre(Long sucursalId, String nombre);
}
