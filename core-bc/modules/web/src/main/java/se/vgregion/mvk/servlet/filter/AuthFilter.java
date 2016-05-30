package se.vgregion.mvk.servlet.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Patrik Björk
 */
@WebFilter("*")
public class AuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    private String userIdHeader = "AJP_sn_id";

    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Filter init...");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        LOGGER.debug("RequestURI: " + request.getRequestURI());

        String env = System.getProperty("env");

        try {
            if (env != null && env.equalsIgnoreCase("dev")) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {

                String ajpSnId = request.getHeader(this.userIdHeader);
                if (ajpSnId != null && !"".equals(ajpSnId) && ajpSnId.length() > 0) {
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    writeErrorPage(response, 401, "Inloggning krävs.");
                }

            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void writeErrorPage(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setContentType("text/html");

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            LOGGER.error("Request without " + userIdHeader + " set.");

            StringBuilder sb = new StringBuilder();

            sb.append("<!DOCTYPE html>" + System.getProperty("line.separator"));
            sb.append("<html>" +
                    "<head><meta charset=\"utf-8\"></meta></head>" +
                    "<body>");
            sb.append(message);
            sb.append("</body></html>");

            outputStream.write(sb.toString().getBytes("UTF-8"));
            response.setStatus(statusCode);
        }
    }

    public void destroy() {

    }
}
