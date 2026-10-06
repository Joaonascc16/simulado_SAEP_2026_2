package simulado.SAEP.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simulado.SAEP.entity.ProdutoEntity;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {

    List<ProdutoEntity> findByNomeContainingIgnoreCase(String nome);
}
