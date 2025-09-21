package ms.seti.r2dbc.sucursal;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SucursalDataRepository extends ReactiveCrudRepository<SucursalData, Long> {
    Mono<Boolean> existsByFranquiciaIdAndNombre(Long franquiciaId, String nombre);
}
