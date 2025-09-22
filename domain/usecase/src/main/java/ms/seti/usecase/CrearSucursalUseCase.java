package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

import static ms.seti.usecase.support.Validations.normalizeNombre;

@RequiredArgsConstructor
public class CrearSucursalUseCase {
    private final SucursalRepository sucRepo;
    private final FranquiciaRepository franRepo;

    public Mono<Sucursal> execute(Long franquiciaId, String nombre) {
        return normalizeNombre(nombre)
                .flatMap(n -> ensureFranquiciaExists(franquiciaId)
                        // Deferimos para no construir el publisher si la franquicia no existe o si esta repetido
                        .then(Mono.defer(() -> ensureUnique(franquiciaId, n)))
                        .then(Mono.defer(() -> persist(franquiciaId, n))));
    }

    private Mono<Void> ensureFranquiciaExists(Long franquiciaId) {
        return franRepo.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Franquicia no encontrada")))
                .then(); // convertimos a Mono<Void>
    }

    private Mono<Void> ensureUnique(Long franquiciaId, String nombre) {
        return sucRepo.existsByFranquiciaIdAndNombre(franquiciaId, nombre)
                .defaultIfEmpty(false)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new IllegalStateException("El nombre de la sucursal ya existe para esta franquicia"))
                        : Mono.empty());
    }

    private Mono<Sucursal> persist(Long franquiciaId, String nombre) {
        var s = Sucursal.builder().franquiciaId(franquiciaId).nombre(nombre).build();
        return sucRepo.create(s);
    }
}
