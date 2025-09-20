package ms.seti.model.franquicia;

import lombok.Builder;

@Builder(toBuilder = true)
public record Franquicia(
        Long id,
        String nombre
) {
}