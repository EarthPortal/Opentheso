package fr.cnrs.opentheso.ws.openapi.filter;

import fr.cnrs.opentheso.services.UserService;
import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.services.security.SsoTokenService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor  // injection via constructeur (Spring)
public class SsoTokenFilter implements Filter {

    private final SsoTokenService ssoTokenService;
    private final UserService userService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ssoToken = request.getParameter("ssoToken");

        if (ssoToken != null && !ssoToken.isBlank()) {

            Integer userId = ssoTokenService.validateAndConsumeToken(ssoToken);

            if (userId != null) {
                User user = userService.findById(userId).orElse(null);

                if (user != null) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute("userConnected", user);
                    session.setAttribute("isLogged", true);

                    log.info("SSO login réussi pour userId : {}", userId);
                    response.sendRedirect(request.getContextPath() + "/index.xhtml");
                    return;
                }
            }

            log.warn("SSO token invalide ou expiré : {}", ssoToken);
            response.sendRedirect(request.getContextPath() + "/index.xhtml?ssoError=true");
            return;
        }

        chain.doFilter(req, res);
    }
}