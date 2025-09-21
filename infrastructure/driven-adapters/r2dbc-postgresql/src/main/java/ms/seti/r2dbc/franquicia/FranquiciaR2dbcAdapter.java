package ms.seti.r2dbc.franquicia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FranquiciaR2dbcAdapter implements FranquiciaRepository {

    private final FranquiciaDataRepository reactiveRepo;

    @Override
    public Mono<Franquicia> create(Franquicia franchise) {
        var entity = toData(franchise);
        return reactiveRepo.save(entity)
                .doOnSubscribe(sus -> log.info("Guardando franquicia: {}", franchise.nombre()))
                .map(FranquiciaR2dbcAdapter::toDomain)
                .doOnSuccess(saved -> log.info("Guardada franquicia con id={}", saved.id()))
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("La franquicia ya existe", e));
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return reactiveRepo.existsByNombre(nombre)
                .doOnSubscribe(s -> log.debug("existsByNombre(nombre={})", nombre))
                .doOnNext(exists -> log.debug("existsByNombre -> {}", exists));
    }

    @Override
    public Mono<Franquicia> findById(Long id) {
        return reactiveRepo.findById(id)
                .map(FranquiciaR2dbcAdapter::toDomain)
                .doOnSubscribe(s -> log.debug("findById({})", id))
                .doOnSuccess(found -> log.debug("findById -> {}", found));
    }

    // ---- Mapeos ----
    private static Franquicia toDomain(FranquiciaData data) {
        return Franquicia.builder()
                .id(data.id)
                .nombre(data.nombre)
                .build();
    }

    private static FranquiciaData toData(Franquicia f) {
        return new FranquiciaData(f.id(), f.nombre());
    }
}
