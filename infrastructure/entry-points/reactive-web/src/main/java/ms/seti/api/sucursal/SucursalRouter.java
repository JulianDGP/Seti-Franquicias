package ms.seti.api.sucursal;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ms.seti.api.dto.request.CrearSucursalRequest;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class SucursalRouter {

    @Bean
    @RouterOperation(path = "/api/v1/sucursales", beanClass = SucursalHandler.class, beanMethod = "crear",
            operation = @Operation(operationId = "crearSucursal",summary = "Crea una sucursal",
                    requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CrearSucursalRequest.class))),
                    responses = {@ApiResponse(responseCode = "201", description = "Creada"),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada"),
                            @ApiResponse(responseCode = "409", description = "Ya existe")}))
    public RouterFunction<ServerResponse> sucursalRoutes(SucursalHandler handler) {
        return route(POST("/api/v1/sucursales"), handler::crear);
    }
}
