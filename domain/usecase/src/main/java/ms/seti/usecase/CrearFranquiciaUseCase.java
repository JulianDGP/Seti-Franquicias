package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CrearFranquiciaUseCase {
    private final FranquiciaRepository repo;

    /**
     * Crea una franquicia validando nombre no vacío y unicidad.
     */
    public Mono<Franquicia> execute(String nombre) {
        return normalizeNombre(nombre)
                .flatMap(n -> ensureUnique(n).then(persist(n)));
    }

    /** Quita espacios y valida no vacío. */
    private Mono<String> normalizeNombre(String nombre) {
        return Mono.justOrEmpty(nombre)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre es requerido")));
    }

    /** Verifica que no exista otra franquicia con el mismo nombre. */
    private Mono<Void> ensureUnique(String nombre) {
        return repo.existsByNombre(nombre)
                .defaultIfEmpty(false)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new IllegalStateException("La franquicia ya existe"))
                        : Mono.empty());
    }

    /** Inserta la franquicia (sin updates). */
    private Mono<Franquicia> persist(String nombre) {
        var franquicia = Franquicia.builder().nombre(nombre).build(); // id null => create
        return repo.create(franquicia);
    }
}