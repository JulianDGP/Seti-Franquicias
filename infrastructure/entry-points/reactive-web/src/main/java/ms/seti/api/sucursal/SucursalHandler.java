package ms.seti.api.sucursal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.request.CrearSucursalRequest;
import ms.seti.api.dto.response.SucursalResponse;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.usecase.CrearSucursalUseCase;
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
public class SucursalHandler {
    private final CrearSucursalUseCase crearUseCase;

    public Mono<ServerResponse> crear(ServerRequest req) {
        return readRequiredBody(req, CrearSucursalRequest.class)
                .flatMap(r -> crearUseCase.execute(r.franquiciaId(), r.nombre()))
                .flatMap(this::createdResponse)
                .doOnSubscribe(s -> log.info("POST /api/v1/sucursales"))
                .doOnError(e -> log.error("Error POST /sucursales", e))
                .onErrorResume(selectOnErrorResponse()); // 400/404/409 aquí; ó 500 lo maneja GlobalErrorHandler
    }

    private Mono<ServerResponse> createdResponse(Sucursal sucursal) {
        var location = URI.create("/api/v1/sucursales/" + sucursal.id());
        return createdJson(location, new SucursalResponse(
                sucursal.id(), sucursal.franquiciaId(), sucursal.nombre()
        ));
    }
}
