package pe.edu.upeu.clinica.shared.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiCacheControlFilterTest {

    private final ApiCacheControlFilter filter = new ApiCacheControlFilter();

    @Test
    void evitaGuardarRespuestasDeLaApiEnCache() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pacientes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("no-store", response.getHeader("Cache-Control"));
    }

    @Test
    void noModificaCabecerasFueraDeLaApi() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(response.getHeader("Cache-Control"));
    }
}
