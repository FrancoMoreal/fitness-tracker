package com.example.fitnesstracker.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public static final String COOKIE_NAME = "fitness_tracker_token";

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public void addAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);        // cambiar a true en producción (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpirationMs / 1000)); // convertir ms → segundos
        // SameSite no tiene setter directo en Jakarta Cookie, se agrega via header
        response.addCookie(cookie);
        // Agregar SameSite=Strict explícitamente
        response.addHeader("Set-Cookie",
                COOKIE_NAME + "=" + token +
                        "; Max-Age=" + (jwtExpirationMs / 1000) +
                        "; Path=/" +
                        "; HttpOnly" +
                        "; SameSite=Strict"
        );
    }

    public void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);        // debe coincidir con addAuthCookie
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        response.addHeader("Set-Cookie",
                COOKIE_NAME + "=" +
                        "; Max-Age=0" +
                        "; Path=/" +
                        "; HttpOnly" +
                        "; SameSite=Strict"
        );
    }
}