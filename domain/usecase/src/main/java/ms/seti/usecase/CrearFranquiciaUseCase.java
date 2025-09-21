package ms.seti.usecase;

import lombok.RequiredArgsConstructor;
import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CrearFranquiciaUseCase {
    private final FranquiciaRepository repo;

    /**
     * Crea una franquicia validando nombre no vacío y unicidad.
     */
    public Mono<Franquicia> execute(String nombre) {
        return Mono.justOrEmpty(nombre)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre es requerido")))
                .flatMap(n -> repo.existsByNombre(n)
                        .flatMap(exists -> Boolean.TRUE.equals(exists)
                                ? Mono.error(new IllegalStateException("La franquicia ya existe"))
                                : repo.save(Franquicia.builder().nombre(n).build())));
    }
}