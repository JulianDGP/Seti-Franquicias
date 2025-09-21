package ms.seti.api.support;

import ms.seti.api.dto.response.ErrorResponseDto;
import org.springframework.core.codec.DecodingException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.function.Function;

public class HttpErrors {

    private HttpErrors() {}

    public static Mono<ServerResponse> mapToResponse(Throwable ex) {
        if (ex instanceof IllegalArgumentException) {
            return Responses.badRequest(new ErrorResponseDto(ex.getMessage()));
        }
        if (ex instanceof DecodingException || ex instanceof ServerWebInputException) {
            return Responses.badRequest(new ErrorResponseDto("Body inválido"));
        }
        if (ex instanceof NoSuchElementException) {
            return Responses.notFound();
        }
        if (ex instanceof IllegalStateException) {
            return Responses.conflict(new ErrorResponseDto(ex.getMessage()));
        }
        return Mono.error(ex); // que lo maneje el GlobalErrorHandler (500)
    }

    /** Function para pasar directo de onErrorResume(...) */
    public static Function<Throwable, Mono<ServerResponse>> selectOnErrorResponse() {
        return HttpErrors::mapToResponse;
    }
}
