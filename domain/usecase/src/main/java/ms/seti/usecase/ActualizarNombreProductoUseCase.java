package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.producto.Producto;
import ms.seti.model.producto.gateways.ProductoRepository;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

/**
 * Actualiza el nombre de un producto.
 * Flujo:
 *  1) Normaliza/valida nombre.
 *  2) Obtiene el producto (404 si no existe).
 *  3) Si el nombre no cambia, retorna el actual (idempotente).
 *  4) Verifica unicidad por (sucursal_id, nombre) y persiste.
 */
@RequiredArgsConstructor
public class ActualizarNombreProductoUseCase {

    private final ProductoRepository productoRepository;

    public Mono<Producto> execute(Long productoId, String nuevoNombre) {
        return productoRepository.findById(productoId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Producto no encontrado")))
                .flatMap(actual -> normalizeNombre(nuevoNombre)
                        .flatMap(nombreNormalizado -> {
                            if (nombreNormalizado.equals(actual.nombre())) {
                                return Mono.just(actual); // idempotente
                            }
                            return ensureUnique(actual.sucursalId(), nombreNormalizado)
                                    .then(productoRepository.updateNombre(productoId, nombreNormalizado));
                        })
                );
    }

    private Mono<String> normalizeNombre(String nombre) {
        return Mono.justOrEmpty(nombre)
                .map(String::trim)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre es requerido")));
    }

    private Mono<Void> ensureUnique(Long sucursalId, String nombre) {
        return productoRepository.existsBySucursalIdAndNombre(sucursalId, nombre)
                .defaultIfEmpty(false)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new IllegalStateException("El producto ya existe para esta sucursal"))
                        : Mono.empty());
    }
}
