package ms.seti.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FranquiciaRouter {
    @Bean
    public RouterFunction<?> franquiciaRoutes(FranquiciaHandler h) {
        return route(POST("/api/v1/franquicias"), h::crear);
    }
}
