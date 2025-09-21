package ms.seti.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.response.ErrorResponseDto;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

/**
 * Manejador global de errores para WebFlux funcional.
 * - Traduce excepciones a códigos HTTP.
 * - Escribe ErrorResponseDto como JSON.
 * - Loguea SIEMPRE la causa (incluye stacktrace en 5xx).
 */
@Slf4j
@Component
@Order(-2) // prioridad más alta que el default de Spring
@RequiredArgsConstructor
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    @Override
    public @NonNull Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {
        // Si ya se escribió algo, deja que Spring termine
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = mapStatus(ex);

        // Log con traza completa + request info
        var request = exchange.getRequest();
        log.error("Error procesando {} {} -> {} {}",
                request.getMethod(), request.getURI(), status.value(), status.getReasonPhrase(), ex);

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var body = new ErrorResponseDto(
                ex.getMessage() != null ? ex.getMessage() : "Error inesperado"
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception jsonError) {
            // Fallback muy simple si falla la serialización
            byte[] bytes = "{\"error\":\"Error\"}".getBytes();
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }
    }

    private static HttpStatus mapStatus(Throwable ex) {
        if (ex instanceof IllegalArgumentException) return HttpStatus.BAD_REQUEST;   // 400
        if (ex instanceof IllegalStateException) return HttpStatus.CONFLICT;      // 409
        if (ex instanceof NoSuchElementException) return HttpStatus.NOT_FOUND;     // 404
        if (ex instanceof DecodingException
                || ex instanceof ServerWebInputException) return HttpStatus.BAD_REQUEST;  // 400
        return HttpStatus.INTERNAL_SERVER_ERROR;                                        // 500
    }
}
