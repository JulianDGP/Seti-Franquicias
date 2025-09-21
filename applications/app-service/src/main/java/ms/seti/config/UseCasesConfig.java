package ms.seti.config;

import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import ms.seti.usecase.CrearFranquiciaUseCase;
import ms.seti.usecase.CrearSucursalUseCase;
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
    public CrearFranquiciaUseCase crearFranquiciaUseCase(FranquiciaRepository franquiciaRepository) {
        return new CrearFranquiciaUseCase(franquiciaRepository);
    }

    @Bean
    public CrearSucursalUseCase crearSucursalUseCase(SucursalRepository sucRepo, FranquiciaRepository franRepo) {
        return new CrearSucursalUseCase(sucRepo, franRepo);
    }
}
