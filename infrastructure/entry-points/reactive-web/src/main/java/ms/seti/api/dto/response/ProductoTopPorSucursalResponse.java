package ms.seti.api.dto.response;

/**
 * Respuesta item: top producto por sucursal para una franquicia dada.
 */
public record ProductoTopPorSucursalResponse(
        Long franquiciaId,
        String franquiciaNombre,
        Long sucursalId,
        String sucursalNombre,
        Long productoId,
        String productoNombre,
        Integer stock
) {
}
