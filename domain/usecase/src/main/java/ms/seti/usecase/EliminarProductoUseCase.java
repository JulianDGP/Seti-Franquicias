package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.producto.gateways.ProductoRepository;
import reactor.core.publisher.Mono;

/**
 * Caso de uso: Eliminar un producto por id.
 * Regla: 404 si no existe.
 */
@RequiredArgsConstructor
public class EliminarProductoUseCase {
    private final ProductoRepository productoRepository;

    public Mono<Void> execute(Long productoId) {
        return Mono.justOrEmpty(productoId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El id de producto es requerido")))
                .flatMap(productoRepository::deleteById);
    }
}
