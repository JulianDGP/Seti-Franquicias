package ms.seti.model.producto;

import lombok.Builder;

@Builder(toBuilder = true)
public record Producto(
        Long id,
        Long sucursalId,
        String nombre,
        Integer stock
) {
}
