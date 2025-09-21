package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.producto.Producto;
import ms.seti.model.producto.gateways.ProductoRepository;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

/**
 * Caso de uso: Modificar el stock de un producto.
 * Flujo:
 *  1) Normaliza/valida stock (>= 0).
 *  2) Verifica que el producto exista.
 *  3) Persiste el nuevo stock (update-only).
 */
@RequiredArgsConstructor
public class ModificarStockProductoUseCase {
    private final ProductoRepository productoRepository;

    public Mono<Producto> execute(Long productoId, Integer nuevoStock) {
        return normalizeStock(nuevoStock)
                .flatMap(stockNormalizado -> ensureProductoExists(productoId)
                        .then(productoRepository.updateStock(productoId, stockNormalizado)));
    }

    private Mono<Integer> normalizeStock(Integer stock) {
        int seguro = (stock == null) ? 0 : stock;
        if (seguro < 0) {
            return Mono.error(new IllegalArgumentException("El stock no puede ser negativo"));
        }
        return Mono.just(seguro);
    }

    private Mono<Void> ensureProductoExists(Long productoId) {
        return productoRepository.findById(productoId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Producto no encontrado")))
                .then();
    }
}
