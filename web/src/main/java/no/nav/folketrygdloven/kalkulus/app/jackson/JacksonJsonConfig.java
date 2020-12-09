package no.nav.folketrygdloven.kalkulus.app.jackson;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.folketrygdloven.kalkulus.app.IndexClasses;

@Provider
public class JacksonJsonConfig implements ContextResolver<ObjectMapper> {

    private static final SimpleModule SER_DESER = createModule();
    private final ObjectMapper objectMapper;

    public JacksonJsonConfig() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(SER_DESER);

        Collection<Class<?>> restClasses = new RestImplementationClasses().getImplementationClasses();

        Set<Class<?>> scanClasses = new LinkedHashSet<>(restClasses);

        // avled code location fra klassene
        scanClasses
                .stream()
                .map(c -> {
                    try {
                        return c.getProtectionDomain().getCodeSource().getLocation().toURI();
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException("Ikke en URI for klasse: " + c, e);
                    }
                })
                .distinct()
                .forEach(uri -> objectMapper.registerSubtypes(getJsonTypeNameClasses(uri)));
    }

    public static Module defaultModule() {
        return SER_DESER;
    }

    private static SimpleModule createModule() {
        SimpleModule module = new SimpleModule("VL-REST", new Version(1, 0, 0, null, null, null));

        addSerializers(module);

        return module;
    }

    private static void addSerializers(SimpleModule module) {
        module.addSerializer(new StringSerializer());
    }

    /**
     * Scan subtyper dynamisk fra WAR slik at superklasse slipper å deklarere @JsonSubtypes.
     */
    public static List<Class<?>> getJsonTypeNameClasses(URI uri) {
        IndexClasses indexClasses;
        indexClasses = IndexClasses.getIndexFor(uri);
        return indexClasses.getClassesWithAnnotation(JsonTypeName.class);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

}
