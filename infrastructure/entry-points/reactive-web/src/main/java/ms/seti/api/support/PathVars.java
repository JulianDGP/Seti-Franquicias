package ms.seti.api.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@Slf4j
public class PathVars {
    private PathVars() {}

    /** Lee un path variable como long (> 0) o emite IllegalArgumentException. */
    public static Mono<Long> validateLongId(ServerRequest request, String name) {
        return Mono.fromCallable(() -> Long.parseLong(request.pathVariable(name)))
                .flatMap(id -> (id > 0)
                        ? Mono.just(id)
                        : Mono.error(new IllegalArgumentException(name + " debe ser > 0")))
                .onErrorMap(NumberFormatException.class,
                        e -> new IllegalArgumentException(name + " inválido"));
    }
}
