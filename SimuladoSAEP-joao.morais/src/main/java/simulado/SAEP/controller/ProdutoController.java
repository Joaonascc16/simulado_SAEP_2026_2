package simulado.SAEP.controller;

import simulado.SAEP.entity.ProdutoEntity;
import simulado.SAEP.service.MovimentacaoService;
import simulado.SAEP.service.ProdutoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import simulado.SAEP.entity.UsuarioEntity;
import simulado.SAEP.enums.Perfil;

import java.util.List;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private MovimentacaoService movimentacaoService;

    @GetMapping("/cadastro-produto")
    public String cadastroProduto(
            @RequestParam(value = "busca", required = false) String busca,
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/login";
        }

        model.addAttribute("produtos", produtoService.buscarPorNome(busca));
        model.addAttribute("produto", new ProdutoEntity());
        model.addAttribute("busca", busca);

        return "cadastro-produto";
    }

    @PostMapping("/produtos/salvar")
    public String salvarProduto(
            @ModelAttribute ProdutoEntity produto,
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/login";
        }

        if (produto.getNome() == null ||
                produto.getNome().trim().isEmpty() ||
                produto.getPreco() == null ||
                produto.getPreco() < 0 ||
                produto.getQuantidade() == null ||
                produto.getQuantidade() < 0) {

            model.addAttribute(
                    "erro",
                    "Preencha todos os campos com valores válidos."
            );

            model.addAttribute("produtos", produtoService.listarTodos());
            model.addAttribute("produto", produto);

            return "cadastro-produto";
        }

        produtoService.salvar(produto);

        return "redirect:/cadastro-produto";
    }

    @GetMapping("/produtos/editar/{id}")
    public String editarProduto(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/login";
        }

        produtoService.buscarPorId(id)
                .ifPresent(p -> model.addAttribute("produto", p));

        model.addAttribute("produtos", produtoService.listarTodos());

        return "cadastro-produto";
    }

    @GetMapping("/produtos/deletar/{id}")
    public String deletarProduto(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        UsuarioEntity usuario =
                (UsuarioEntity) session.getAttribute("usuarioLogado");

        // Não está logado
        if (usuario == null) {
            return "redirect:/login";
        }

        // Não é administrador
        if (usuario.getPerfil() != Perfil.ADMIN) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Você não possui permissão para excluir produtos."
            );

            return "redirect:/cadastro-produto";
        }

        try {

            produtoService.deletar(id);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Produto excluído com sucesso!"
            );

        } catch (DataIntegrityViolationException e) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não é possível excluir este produto pois ele possui movimentações no histórico de estoque."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Ocorreu um erro ao tentar excluir o produto."
            );
        }

        return "redirect:/cadastro-produto";
    }

    @GetMapping("/gestao-estoque")
    public String gestaoEstoque(
            @RequestParam(value = "ordenar", defaultValue = "nome") String ordenar,
            @RequestParam(value = "direcao", defaultValue = "asc") String direcao,
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/login";
        }

        List<ProdutoEntity> produtos =
                produtoService.ordenarProdutos(ordenar, direcao);

        int totalEstoque = 0;
        int estoqueBaixo = 0;
        int semEstoque = 0;

        for (ProdutoEntity produto : produtos) {

            totalEstoque += produto.getQuantidade();

            if (produto.getQuantidade() == 0) {

                semEstoque++;

            } else if (produto.getQuantidade()
                    <= produto.getEstoqueMinimo()) {

                estoqueBaixo++;
            }
        }

        model.addAttribute(
                "usuario",
                session.getAttribute("usuarioLogado")
        );

        model.addAttribute("produtos", produtos);
        model.addAttribute("totalEstoque", totalEstoque);
        model.addAttribute("produtosEstoqueBaixo", estoqueBaixo);
        model.addAttribute("produtosSemEstoque", semEstoque);

        model.addAttribute(
                "movimentacoes",
                movimentacaoService.listarTodas()
        );

        model.addAttribute("ordenar", ordenar);
        model.addAttribute("direcao", direcao);

        return "gestao-estoque";
    }
}