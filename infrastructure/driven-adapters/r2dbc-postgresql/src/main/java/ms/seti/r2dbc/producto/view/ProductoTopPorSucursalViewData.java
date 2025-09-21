package ms.seti.r2dbc.producto.view;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Mapeo a la vista dbo.v_producto_max_stock_por_sucursal (read-only).
 * @Id: se usa producto_id solo para satisfacer el mapeo; no se usa para CRUD.
 */
@Table(schema = "dbo", value = "v_producto_max_stock_por_sucursal")
public class ProductoTopPorSucursalViewData {
    @Id
    @Column("producto_id")
    public Long productoId;

    @Column("franquicia_id")
    public Long franquiciaId;

    @Column("franquicia_nombre")
    public String franquiciaNombre;

    @Column("sucursal_id")
    public Long sucursalId;

    @Column("sucursal_nombre")
    public String sucursalNombre;

    @Column("producto_nombre")
    public String productoNombre;

    @Column("stock")
    public Integer stock;
}
