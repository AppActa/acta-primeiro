package br.com.acta.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Reaproveita arquivos estáticos, conferindo atualizações antes de usar o cache. */
@WebFilter(filterName = "CacheRecursosFilter",
        urlPatterns = {"/assets/css/*", "/assets/js/*", "/assets/images/*"})
public class CacheRecursosFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if ("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod())) {
            // O Tomcat mantém ETag/Last-Modified e responde 304 quando o arquivo não mudou.
            resp.setHeader("Cache-Control", "public, max-age=0, must-revalidate");
        }
        chain.doFilter(request, response);
    }
}
