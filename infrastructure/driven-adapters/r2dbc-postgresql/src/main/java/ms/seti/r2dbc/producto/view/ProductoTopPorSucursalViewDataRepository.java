package ms.seti.r2dbc.producto.view;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Es una vista, se ejecutar @Query SELECT.
 * No se usan operaciones de escritura.
 */
@Repository
public interface ProductoTopPorSucursalViewDataRepository extends ReactiveCrudRepository<ProductoTopPorSucursalViewData, Long> {
    @Query("""
            SELECT franquicia_id, franquicia_nombre,
                   sucursal_id,   sucursal_nombre,
                   producto_id,   producto_nombre,
                   stock
            FROM dbo.v_producto_max_stock_por_sucursal
            WHERE franquicia_id = :franquiciaId
            """)
    Flux<ProductoTopPorSucursalViewData> findByFranquiciaId(Long franquiciaId);
}
