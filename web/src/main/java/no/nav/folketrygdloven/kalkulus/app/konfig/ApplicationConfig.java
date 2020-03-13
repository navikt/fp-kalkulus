package no.nav.folketrygdloven.kalkulus.app.konfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

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
import no.nav.folketrygdloven.kalkulus.rest.HentKalkulusRestTjeneste;
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

        //andre tjenester
        classes.add(OpenApiResource.class);
        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.add(GeneralRestExceptionMapper.class);
        classes.add(JacksonJsonConfig.class);

        return Collections.unmodifiableSet(classes);
    }
}
