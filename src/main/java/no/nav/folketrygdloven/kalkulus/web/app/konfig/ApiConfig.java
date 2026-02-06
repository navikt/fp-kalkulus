package no.nav.folketrygdloven.kalkulus.web.app.konfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import no.nav.folketrygdloven.kalkulus.domene.rest.GrunnbeløpRestTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.rest.HentKalkulusRestTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.rest.OperereKalkulusRestTjeneste;
import no.nav.folketrygdloven.kalkulus.web.app.exceptions.ConstraintViolationMapper;
import no.nav.folketrygdloven.kalkulus.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.folketrygdloven.kalkulus.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.folketrygdloven.kalkulus.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.folketrygdloven.kalkulus.web.app.jackson.JacksonJsonConfig;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    public static final String API_URI = "/api";

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        //kalkulus rest
        classes.add(OperereKalkulusRestTjeneste.class);
        classes.add(HentKalkulusRestTjeneste.class);
        classes.add(GrunnbeløpRestTjeneste.class);

        //andre tjenester
        classes.add(AuthenticationFilter.class);
        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.add(GeneralRestExceptionMapper.class);
        classes.add(JacksonJsonConfig.class);
        return Collections.unmodifiableSet(classes);
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }
}
