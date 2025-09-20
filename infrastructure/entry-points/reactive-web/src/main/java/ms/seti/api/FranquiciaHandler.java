package ms.seti.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.CrearFranquiciaRequest;
import ms.seti.usecase.crearfranquicia.CrearFranquiciaUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FranquiciaHandler {
    private final CrearFranquiciaUseCase crear;

    public Mono<ServerResponse> crear(ServerRequest req) {
        return req.bodyToMono(CrearFranquiciaRequest.class)
                .flatMap(body -> crear.execute(body.nombre()))
                .flatMap(f -> ServerResponse
                        .created(req.uri()) // podrías construir Location con /{id}
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new Resp(f.id(), f.nombre())))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(new Err(e.getMessage())))
                .onErrorResume(IllegalStateException.class,
                        e -> ServerResponse.status(409).contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(new Err(e.getMessage())))
                .doOnSubscribe(s -> log.info("POST /api/v1/franquicias"))
                .doOnError(e -> log.error("Error POST /franquicias: {}", e.getMessage()));
    }

    private record Resp(Long id, String nombre) {}
    private record Err(String error) {}
}
