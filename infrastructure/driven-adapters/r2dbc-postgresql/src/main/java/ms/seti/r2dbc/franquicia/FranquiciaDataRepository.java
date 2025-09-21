package ms.seti.r2dbc.franquicia;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface FranquiciaDataRepository  extends ReactiveCrudRepository<FranquiciaData, Long> {
    Mono<Boolean> existsByNombre(String nombre);
    Mono<FranquiciaData> findByNombre(String nombre);
}
