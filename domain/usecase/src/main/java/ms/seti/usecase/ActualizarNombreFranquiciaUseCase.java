package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

/**
 * Caso de uso: Actualizar el nombre de una franquicia.
 * Flujo:
 *  1) Normaliza/valida nombre (no vacío).
 *  2) Verifica que la franquicia exista (404).
 *  3) Si el nombre no cambia, retorna el actual (idempotente).
 *  4) Verifica unicidad (409) y persiste el cambio.
 */
@RequiredArgsConstructor
public class ActualizarNombreFranquiciaUseCase {

    private final FranquiciaRepository repo;

    public Mono<Franquicia> execute(Long id, String nuevoNombre) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Franquicia no encontrada")))
                .flatMap(actual ->
                        normalizeNombre(nuevoNombre)
                                .flatMap(nombreNormalizado -> {
                                    if (nombreNormalizado.equals(actual.nombre())) {
                                        // Idempotente: no hay cambios
                                        return Mono.just(actual);
                                    }
                                    return ensureUnique(nombreNormalizado)
                                            .then(repo.updateNombre(id, nombreNormalizado));
                                })
                );
    }

    private Mono<String> normalizeNombre(String nombre) {
        return Mono.justOrEmpty(nombre)
                .map(String::trim)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre es requerido")));
    }

    private Mono<Void> ensureUnique(String nombre) {
        return repo.existsByNombre(nombre)
                .defaultIfEmpty(false)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new IllegalStateException("La franquicia ya existe"))
                        : Mono.empty());
    }
}
