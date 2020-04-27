package no.nav.folketrygdloven.kalkulus.app.jackson;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import no.nav.folketrygdloven.kalkulus.rest.HentKalkulusRestTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.OperereKalkulusRestTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.UtledKalkulusRestTjeneste;

public class RestImplementationClasses {
    public Collection<Class<?>> getImplementationClasses() {
        Set<Class<?>> classes = new HashSet<>();

        //kalkulus rest
        classes.add(OperereKalkulusRestTjeneste.class);
        classes.add(UtledKalkulusRestTjeneste.class);
        classes.add(HentKalkulusRestTjeneste.class);
        return classes;
    }
}
