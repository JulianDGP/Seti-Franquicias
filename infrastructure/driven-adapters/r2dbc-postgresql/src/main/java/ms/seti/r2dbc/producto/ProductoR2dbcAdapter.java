package ms.seti.r2dbc.producto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.model.producto.Producto;
import ms.seti.model.producto.gateways.ProductoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoR2dbcAdapter implements ProductoRepository {
    private final ProductoDataRepository reactiveRepository;

    @Override
    public Mono<Producto> create(Producto producto) {
        if (producto.id() != null) {
            return Mono.error(new IllegalArgumentException("El endpoint de creación no permite actualizar (id debe ser null)"));
        }

        ProductoData productoData = toData(producto);
        return reactiveRepository.save(productoData)
                .doOnSubscribe(subscription -> log.info("Insertando producto '{}' en sucursal {}", productoData.nombre, productoData.sucursalId))
                .map(ProductoR2dbcAdapter::toDomain)
                .doOnSuccess(savedProducto -> log.info("Persistido producto id={}", savedProducto.id()))
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El producto ya existe para esta sucursal", e));
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return reactiveRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Producto no encontrado")))
                .doOnNext(entity -> log.info("Eliminando producto id={} (sucursalId={}, nombre='{}')",
                        entity.id, entity.sucursalId, entity.nombre))
                .flatMap(entity -> reactiveRepository.deleteById(id))
                .doOnSuccess(vacio -> log.info("Eliminado producto id={}", id));
    }

    @Override
    public Mono<Producto> updateStock(Long id, Integer stock) {
        return reactiveRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Producto no encontrado")))
                .flatMap(productEntity -> {
                    productEntity.stock = stock;
                    return reactiveRepository.save(productEntity);
                })
                .doOnSubscribe(s -> log.info("Actualizando stock de producto id={} a {}", id, stock))
                .map(ProductoR2dbcAdapter::toDomain)
                .doOnSuccess(updated -> log.info("Actualizado stock producto id={} -> {}", id, updated.stock()))
                .onErrorMap(DataIntegrityViolationException.class,
                        e -> new IllegalArgumentException("El stock no puede ser negativo", e));
    }

    @Override
    public Mono<Boolean> existsBySucursalIdAndNombre(Long sucursalId, String nombre) {
        return reactiveRepository.existsBySucursalIdAndNombre(sucursalId, nombre)
                .doOnSubscribe(subscription -> log.debug("existsBySucursalIdAndNombre(sucursalId={}, nombre={})", sucursalId, nombre))
                .doOnNext(exists -> log.debug("existsBySucursalIdAndNombre -> {}", exists));
    }

    @Override
    public Mono<Producto> findById(Long id) {
        return reactiveRepository.findById(id)
                .map(ProductoR2dbcAdapter::toDomain)
                .doOnSubscribe(subscription -> log.debug("findById({})", id))
                .doOnSuccess(found -> log.debug("findById -> {}", found));
    }


    // --- Mapeos ---
    private static Producto toDomain(ProductoData data) {
        return Producto.builder()
                .id(data.id)
                .sucursalId(data.sucursalId)
                .nombre(data.nombre)
                .stock(data.stock)
                .build();
    }

    private static ProductoData toData(Producto producto) {
        return new ProductoData(producto.id(), producto.sucursalId(), producto.nombre(), producto.stock());
    }
}
