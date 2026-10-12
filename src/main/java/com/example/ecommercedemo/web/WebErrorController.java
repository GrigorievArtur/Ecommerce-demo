package com.example.ecommercedemo.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Replaces Spring Boot's default error page with one that uses the site layout for
 * browsers, while still returning JSON to API clients.
 */
@Controller
public class WebErrorController implements ErrorController {

    @RequestMapping(value = "${server.error.path:${error.path:/error}}", produces = MediaType.TEXT_HTML_VALUE)
    public String htmlError(HttpServletRequest request, HttpServletResponse response, Model model) {
        int status = statusCode(request);
        response.setStatus(status);
        model.addAttribute("statusCode", status);
        model.addAttribute("errorTitle", title(status));
        model.addAttribute("errorMessage", message(request, status));
        return "error";
    }

    @RequestMapping("${server.error.path:${error.path:/error}}")
    public ResponseEntity<Map<String, Object>> jsonError(HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode(request));
        status = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        body.put("path", path != null ? path.toString() : request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private int statusCode(HttpServletRequest request) {
        Object value = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        return value instanceof Integer code ? code : 500;
    }

    private String title(int status) {
        return switch (status) {
            case 400 -> "Bad request";
            case 401 -> "Please sign in";
            case 403 -> "Access denied";
            case 404 -> "Page not found";
            case 409 -> "That action couldn’t be completed";
            default -> "Something went wrong";
        };
    }

    private String message(HttpServletRequest request, int status) {
        return switch (status) {
            case 400 -> "We couldn’t process that request.";
            case 401 -> "Sign in to continue.";
            case 403 -> "You don’t have permission to view that page.";
            case 404 -> "The page you were looking for doesn’t exist or has moved.";
            case 409 -> "Please check your details and try again.";
            default -> {
                Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
                yield message != null && !message.toString().isBlank()
                        ? message.toString()
                        : "An unexpected error occurred. Please try again.";
            }
        };
    }
}
