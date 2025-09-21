package ms.seti.api.franquicia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.api.dto.request.CrearFranquiciaRequest;
import ms.seti.api.dto.response.ErrorResponseDto;
import ms.seti.api.dto.response.ResponseDto;
import ms.seti.model.franquicia.Franquicia;
import ms.seti.usecase.CrearFranquiciaUseCase;
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
public class FranquiciaHandler {
    private final CrearFranquiciaUseCase crearUseCase;

    public Mono<ServerResponse> crear(ServerRequest req) {
        return readRequiredBody(req, CrearFranquiciaRequest.class)
                .flatMap(this::createFranquicia)           // llama al use case
                .flatMap(this::createdResponse)    // construye el ServerResponse
                .doOnSubscribe(sub -> log.info("POST /api/v1/franquicias"))
                .doOnError(e -> log.error("Error POST /franquicias", e))
                .onErrorResume(selectOnErrorResponse()); // 400/404/409 aquí; ó 500 lo maneja GlobalErrorHandler
    }

    private Mono<Franquicia> createFranquicia(CrearFranquiciaRequest franquiciaRequest) {
        return crearUseCase.execute(franquiciaRequest.nombre());
    }

    private Mono<ServerResponse> createdResponse(Franquicia franquicia) {
        URI location = URI.create("/api/v1/franquicias/" + franquicia.id());
        return createdJson(location, new ResponseDto(franquicia.id(), franquicia.nombre()));
    }
}
