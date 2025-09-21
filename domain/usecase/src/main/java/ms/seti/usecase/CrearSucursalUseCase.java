package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class CrearSucursalUseCase {
    private final SucursalRepository sucRepo;
    private final FranquiciaRepository franRepo;

    public Mono<Sucursal> execute(Long franquiciaId, String nombre) {
        return normalizeNombre(nombre)
                .flatMap(n -> ensureFranquiciaExists(franquiciaId)
                        .then(ensureUnique(franquiciaId, n))
                        .then(persist(franquiciaId, n)));
    }

    private Mono<String> normalizeNombre(String nombre) {
        return Mono.justOrEmpty(nombre)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre es requerido")));
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
                        ? Mono.error(new IllegalStateException("La sucursal ya existe para esta franquicia"))
                        : Mono.empty());
    }

    private Mono<Sucursal> persist(Long franquiciaId, String nombre) {
        var s = Sucursal.builder().franquiciaId(franquiciaId).nombre(nombre).build();
        return sucRepo.create(s);
    }
}
