package no.nav.folketrygdloven.kalkulus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.util.AnnotationLiteral;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * Marker type som implementerer interface {@link BehandlingSteg} for å skille ulike implementasjoner av samme steg for ulike ytelser (eks.
 * Foreldrepenger vs. Engangsstønad).<br>
 * <p>
 * NB: Settes kun dersom det er flere implementasjoner med samme {@link BehandlingStegRef}.
 */
@Repeatable(FagsakYtelseTypeRef.ContainerOfFagsakYtelseTypeRef.class)
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface FagsakYtelseTypeRef {

    /**
     * Kode-verdi som skiller ulike implementasjoner for ulike behandling typer.
     * <p>
     * Må matche ett innslag i <code>FAGSAK_YTELSE_TYPE</code> tabell for å kunne kjøres.
     *
     * @see no.nav.folketrygdloven.kalkulus.kodeverk.behandlingslager.fagsak.FagsakYtelseType
     */
    FagsakYtelseType value() default FagsakYtelseType.UDEFINERT;

    /**
     * AnnotationLiteral som kan brukes ved CDI søk.
     */
    class FagsakYtelseTypeRefLiteral extends AnnotationLiteral<FagsakYtelseTypeRef> implements FagsakYtelseTypeRef {

        private FagsakYtelseType ytelseType;

        public FagsakYtelseTypeRefLiteral() {
            this(FagsakYtelseType.UDEFINERT);
        }

        public FagsakYtelseTypeRefLiteral(FagsakYtelseType ytelseType) {
            this.ytelseType = ytelseType != null ? ytelseType : FagsakYtelseType.UDEFINERT;
        }

        @Override
        public FagsakYtelseType value() {
            return ytelseType;
        }

    }

    @SuppressWarnings("unchecked")
    final class Lookup {

        private Lookup() {
        }

        /**
         * Kan brukes til å finne instanser blant angitte som matcher følgende kode, eller default '*' implementasjon. Merk at Instance bør være
         * injected med riktig forventet klassetype og @Any qualifier.
         */
        public static <I> Optional<I> find(Instance<I> instances, FagsakYtelseType ytelseType) {
            Objects.requireNonNull(instances, "instances");

            for (var fagsakLiteral : coalesce(ytelseType, FagsakYtelseType.UDEFINERT)) {
                var inst = instances.select(new FagsakYtelseTypeRefLiteral(fagsakLiteral));
                if (inst.isResolvable()) {
                    return Optional.of(getInstance(inst));
                } else {
                    if (inst.isAmbiguous()) {
                        throw new IllegalStateException("Har flere matchende instanser for klasse : FagsakYtelseTypeRef, fagsakType=" + fagsakLiteral);
                    }
                }
            }

            return Optional.empty();
        }

        private static <I> I getInstance(Instance<I> inst) {
            var i = inst.get();
            if (i.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                        "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + i.getClass());
            }
            return i;
        }

        private static List<FagsakYtelseType> coalesce(FagsakYtelseType... vals) {
            return Arrays.stream(vals).filter(Objects::nonNull).distinct().toList();
        }
    }

    /**
     * container for repeatable annotations.
     *
     * @see https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
    @Documented
    @interface ContainerOfFagsakYtelseTypeRef {
        FagsakYtelseTypeRef[] value();
    }
}
