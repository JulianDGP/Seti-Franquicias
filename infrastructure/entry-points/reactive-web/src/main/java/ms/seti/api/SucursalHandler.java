package ms.seti.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.request.CrearSucursalRequest;
import ms.seti.api.dto.response.ErrorResponseDto;
import ms.seti.api.dto.response.SucursalResponse;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.usecase.CrearSucursalUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SucursalHandler {
    private final CrearSucursalUseCase crearUseCase;

    public Mono<ServerResponse> crear(ServerRequest req) {
        return req.bodyToMono(CrearSucursalRequest.class)
                .flatMap(r -> crearUseCase.execute(r.franquiciaId(), r.nombre()))
                .flatMap(this::createdResponse)
                .onErrorResume(IllegalArgumentException.class, e -> badRequest(e.getMessage()))
                .onErrorResume(NoSuchElementException.class, e -> ServerResponse.notFound().build())
                .onErrorResume(IllegalStateException.class, e -> conflict(e.getMessage()))
                .doOnSubscribe(s -> log.info("POST /api/v1/sucursales"))
                .doOnError(e -> log.error("Error POST /sucursales: {}", e.getMessage()));
    }

    private Mono<ServerResponse> createdResponse(Sucursal s) {
        var location = URI.create("/api/v1/sucursales/" + s.id());
        return ServerResponse.created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SucursalResponse(s.id(), s.franquiciaId(), s.nombre()));
    }

    private Mono<ServerResponse> badRequest(String msg) {
        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponseDto(msg));
    }

    private Mono<ServerResponse> conflict(String msg) {
        return ServerResponse.status(409)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponseDto(msg));
    }
}
