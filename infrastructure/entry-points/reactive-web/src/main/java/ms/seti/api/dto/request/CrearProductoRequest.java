package ms.seti.api.dto.request;

/** Request para crear producto. stock es opcional, default 0. */
public record CrearProductoRequest(Long sucursalId, String nombre, Integer stock) {
}
