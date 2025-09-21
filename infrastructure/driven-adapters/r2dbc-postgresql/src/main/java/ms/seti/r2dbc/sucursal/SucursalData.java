package ms.seti.r2dbc.sucursal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "dbo", value = "sucursal")
@AllArgsConstructor
@NoArgsConstructor
public class SucursalData {
    @Id
    Long id;
    @Column("franquicia_id")
    Long franquiciaId;
    String nombre;
}
