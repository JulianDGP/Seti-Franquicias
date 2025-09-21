package ms.seti.api.producto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ms.seti.api.dto.request.CrearProductoRequest;
import ms.seti.api.dto.request.ModificarStockRequest;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
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

    @Bean
    @RouterOperation(path = "/api/v1/productos/{id}", beanClass = ProductoHandler.class, beanMethod = "eliminar",
            operation = @Operation(operationId = "eliminarProducto", summary = "Elimina un producto por id",
                    parameters = {@Parameter(name = "id", in = ParameterIn.PATH, required = true,
                                    description = "ID del producto",
                                    schema = @Schema(type = "integer", format = "int64", minimum = "1"))
                    },
                    responses = {
                            @ApiResponse(responseCode = "204", description = "Eliminado"),
                            @ApiResponse(responseCode = "400", description = "Id inválido"),
                            @ApiResponse(responseCode = "404", description = "Producto no encontrado")}))
    public RouterFunction<ServerResponse> productoDeleteRoute(ProductoHandler handler) {
        return route(DELETE("/api/v1/productos/{id}"), handler::eliminar);
    }

    @Bean
    @RouterOperation(path = "/api/v1/productos/{id}/stock", beanClass = ProductoHandler.class, beanMethod = "modificarStock",
            operation = @Operation(operationId = "modificarStockProducto", summary = "Modifica el stock de un producto por id",
                    parameters = {@Parameter(name = "id", in = ParameterIn.PATH, required = true,
                                    description = "ID del producto",
                                    schema = @Schema(type = "integer", format = "int64", minimum = "1"))
                    },
                    requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ModificarStockRequest.class))),
                    responses = {
                            @ApiResponse(responseCode = "200", description = "Actualizado"),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                            @ApiResponse(responseCode = "404", description = "Producto no encontrado")}))
    public RouterFunction<ServerResponse> productoUpdateStockRoute(ProductoHandler handler) {
        return route(PUT("/api/v1/productos/{id}/stock"), handler::modificarStock);
    }
}
