package ms.seti.api.producto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ms.seti.api.dto.request.CrearProductoRequest;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProductoRouter {

    @Bean
    @RouterOperation(path = "/api/v1/productos", beanClass = ProductoHandler.class, beanMethod = "crear",
            operation = @Operation(operationId = "crearProducto", summary = "Crea un producto dentro de una sucursal",
                    requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CrearProductoRequest.class))),
                    responses = {@ApiResponse(responseCode = "201", description = "Creado"),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada"),
                            @ApiResponse(responseCode = "409", description = "Producto ya existe en la sucursal")}))
    public RouterFunction<ServerResponse> productoRoutes(ProductoHandler handler) {
        return route(POST("/api/v1/productos"), handler::crear);
    }
}
