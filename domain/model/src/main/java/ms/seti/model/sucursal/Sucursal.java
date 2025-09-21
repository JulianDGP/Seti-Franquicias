package ms.seti.model.sucursal;
import lombok.Builder;

@Builder(toBuilder = true)
public record Sucursal(
        Long id,
        Long franquiciaId,
        String nombre
) {}
