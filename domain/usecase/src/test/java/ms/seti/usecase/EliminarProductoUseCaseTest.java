package ms.seti.usecase;

import org.junit.jupiter.api.Test;

import ms.seti.model.producto.gateways.ProductoRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EliminarProductoUseCaseTest {

    private final ProductoRepository repo = Mockito.mock(ProductoRepository.class);
    private final EliminarProductoUseCase useCase = new EliminarProductoUseCase(repo);

    @Test
    void elimina_ok() {
        // given
        when(repo.deleteById(10L)).thenReturn(Mono.empty());

        // when + then
        StepVerifier.create(useCase.execute(10L))
                .verifyComplete();

        verify(repo).deleteById(10L);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void id_requerido() {
        // when + then
        StepVerifier.create(useCase.execute(null))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("requerido"))
                .verify();

        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    void producto_no_encontrado_propagado() {
        // given
        when(repo.deleteById(999L)).thenReturn(Mono.error(new NoSuchElementException("Producto no encontrado")));

        // when + then
        StepVerifier.create(useCase.execute(999L))
                .expectErrorMatches(e -> e instanceof NoSuchElementException
                        && e.getMessage().contains("no encontrado"))
                .verify();

        verify(repo).deleteById(999L);
        verifyNoMoreInteractions(repo);
    }
}