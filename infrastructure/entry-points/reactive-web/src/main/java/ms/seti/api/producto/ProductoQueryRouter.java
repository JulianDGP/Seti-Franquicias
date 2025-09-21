package ms.seti.api.producto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import ms.seti.api.dto.response.ProductoTopPorSucursalResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProductoQueryRouter {

    @Bean
    @RouterOperation(path = "/api/v1/franquicias/{id}/productos/top-por-sucursal", beanClass = ProductoQueryHandler.class, beanMethod = "topPorSucursal",
            operation = @Operation(operationId = "topProductoPorSucursal", summary = "Lista, para cada sucursal de la franquicia, el producto con mayor stock",
                    parameters = {@Parameter(name = "id", in = ParameterIn.PATH, description = "Id de la franquicia", required = true, schema = @Schema(type = "integer", format = "int64"), example = "1")},
                    responses = {@ApiResponse(responseCode = "200", description = "OK", content = @io.swagger.v3.oas.annotations.media.Content(array = @ArraySchema(schema = @Schema(implementation = ProductoTopPorSucursalResponse.class)))),
                            @ApiResponse(responseCode = "400", description = "Id inválido"),
                            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada")}))
    public RouterFunction<ServerResponse> productoQueryRoutes(ProductoQueryHandler handler) {
        return route(GET("/api/v1/franquicias/{id}/productos/top-por-sucursal"), handler::topPorSucursal);
    }
}
