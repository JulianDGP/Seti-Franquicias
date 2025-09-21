package ms.seti.api.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Utilidades reusables para handlers WebFlux:
 * - Leer body requerido (400 si falta).
 * - Responder 201 con JSON.
 */
@Slf4j
public class BaseHandler {
    private BaseHandler() {}

    public static <T> Mono<T> readRequiredBody(ServerRequest request, Class<T> type) {
        return request.bodyToMono(type)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body requerido")))
                .doOnNext(body -> log.debug("Payload({}): {}", type.getSimpleName(), body));
    }

    public static Mono<ServerResponse> createdJson(URI location, Object body) {
        return ServerResponse.created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }
}
