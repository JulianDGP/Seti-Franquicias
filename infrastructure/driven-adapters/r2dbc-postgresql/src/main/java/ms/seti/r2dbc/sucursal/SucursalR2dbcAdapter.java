package ms.seti.r2dbc.sucursal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SucursalR2dbcAdapter  implements SucursalRepository {

    private final SucursalDataRepository reactiveRepo;

    @Override
    public Mono<Sucursal> create(Sucursal s) {
        var data = toData(s);
        return reactiveRepo.save(data)
                .doOnSubscribe(sub -> log.info("Insertando sucursal '{}'", data.nombre))
                .map(SucursalR2dbcAdapter::toDomain)
                .doOnSuccess(x -> log.info("Persistida sucursal id={}", x.id()))
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El nombre de la sucursal ya existe para esta franquicia", e));
    }

    @Override
    public Mono<Boolean> existsByFranquiciaIdAndNombre(Long franquiciaId, String nombre) {
        return reactiveRepo.existsByFranquiciaIdAndNombre(franquiciaId, nombre)
                .doOnSubscribe(sub -> log.debug("existsByFranquiciaIdAndNombre(fid={}, nombre={})", franquiciaId, nombre))
                .doOnNext(exists -> log.debug("exists -> {}", exists));
    }

    @Override
    public Mono<Sucursal> findById(Long id) {
        return reactiveRepo.findById(id)
                .map(SucursalR2dbcAdapter::toDomain)
                .doOnSubscribe(sub -> log.debug("findById({})", id))
                .doOnSuccess(suc -> log.debug("findById -> {}", suc));
    }

    @Override
    public Mono<Sucursal> updateNombre(Long id, String nuevoNombre) {
        return reactiveRepo.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Sucursal no encontrada")))
                .flatMap(entity -> {
                    entity.nombre = nuevoNombre;
                    return reactiveRepo.save(entity);
                })
                .doOnSubscribe(sub -> log.info("Actualizando nombre de sucursal id={} -> '{}'", id, nuevoNombre))
                .map(SucursalR2dbcAdapter::toDomain)
                .doOnSuccess(updated -> log.info("Actualizada sucursal id={} nombre='{}'", updated.id(), updated.nombre()))
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El nombre de la sucursal ya existe para esta franquicia", e));
    }

    private static Sucursal toDomain(SucursalData d) {
        return Sucursal.builder().id(d.id).franquiciaId(d.franquiciaId).nombre(d.nombre).build();
    }
    private static SucursalData toData(Sucursal s) { return new SucursalData(s.id(), s.franquiciaId(), s.nombre()); }
}
