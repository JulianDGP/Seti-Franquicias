package ms.seti.config;

import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import ms.seti.model.producto.gateways.ProductoRepository;
import ms.seti.model.producto.gateways.ProductoTopPorSucursalQueryRepository;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import ms.seti.usecase.*;
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
    public CrearProductoUseCase crearProductoUseCase(ProductoRepository productoRepository, SucursalRepository sucursalRepository) {
        return new CrearProductoUseCase(productoRepository, sucursalRepository);
    }

    @Bean
    public EliminarProductoUseCase eliminarProductoUseCase(ProductoRepository productoRepository) {
        return new EliminarProductoUseCase(productoRepository);
    }

    @Bean
    public ModificarStockProductoUseCase modificarStockProductoUseCase(ProductoRepository productoRepository) {
        return new ModificarStockProductoUseCase(productoRepository);
    }

    @Bean
    public ObtenerTopProductoPorSucursalUseCase obtenerTopProductoPorSucursalUseCase(
            FranquiciaRepository franquiciaRepository,
            ProductoTopPorSucursalQueryRepository queryRepository
    ) {
        return new ObtenerTopProductoPorSucursalUseCase(franquiciaRepository, queryRepository);
    }
    @Bean
    public ActualizarNombreFranquiciaUseCase actualizarNombreFranquiciaUseCase(
            ms.seti.model.franquicia.gateways.FranquiciaRepository franquiciaRepository) {
        return new ActualizarNombreFranquiciaUseCase(franquiciaRepository);
    }
}
