package no.nav.folketrygdloven.kalkulus.håndtering;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;


/**
 * Marker type definerer adapter for å transformere en Dto til et tjenestekall.
 * <p>
 * Beans bør være @ApplicationScoped eller @RequestScoped slik at de ikke trenger å destroyes etter oppslag.
 */
@Repeatable(DtoTilServiceAdapter.ContainerOfDtoTilServiceAdapter.class)
@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface DtoTilServiceAdapter {

    /**
     * Identifiserer Dto denne adapteren håndterer
     */
    Class<?> dto();

    /**
     * Identifiserer adapter som håndterer DTO
     */
    Class<?> adapter();

    /** For søk på annotation. */
    public static class Literal extends AnnotationLiteral<DtoTilServiceAdapter> implements DtoTilServiceAdapter {

        private Class<?> dto;
        private Class<?> adapter;

        public Literal(Class<?> dto, Class<?> adapter) {
            this.dto = dto;
            this.adapter = adapter;

        }

        @Override
        public Class<?> dto() {
            return dto;
        }

        @Override
        public Class<?> adapter() {
            return adapter;
        }

    }

    /**
     * container for repeatable annotations.
     *
     * @see https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    @Documented
    public @interface ContainerOfDtoTilServiceAdapter {
        DtoTilServiceAdapter[] value();
    }
}
