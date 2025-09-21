package ms.seti.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.request.CrearFranquiciaRequest;
import ms.seti.api.dto.response.ErrorResponseDto;
import ms.seti.api.dto.response.ResponseDto;
import ms.seti.model.franquicia.Franquicia;
import ms.seti.usecase.CrearFranquiciaUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class FranquiciaHandler {
    private final CrearFranquiciaUseCase crearUseCase;

    public Mono<ServerResponse> crear(ServerRequest req) {
        return readRequest(req)
                .flatMap(this::createFranquicia)           // llama al use case
                .flatMap(this::createdResponse)    // construye el ServerResponse
                .onErrorResume(IllegalArgumentException.class, this::badRequest)
                .onErrorResume(IllegalStateException.class, this::conflict)
                .doOnSubscribe(s -> log.info("POST /api/v1/franquicias"))
                .doOnError(e -> log.error("Error POST /franquicias: {}", e.getMessage()));
    }

    private Mono<CrearFranquiciaRequest> readRequest(ServerRequest req) {
        return req.bodyToMono(CrearFranquiciaRequest.class)
                .doOnNext(b -> log.debug("Payload: {}", b));
    }

    private Mono<Franquicia> createFranquicia(CrearFranquiciaRequest franquiciaRequest) {
        return crearUseCase.execute(franquiciaRequest.nombre());
    }

    private Mono<ServerResponse> createdResponse(Franquicia f) {
        URI location = URI.create("/api/v1/franquicias/" + f.id());
        return ServerResponse.created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ResponseDto(f.id(), f.nombre()));
    }

    private Mono<ServerResponse> badRequest(Throwable e) {
        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponseDto(e.getMessage()));
    }

    private Mono<ServerResponse> conflict(Throwable e) {
        return ServerResponse.status(409)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponseDto(e.getMessage()));
    }

}
