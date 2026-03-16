package no.nav.folketrygdloven.kalkulus.web.app.konfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

class RestApiTester {

    static Collection<Method> finnAlleRestMetoder() {
        List<Method> liste = new ArrayList<>();
        for (Class<?> klasse : finnAlleRestTjenester()) {
            for (Method method : klasse.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    liste.add(method);
                }
            }
        }
        return liste;
    }

    static Collection<Class<?>> finnAlleRestTjenester() {
        List<Class<?>> klasser = new ArrayList<>();
        ApiConfig config = new ApiConfig();
        Collection<Class<?>> c = finnAlleRestTjenester(config);
        klasser.addAll(c);
        return klasser;
    }

    static Collection<Class<?>> finnAlleRestTjenester(Application config) {
        return config.getClasses().stream()
            .filter(c -> c.getAnnotation(Path.class) != null)
            .collect(Collectors.toList());
    }
}
