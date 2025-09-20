package ms.seti.config;

import ms.seti.usecase.crearfranquicia.CrearFranquiciaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "ms.seti.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {
    @Bean
    public CrearFranquiciaUseCase crearFranquiciaUseCase(ms.seti.model.franquicia.gateways.FranquiciaRepository repo) {
        return new CrearFranquiciaUseCase(repo);
    }
}
