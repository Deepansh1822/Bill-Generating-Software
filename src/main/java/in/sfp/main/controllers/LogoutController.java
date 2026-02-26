package in.sfp.main.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Simple logout endpoint that clears the JWT cookie and redirects to the login
 * page.
 * The security filter will then treat subsequent requests as unauthenticated.
 */
@Controller
public class LogoutController {

    @GetMapping("/billing-app/api/logout")
    public String logout(HttpServletResponse response) {
        // Delete the JWT cookie(s) (if they exist)
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        // Also clear legacy 'jwt' cookie just in case
        Cookie legacyCookie = new Cookie("jwt", null);
        legacyCookie.setPath("/");
        legacyCookie.setHttpOnly(true);
        legacyCookie.setMaxAge(0);
        response.addCookie(legacyCookie);
        // Redirect to the login page (or any public landing page)
        return "redirect:/billing-app/api/Login";
    }
}
