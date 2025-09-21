package ms.seti.r2dbc.franquicia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FranquiciaR2dbcAdapter implements FranquiciaRepository {


    private final R2dbcEntityTemplate template;

    // ---- Implementación del gateway ----

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        var q = Query.query(Criteria.where("nombre").is(nombre));
        return template.exists(q, FranquiciaData.class)
                .doOnSubscribe(s -> log.info("existsByNombre(nombre={})", nombre))
                .doOnNext(exists -> log.info("existsByNombre -> {}", exists));
    }

    @Override
    public Mono<Franquicia> save(Franquicia f) {
        var data = toData(f);
        Mono<FranquiciaData> op = (data.id == null)
                ? template.insert(FranquiciaData.class).using(data)
                : template.update(data);

        return op
                .doOnSubscribe(s -> log.info("Guardando franquicia: {}", f.nombre()))
                .map(FranquiciaR2dbcAdapter::toDomain)
                .doOnSuccess(x -> log.info("Guardada franquicia con id={}", x.id()));
    }

    @Override
    public Mono<Franquicia> findById(Long id) {
        var q = Query.query(Criteria.where("id").is(id));
        return template.selectOne(q, FranquiciaData.class)
                .map(FranquiciaR2dbcAdapter::toDomain)
                .doOnSubscribe(s -> log.info("findById({})", id))
                .doOnSuccess(x -> log.info("findById -> {}", x));
    }

    // ---- Mapeos ----
    private static Franquicia toDomain(FranquiciaData d) {
        return Franquicia.builder()
                .id(d.id)
                .nombre(d.nombre)
                .build();
    }

    private static FranquiciaData toData(Franquicia f) {
        return new FranquiciaData(f.id(), f.nombre());
    }
}
