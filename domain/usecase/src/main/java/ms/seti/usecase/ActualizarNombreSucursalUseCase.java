package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

/**
 * Actualiza el nombre de una sucursal.
 * Flujo:
 *  1) Normaliza/valida nombre (no vacío).
 *  2) Obtiene la sucursal (404 si no existe).
 *  3) Si no cambia el nombre, retorna el actual (idempotente).
 *  4) Verifica unicidad (uq por franquicia) y persiste el cambio.
 */
@RequiredArgsConstructor
public class ActualizarNombreSucursalUseCase {

    private final SucursalRepository sucursalRepository;

    public Mono<Sucursal> execute(Long sucursalId, String nuevoNombre) {
        return sucursalRepository.findById(sucursalId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Sucursal no encontrada")))
                .flatMap(actual -> normalizeNombre(nuevoNombre)
                        .flatMap(nombreNormalizado -> {
                            if (nombreNormalizado.equals(actual.nombre())) {
                                return Mono.just(actual); // idempotente
                            }
                            return ensureUnique(actual.franquiciaId(), nombreNormalizado)
                                    .then(sucursalRepository.updateNombre(sucursalId, nombreNormalizado));
                        })
                );
    }

    private Mono<String> normalizeNombre(String nombre) {
        return Mono.justOrEmpty(nombre)
                .map(String::trim)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre es requerido")));
    }

    private Mono<Void> ensureUnique(Long franquiciaId, String nombre) {
        return sucursalRepository.existsByFranquiciaIdAndNombre(franquiciaId, nombre)
                .defaultIfEmpty(false)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new IllegalStateException("El nombre de la sucursal ya existe para esta franquicia"))
                        : Mono.empty());
    }
}
