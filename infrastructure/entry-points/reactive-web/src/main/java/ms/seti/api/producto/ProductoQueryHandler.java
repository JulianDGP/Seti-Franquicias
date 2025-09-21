package ms.seti.api.producto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.response.ProductoTopPorSucursalResponse;
import ms.seti.model.producto.projections.ProductoTopPorSucursal;
import ms.seti.usecase.ObtenerTopProductoPorSucursalUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static ms.seti.api.support.HttpErrors.selectOnErrorResponse;
import static ms.seti.api.support.PathVars.validateLongId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoQueryHandler {
    private final ObtenerTopProductoPorSucursalUseCase obtenerTopProductoPorSucursalUseCase;

    /** GET /api/v1/franquicias/{id}/productos/top-por-sucursal */
    public Mono<ServerResponse> topPorSucursal(ServerRequest request) {
        return validateLongId(request, "id")
                .flatMapMany(obtenerTopProductoPorSucursalUseCase::execute)
                .map(this::toResponse)
                .collectList()
                .flatMap(this::okJson)
                .doOnSubscribe(s -> log.info("GET /api/v1/franquicias/{}/productos/top-por-sucursal", request.pathVariable("id")))
                .doOnError(e -> log.error("Error GET /franquicias/{}/productos/top-por-sucursal", request.pathVariable("id"), e))
                .onErrorResume(selectOnErrorResponse()); // 400/404 aquí; 500 via GlobalErrorHandler
    }

    private ProductoTopPorSucursalResponse toResponse(ProductoTopPorSucursal item) {
        return new ProductoTopPorSucursalResponse(
                item.franquiciaId(),
                item.franquiciaNombre(),
                item.sucursalId(),
                item.sucursalNombre(),
                item.productoId(),
                item.productoNombre(),
                item.stock()
        );
    }

    private Mono<ServerResponse> okJson(List<ProductoTopPorSucursalResponse> body) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }
}
