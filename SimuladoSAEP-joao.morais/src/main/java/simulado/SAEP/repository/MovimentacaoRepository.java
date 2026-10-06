package simulado.SAEP.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simulado.SAEP.entity.MovimentacaoEntity;

import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<MovimentacaoEntity, Long> {

    List<MovimentacaoEntity> findAllByOrderByDataDesc();

    List<MovimentacaoEntity> findByProdutoIdOrderByDataDesc(Long produtoId);
}
