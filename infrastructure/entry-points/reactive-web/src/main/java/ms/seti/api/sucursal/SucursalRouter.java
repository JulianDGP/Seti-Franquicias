package ms.seti.api.sucursal;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ms.seti.api.dto.request.ActualizarSucursalRequest;
import ms.seti.api.dto.request.CrearSucursalRequest;
import ms.seti.api.dto.response.SucursalResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
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

    @Bean
    @RouterOperation(path = "/api/v1/sucursales/{id}", beanClass = SucursalHandler.class, beanMethod = "actualizarNombre",
            operation = @Operation(operationId = "actualizarNombreSucursal", summary = "Actualiza el nombre de una sucursal",
                    parameters = {@Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "Id de la sucursal",schema = @Schema(type = "integer", format = "int64"), example = "1")
                    },
                    requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ActualizarSucursalRequest.class))),
                    responses = {
                            @ApiResponse(responseCode = "200", description = "Actualizada", content = @Content(schema = @Schema(implementation = SucursalResponse.class))),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada"),
                            @ApiResponse(responseCode = "409", description = "Ya existe")}))
    public RouterFunction<ServerResponse> sucursalUpdateRoutes(SucursalHandler handler) {
        return route(PUT("/api/v1/sucursales/{id}"), handler::actualizarNombre);
    }
}
