package no.nav.folketrygdloven.kalkulus.web.app.konfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import no.nav.vedtak.exception.TekniskException;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.folketrygdloven.kalkulus.web.app.exceptions.ConstraintViolationMapper;
import no.nav.folketrygdloven.kalkulus.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.folketrygdloven.kalkulus.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.folketrygdloven.kalkulus.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.folketrygdloven.kalkulus.web.app.jackson.JacksonJsonConfig;
import no.nav.folketrygdloven.kalkulus.domene.rest.GrunnbeløpRestTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.rest.HentKalkulusRestTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.rest.OperereKalkulusRestTjeneste;
import no.nav.foreldrepenger.konfig.Environment;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    public static final String API_URI = "/api";

    private static final Environment ENV = Environment.current();

    static {
        // config for OpenAPI
        ModelResolver.enumsAsRef = true; // use reusable enums (do not inline per api)
    }

    public ApiConfig() {
        var oas = new OpenAPI();
        var info = new Info().title("Vedtaksløsningen - Kalkulus").version("1.0").description("REST grensesnitt for fp-kalkulus.");

        oas.info(info).addServersItem(new Server().url(ENV.getProperty("context.path", "/fpkalkulus")));

        var oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .resourceClasses(getClasses().stream().map(Class::getName).collect(Collectors.toSet()));
        try {
            new JaxrsOpenApiContextBuilder<>()
                .application(this)
                .openApiConfiguration(oasConfig)
                .buildContext(true)
                .read();
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        //kalkulus rest
        classes.add(OperereKalkulusRestTjeneste.class);
        classes.add(HentKalkulusRestTjeneste.class);
        classes.add(GrunnbeløpRestTjeneste.class);

        //andre tjenester
        classes.add(AuthenticationFilter.class);
        classes.add(OpenApiResource.class);
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
