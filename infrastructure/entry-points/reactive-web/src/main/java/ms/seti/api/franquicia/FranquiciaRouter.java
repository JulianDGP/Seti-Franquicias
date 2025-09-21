package ms.seti.api.franquicia;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ms.seti.api.dto.request.ActualizarFranquiciaRequest;
import ms.seti.api.dto.request.CrearFranquiciaRequest;
import ms.seti.api.dto.response.ResponseDto;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FranquiciaRouter {
    @Bean
    @RouterOperations({@RouterOperation(path = "/api/v1/franquicias", beanClass = FranquiciaHandler.class, beanMethod = "crear",
            operation = @Operation(operationId = "crearFranquicia", summary = "Crea una franquicia",
                    requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CrearFranquiciaRequest.class))),
                    responses = {@ApiResponse(responseCode = "201", description = "Creada"),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                            @ApiResponse(responseCode = "409", description = "Ya existe")}))})
    public RouterFunction<ServerResponse> franquiciaRoutes(FranquiciaHandler h) {
        return route(POST("/api/v1/franquicias"), h::crear);
    }

    @Bean
    @RouterOperation(
            path = "/api/v1/franquicias/{id}",
            beanClass = FranquiciaHandler.class, beanMethod = "actualizarNombre",
            operation = @Operation(operationId = "actualizarNombreFranquicia", summary = "Actualiza el nombre de una franquicia",
                    parameters = {@Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "Id de la franquicia", schema = @Schema(type = "integer", format = "int64"), example = "1")},
                    requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ActualizarFranquiciaRequest.class))),
                    responses = {@ApiResponse(responseCode = "200", description = "Actualizada", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada"),
                            @ApiResponse(responseCode = "409", description = "Ya existe")}))
    public RouterFunction<ServerResponse> franquiciaUpdateRoutes(FranquiciaHandler handler) {
        return route(PUT("/api/v1/franquicias/{id}"), handler::actualizarNombre);
    }
}
