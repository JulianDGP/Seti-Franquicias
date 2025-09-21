package ms.seti.r2dbc.sucursal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SucursalR2dbcAdapter  implements SucursalRepository {
    private final R2dbcEntityTemplate template;

    @Override
    public Mono<Boolean> existsByFranquiciaIdAndNombre(Long franquiciaId, String nombre) {
        var query = Query.query(Criteria.where("franquicia_id").is(franquiciaId).and("nombre").is(nombre));
        return template.exists(query, SucursalData.class)
                .doOnSubscribe(s -> log.debug("existsByFranquiciaIdAndNombre(fid={}, nombre={})", franquiciaId, nombre))
                .doOnNext(exists -> log.debug("exists -> {}", exists));
    }

    @Override
    public Mono<Sucursal> save(Sucursal s) {
        var sucursalData = toData(s);
        Mono<SucursalData> op = (sucursalData.id == null)
                ? template.insert(SucursalData.class).using(sucursalData)
                : template.update(sucursalData);

        return op.doOnSubscribe(su -> log.info("{} sucursal '{}'",
                        sucursalData.id == null ? "Insertando" : "Actualizando", sucursalData.nombre))
                .map(SucursalR2dbcAdapter::toDomain)
                .doOnSuccess(x -> log.info("Persistida sucursal id={}", x.id()));
    }

    @Override
    public Mono<Sucursal> findById(Long id) {
        var query = Query.query(Criteria.where("id").is(id));
        return template.selectOne(query, SucursalData.class)
                .doOnSubscribe(s -> log.debug("findById({})", id))
                .map(SucursalR2dbcAdapter::toDomain)
                .doOnSuccess(suc -> log.debug("findById -> {}", suc));
    }

    private static Sucursal toDomain(SucursalData d) {
        return Sucursal.builder().id(d.id).franquiciaId(d.franquiciaId).nombre(d.nombre).build();
    }
    private static SucursalData toData(Sucursal s) { return new SucursalData(s.id(), s.franquiciaId(), s.nombre()); }
}
