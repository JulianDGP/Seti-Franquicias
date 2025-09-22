package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.producto.Producto;
import ms.seti.model.producto.gateways.ProductoRepository;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

import static ms.seti.usecase.support.Validations.normalizeNombre;
import static ms.seti.usecase.support.Validations.normalizeStock;

/**
 * Caso de uso: Crear producto en una sucursal.
 * Flujo:
 *  1) Normaliza y valida nombre y stock.
 *  2) Verifica que la sucursal exista.
 *  3) Verifica unicidad (uq_producto_por_sucursal).
 *  4) Inserta el producto (create-only).
 */
@RequiredArgsConstructor
public class CrearProductoUseCase {
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    public Mono<Producto> execute(Long sucursalId, String nombre, Integer stock) {
        return normalizeNombre(nombre)
                .zipWith(normalizeStock(stock))
                .flatMap(tuple -> {
                    String nombreNormalizado = tuple.getT1();
                    Integer stockNormalizado = tuple.getT2();

                    return ensureSucursalExists(sucursalId)
                            .then(Mono.defer(() -> ensureUnique(sucursalId, nombreNormalizado)))
                            .then(Mono.defer(() -> persist(sucursalId, nombreNormalizado, stockNormalizado)));
                });
    }

    private Mono<Void> ensureSucursalExists(Long sucursalId) {
        return sucursalRepository.findById(sucursalId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Sucursal no encontrada")))
                .then();
    }

    private Mono<Void> ensureUnique(Long sucursalId, String nombre) {
        return productoRepository.existsBySucursalIdAndNombre(sucursalId, nombre)
                .defaultIfEmpty(false)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new IllegalStateException("El producto ya existe para esta sucursal"))
                        : Mono.empty());
    }

    private Mono<Producto> persist(Long sucursalId, String nombre, Integer stock) {
        Producto nuevoProducto = Producto.builder()
                .sucursalId(sucursalId)
                .nombre(nombre)
                .stock(stock)
                .build();
        return productoRepository.create(nuevoProducto);
    }
}
