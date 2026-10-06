package simulado.SAEP.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import simulado.SAEP.dtos.UsuarioDto;
import simulado.SAEP.entity.UsuarioEntity;
import simulado.SAEP.service.UsuarioService;

import java.util.Optional;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("usuarioDto", new UsuarioDto());
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(@ModelAttribute UsuarioDto usuarioDto, Model model, HttpSession session) {
        if (usuarioDto.getLogin() == null || usuarioDto.getLogin().trim().isEmpty() ||
                usuarioDto.getSenha() == null || usuarioDto.getSenha().trim().isEmpty()) {
            model.addAttribute("erro", "Preencha todos os campos.");
            return "login";

        }
        Optional<UsuarioEntity> usuarioOpt = usuarioService.autenticar(usuarioDto.getLogin(), usuarioDto.getSenha());

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("erro!", "login ou senha inválidos!");
            return "login";
        }
        session.setAttribute("usuarioLogado", usuarioOpt.get());
        return "redirect:/home";
    }
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        UsuarioEntity usuario = (UsuarioEntity) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

