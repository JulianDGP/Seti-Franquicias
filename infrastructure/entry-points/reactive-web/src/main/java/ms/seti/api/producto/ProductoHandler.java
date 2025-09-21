package ms.seti.api.producto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.request.CrearProductoRequest;
import ms.seti.api.dto.response.ErrorResponseDto;
import ms.seti.api.dto.response.ProductoResponse;
import ms.seti.model.producto.Producto;
import ms.seti.usecase.CrearProductoUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import static ms.seti.api.support.BaseHandler.createdJson;
import static ms.seti.api.support.BaseHandler.readRequiredBody;
import static ms.seti.api.support.HttpErrors.selectOnErrorResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoHandler {
    private final CrearProductoUseCase crearProductoUseCase;

    /** POST /api/v1/productos */
    public Mono<ServerResponse> crear(ServerRequest request) {
        return readRequiredBody(request, CrearProductoRequest.class)
                .flatMap(reqBody -> crearProductoUseCase.execute(reqBody.sucursalId(), reqBody.nombre(), reqBody.stock()))
                .flatMap(this::createdResponse)
                .doOnSubscribe(subscription -> log.info("POST /api/v1/productos"))
                .doOnError(e -> log.error("Error POST /productos", e))
                .onErrorResume(selectOnErrorResponse()); // 400/404/409 aquí; ó 500 lo maneja GlobalErrorHandler
    }

    private Mono<ServerResponse> createdResponse(Producto producto) {
        URI location = URI.create("/api/v1/productos/" + producto.id());
        return createdJson(location, new ProductoResponse(
                producto.id(), producto.sucursalId(), producto.nombre(), producto.stock()
        ));
    }

}
