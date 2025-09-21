package ms.seti.api.producto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.request.CrearProductoRequest;
import ms.seti.api.dto.request.ModificarStockRequest;
import ms.seti.api.dto.response.ProductoResponse;
import ms.seti.model.producto.Producto;
import ms.seti.usecase.CrearProductoUseCase;
import ms.seti.usecase.EliminarProductoUseCase;
import ms.seti.usecase.ModificarStockProductoUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import static ms.seti.api.support.BaseHandler.createdJson;
import static ms.seti.api.support.BaseHandler.readRequiredBody;
import static ms.seti.api.support.HttpErrors.selectOnErrorResponse;
import static ms.seti.api.support.PathVars.validateLongId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoHandler {
    private final CrearProductoUseCase crearProductoUseCase;
    private final EliminarProductoUseCase eliminarProductoUseCase;
    private final ModificarStockProductoUseCase modificarStockProductoUseCase;

    /** POST /api/v1/productos */
    public Mono<ServerResponse> crear(ServerRequest request) {
        return readRequiredBody(request, CrearProductoRequest.class)
                .flatMap(reqBody -> crearProductoUseCase.execute(reqBody.sucursalId(), reqBody.nombre(), reqBody.stock()))
                .flatMap(this::createdResponse)
                .doOnSubscribe(subscription -> log.info("POST /api/v1/productos"))
                .doOnError(e -> log.error("Error POST /productos", e))
                .onErrorResume(selectOnErrorResponse()); // 400/404/409 aquí; ó 500 lo maneja GlobalErrorHandler
    }

    /** DELETE /api/v1/productos/{id} */
    public Mono<ServerResponse> eliminar(ServerRequest request) {
        return validateLongId(request, "id")
                .flatMap(eliminarProductoUseCase::execute)
                .then(ServerResponse.noContent().build())
                .doOnSubscribe(s -> log.info("DELETE /api/v1/productos/{}", request.pathVariable("id")))
                .doOnError(e -> log.error("Error DELETE /productos/{}", request.pathVariable("id"), e))
                .onErrorResume(selectOnErrorResponse()); // 400/404 aquí; 500 vía GlobalErrorHandler
    }

    /** PUT /api/v1/productos/{id}/stock */
    public Mono<ServerResponse> modificarStock(ServerRequest request) {
        return validateLongId(request, "id")
                .zipWith(readRequiredBody(request, ModificarStockRequest.class))
                .flatMap(tuple -> {
                    Long productoId = tuple.getT1();
                    Integer nuevoStock = tuple.getT2().stock();
                    return modificarStockProductoUseCase.execute(productoId, nuevoStock);
                })
                .flatMap(this::okResponse)
                .doOnSubscribe(sub -> log.info("PUT /api/v1/productos/{}/stock", request.pathVariable("id")))
                .doOnError(e -> log.error("Error PUT /productos/{}/stock", request.pathVariable("id"), e))
                .onErrorResume(selectOnErrorResponse());
    }

    private Mono<ServerResponse> createdResponse(Producto producto) {
        URI location = URI.create("/api/v1/productos/" + producto.id());
        return createdJson(location, new ProductoResponse(
                producto.id(), producto.sucursalId(), producto.nombre(), producto.stock()
        ));
    }
    private Mono<ServerResponse> okResponse(Producto producto) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(toResponse(producto));
    }
    private ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(producto.id(), producto.sucursalId(), producto.nombre(), producto.stock());
    }
}
