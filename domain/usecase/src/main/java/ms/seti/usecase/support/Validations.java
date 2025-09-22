package ms.seti.usecase.support;

import reactor.core.publisher.Mono;

/**
 * Validaciones/normalizaciones reactivas reutilizables para casos de uso.
 */
public class Validations {
    private Validations() {}

    /** Quita espacios y valida no vacío. */
    public static Mono<String> normalizeNombre(String nombre) {
        return Mono.justOrEmpty(nombre)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre es requerido")));
    }

    /** Stock null -> 0; valida no-negativo. */
    public static Mono<Integer> normalizeStock(Integer stock) {
        int seguro = (stock == null) ? 0 : stock;
        if (seguro < 0) {
            return Mono.error(new IllegalArgumentException("El stock no puede ser negativo"));
        }
        return Mono.just(seguro);
    }
}
