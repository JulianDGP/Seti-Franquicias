package ms.seti.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Payload para modificar el stock de un producto. */
public record ModificarStockRequest(
        @NotNull @Min(0) Integer stock
) {}
