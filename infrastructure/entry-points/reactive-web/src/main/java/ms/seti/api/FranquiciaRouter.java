package ms.seti.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FranquiciaRouter {
    @Bean
    @RouterOperations({@RouterOperation(path = "/api/v1/franquicias", beanClass = FranquiciaHandler.class, beanMethod = "crear",
            operation = @Operation(operationId = "crearFranquicia", summary = "Crea una franquicia",
                    requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ms.seti.api.dto.CrearFranquiciaRequest.class))),
                    responses = {@ApiResponse(responseCode = "201", description = "Creada"),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                            @ApiResponse(responseCode = "409", description = "Ya existe")}))})
    public RouterFunction<?> franquiciaRoutes(FranquiciaHandler h) {
        return route(POST("/api/v1/franquicias"), h::crear);
    }
}
