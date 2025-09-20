package ms.seti.model.franquicia.gateways;

import ms.seti.model.franquicia.Franquicia;
import reactor.core.publisher.Mono;

public interface FranquiciaRepository {
    Mono<Boolean> existsByNombre(String nombre);
    Mono<Franquicia> save(Franquicia franquicia);
    Mono<Franquicia> findById(Long id);
}
