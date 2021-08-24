package no.nav.folketrygdloven.kalkulus.app.konfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.folketrygdloven.kalkulus.app.exceptions.ConstraintViolationMapper;
import no.nav.folketrygdloven.kalkulus.app.exceptions.GeneralRestExceptionMapper;
import no.nav.folketrygdloven.kalkulus.app.exceptions.JsonMappingExceptionMapper;
import no.nav.folketrygdloven.kalkulus.app.exceptions.JsonParseExceptionMapper;
import no.nav.folketrygdloven.kalkulus.app.jackson.JacksonJsonConfig;
import no.nav.folketrygdloven.kalkulus.forvaltning.AksjonspunktMigreringTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.ForvaltningFrisinnRestTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.GrunnbeløpRestTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.HentKalkulusRestTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.MigrerAksjonspunktRestTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.OperereKalkulusRestTjeneste;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    public static final String API_URI = "/api";

    public ApplicationConfig() {
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
                .title("Kalkulus")
                .version("1.0")
                .description("REST grensesnitt for Kalkulus.");

        oas.info(info)
                .addServersItem(new Server()
                        .url("/ftkalkulus"));
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner")
                .resourcePackages(Set.of("no.nav.folketrygdloven"));
        try {
            new JaxrsOpenApiContextBuilder<>()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
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
        classes.add(OpenApiResource.class);
        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.add(GeneralRestExceptionMapper.class);
        classes.add(JacksonJsonConfig.class);

        // forvaltning
        classes.add(ForvaltningFrisinnRestTjeneste.class);
        classes.add(MigrerAksjonspunktRestTjeneste.class);


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
