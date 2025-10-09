package com.example.fitnesstracker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Punto de entrada para manejar errores de autenticación.
 *
 * Se activa cuando un usuario intenta acceder a un recurso protegido
 * sin estar autenticado o con credenciales inválidas.
 *
 * Retorna una respuesta JSON con el error en lugar del comportamiento
 * por defecto de Spring Security (redirección a login).
 *
 * @author Fitness Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Maneja intentos de acceso no autorizados.
     *
     * Retorna un JSON con el error 401 Unauthorized.
     *
     * @param request Request HTTP
     * @param response Response HTTP
     * @param authException Excepción de autenticación
     * @throws IOException Si ocurre un error al escribir la respuesta
     * @throws ServletException Si ocurre un error en el servlet
     */
    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        logger.error("Error de autenticación: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", "Acceso no autorizado. Por favor, proporcione un token JWT válido.");
        body.put("path", request.getServletPath());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}