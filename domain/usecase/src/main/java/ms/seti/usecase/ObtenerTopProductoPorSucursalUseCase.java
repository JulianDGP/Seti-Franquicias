package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import ms.seti.model.producto.gateways.ProductoTopPorSucursalQueryRepository;
import ms.seti.model.producto.projections.ProductoTopPorSucursal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

/**
 * Caso de uso: obtener, para cada sucursal de una franquicia,
 * el producto con mayor stock (1 por sucursal),
 * recupera de una vista de base de datos que desempata por nombre
 */
@RequiredArgsConstructor
public class ObtenerTopProductoPorSucursalUseCase {
    private final FranquiciaRepository franquiciaRepository;
    private final ProductoTopPorSucursalQueryRepository queryRepository;

    public Flux<ProductoTopPorSucursal> execute(Long franquiciaId) {
        // Verifica que la franquicia exista (404 si no)
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Franquicia no encontrada")))
                .flatMapMany(f -> queryRepository.findByFranquiciaId(franquiciaId));
    }
}
