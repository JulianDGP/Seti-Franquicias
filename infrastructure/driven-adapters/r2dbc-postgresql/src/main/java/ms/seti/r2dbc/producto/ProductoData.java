package ms.seti.r2dbc.producto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table(schema = "dbo", value = "producto")
@AllArgsConstructor
@NoArgsConstructor
public class ProductoData {
    @Id
    Long id;

    @Column("sucursal_id")
    Long sucursalId;

    String nombre;

    Integer stock;
}
