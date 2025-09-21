package ms.seti.model.producto.projections;

import lombok.Builder;

/** Resultado: producto con más stock por sucursal de una franquicia. */
@Builder(toBuilder = true)
public record ProductoTopPorSucursal(
        Long franquiciaId,
        String franquiciaNombre,
        Long sucursalId,
        String sucursalNombre,
        Long productoId,
        String productoNombre,
        Integer stock
) {}