package ms.seti.usecase;

import ms.seti.model.franquicia.Franquicia;
import ms.seti.model.franquicia.gateways.FranquiciaRepository;
import ms.seti.model.sucursal.Sucursal;
import ms.seti.model.sucursal.gateways.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrearSucursalUseCaseTest {

    private SucursalRepository sucRepo;
    private FranquiciaRepository franRepo;
    private CrearSucursalUseCase useCase;

    @BeforeEach
    void setUp() {
        sucRepo = mock(SucursalRepository.class);
        franRepo = mock(FranquiciaRepository.class);
        useCase = new CrearSucursalUseCase(sucRepo, franRepo);
    }

    @Test
    @DisplayName("Crea sucursal cuando la franquicia existe y el nombre es único")
    void crea_ok() {
        Long franquiciaId = 1L;
        String nombre = "  Centro  "; // con espacios para cubrir normalización

        Franquicia franquicia = Franquicia.builder().id(franquiciaId).nombre("F1").build();
        Sucursal creada = Sucursal.builder().id(100L).franquiciaId(franquiciaId).nombre("Centro").build();

        when(franRepo.findById(franquiciaId)).thenReturn(Mono.just(franquicia));
        when(sucRepo.existsByFranquiciaIdAndNombre(franquiciaId, "Centro")).thenReturn(Mono.just(false));
        when(sucRepo.create(any(Sucursal.class))).thenReturn(Mono.just(creada));

        StepVerifier.create(useCase.execute(franquiciaId, nombre))
                .expectNextMatches(s -> s.id().equals(100L) && s.nombre().equals("Centro"))
                .verifyComplete();

        verify(franRepo).findById(franquiciaId);
        verify(sucRepo).existsByFranquiciaIdAndNombre(franquiciaId, "Centro");
        verify(sucRepo).create(any(Sucursal.class));
    }

    @Test
    @DisplayName("Lanza 400 si el nombre es nulo o vacío (normalización)")
    void nombre_invalido_badRequest() {
        StepVerifier.create(useCase.execute(1L, "   "))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(IllegalArgumentException.class);
                    assertThat(e).hasMessage("El nombre es requerido");
                })
                .verify();

        verifyNoInteractions(franRepo, sucRepo);
    }

    @Test
    @DisplayName("Lanza 404 si la franquicia no existe")
    void franquicia_no_encontrada_notFound() {
        Long franquiciaId = 2L;
        when(franRepo.findById(franquiciaId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(franquiciaId, "Sur"))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NoSuchElementException.class);
                    assertThat(e).hasMessage("Franquicia no encontrada");
                })
                .verify();

        verify(franRepo).findById(franquiciaId);
        verify(sucRepo, never()).create(any());
    }

    @Test
    @DisplayName("Lanza 409 si el nombre ya existe en la franquicia")
    void nombre_duplicado_conflict() {
        Long franquiciaId = 3L;
        String nombre = "Norte";
        Franquicia franquicia = Franquicia.builder().id(franquiciaId).nombre("F3").build();

        when(franRepo.findById(franquiciaId)).thenReturn(Mono.just(franquicia));
        when(sucRepo.existsByFranquiciaIdAndNombre(franquiciaId, nombre)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(franquiciaId, nombre))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(IllegalStateException.class);
                    assertThat(e).hasMessage("El nombre de la sucursal ya existe para esta franquicia");
                })
                .verify();

        verify(sucRepo, never()).create(any());
    }

    @Test
    @DisplayName("Si el exists() devuelve empty, se considera 'false' y se crea")
    void exists_empty_se_trata_como_falso() {
        Long franquiciaId = 4L;
        String nombre = "Oeste";
        Franquicia franquicia = Franquicia.builder().id(franquiciaId).nombre("F4").build();
        Sucursal creada = Sucursal.builder().id(200L).franquiciaId(franquiciaId).nombre(nombre).build();

        when(franRepo.findById(franquiciaId)).thenReturn(Mono.just(franquicia));
        when(sucRepo.existsByFranquiciaIdAndNombre(franquiciaId, nombre)).thenReturn(Mono.empty()); // defaultIfEmpty(false)
        when(sucRepo.create(any(Sucursal.class))).thenReturn(Mono.just(creada));

        StepVerifier.create(useCase.execute(franquiciaId, nombre))
                .expectNextMatches(s -> s.id().equals(200L) && s.nombre().equals("Oeste"))
                .verifyComplete();

        verify(sucRepo).create(any(Sucursal.class));
    }
}