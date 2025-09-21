package ms.seti.config;

import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import ms.seti.model.producto.gateways.ProductoRepository;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import ms.seti.usecase.CrearFranquiciaUseCase;
import ms.seti.usecase.CrearProductoUseCase;
import ms.seti.usecase.CrearSucursalUseCase;
import ms.seti.usecase.EliminarProductoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {
    @Bean
    public CrearFranquiciaUseCase crearFranquiciaUseCase(FranquiciaRepository franquiciaRepository) {
        return new CrearFranquiciaUseCase(franquiciaRepository);
    }

    @Bean
    public CrearSucursalUseCase crearSucursalUseCase(SucursalRepository sucRepo, FranquiciaRepository franRepo) {
        return new CrearSucursalUseCase(sucRepo, franRepo);
    }

    @Bean
    public CrearProductoUseCase crearProductoUseCase(ProductoRepository productoRepository,SucursalRepository sucursalRepository) {
        return new CrearProductoUseCase(productoRepository, sucursalRepository);
    }

    @Bean
    public EliminarProductoUseCase eliminarProductoUseCase(ProductoRepository productoRepository) {
        return new EliminarProductoUseCase(productoRepository);
    }
}
