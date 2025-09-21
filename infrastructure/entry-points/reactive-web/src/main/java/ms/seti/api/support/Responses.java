package ms.seti.api.support;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/** Helpers para construir respuestas JSON. */
public class Responses {
    private Responses() {}

    public static Mono<ServerResponse> json(HttpStatus status, Object body) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    public static Mono<ServerResponse> badRequest(Object body) {
        return json(HttpStatus.BAD_REQUEST, body);
    }

    public static Mono<ServerResponse> conflict(Object body) {
        return json(HttpStatus.CONFLICT, body);
    }

    public static Mono<ServerResponse> notFound() {
        return ServerResponse.status(HttpStatus.NOT_FOUND).build();
    }
}
