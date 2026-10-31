package com.apigestao.transacoes.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TitularidadeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String usuarioIdHeader = request.getHeader("X-Usuario-Id");

        // Só valida quando há usuário autenticado e o header foi enviado.
        // Rotas sem autenticação (Swagger, docs) já foram liberadas pelo
        // SecurityConfig e nem chegam até aqui autenticadas.
        if (authentication != null
                && authentication.getPrincipal() instanceof UsuarioAutenticado usuarioAutenticado
                && usuarioIdHeader != null) {

            try {
                Long usuarioIdSolicitado = Long.parseLong(usuarioIdHeader);

                if (!usuarioIdSolicitado.equals(usuarioAutenticado.getId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"message\":\"Você não pode acessar dados de outro usuário.\"}"
                    );
                    return;
                }

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\":\"Cabeçalho X-Usuario-Id inválido.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
