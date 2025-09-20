package ms.seti.r2dbc.franquicia;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table(schema = "dbo", value = "franquicia")
@NoArgsConstructor
@AllArgsConstructor
public class FranquiciaData {
    @Id
    Long id;
    String nombre;
}