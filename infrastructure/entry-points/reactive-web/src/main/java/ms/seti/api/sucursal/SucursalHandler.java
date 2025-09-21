package ms.seti.api.sucursal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.request.ActualizarSucursalRequest;
import ms.seti.api.dto.request.CrearSucursalRequest;
import ms.seti.api.dto.response.SucursalResponse;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.usecase.ActualizarNombreSucursalUseCase;
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
import static ms.seti.api.support.PathVars.validateLongId;

@Slf4j
@Component
@RequiredArgsConstructor
public class SucursalHandler {
    private final CrearSucursalUseCase crearUseCase;
    private final ActualizarNombreSucursalUseCase actualizarNombreUseCase;


    public Mono<ServerResponse> crear(ServerRequest req) {
        return readRequiredBody(req, CrearSucursalRequest.class)
                .flatMap(r -> crearUseCase.execute(r.franquiciaId(), r.nombre()))
                .flatMap(this::createdResponse)
                .doOnSubscribe(s -> log.info("POST /api/v1/sucursales"))
                .doOnError(e -> log.error("Error POST /sucursales", e))
                .onErrorResume(selectOnErrorResponse()); // 400/404/409 aquí; ó 500 lo maneja GlobalErrorHandler
    }

    /** PUT /api/v1/sucursales/{id} */
    public Mono<ServerResponse> actualizarNombre(ServerRequest request) {
        return validateLongId(request, "id")
                .zipWith(readRequiredBody(request, ActualizarSucursalRequest.class))
                .flatMap(tuple -> {
                    Long sucursalId = tuple.getT1();
                    String nuevoNombre = tuple.getT2().nombre();
                    return actualizarNombreUseCase.execute(sucursalId, nuevoNombre);
                })
                .flatMap(this::okResponse)
                .doOnSubscribe(sub -> log.info("PUT /api/v1/sucursales/{}", request.pathVariable("id")))
                .doOnError(e -> log.error("Error PUT /sucursales/{}", request.pathVariable("id"), e))
                .onErrorResume(selectOnErrorResponse());
    }

    private Mono<ServerResponse> createdResponse(Sucursal sucursal) {
        var location = URI.create("/api/v1/sucursales/" + sucursal.id());
        return createdJson(location, new SucursalResponse(
                sucursal.id(), sucursal.franquiciaId(), sucursal.nombre()
        ));
    }

    private Mono<ServerResponse> okResponse(Sucursal sucursal) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(toResponse(sucursal));
    }

    private SucursalResponse toResponse(Sucursal sucursal) {
        return new SucursalResponse(sucursal.id(), sucursal.franquiciaId(), sucursal.nombre());
    }
}
