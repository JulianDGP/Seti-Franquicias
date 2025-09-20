package ms.seti.api.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi franquiciasApi() {
        return GroupedOpenApi.builder()
                .group("franquicias")
                .pathsToMatch("/api/**")
                .build();
    }
}
