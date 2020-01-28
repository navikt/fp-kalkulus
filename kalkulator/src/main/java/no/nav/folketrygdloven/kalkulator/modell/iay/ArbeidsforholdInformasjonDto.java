package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.OrderBy;

import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class ArbeidsforholdInformasjonDto {

    @OrderBy("opprettetTidspunkt ASC")
    private Set<ArbeidsforholdReferanseDto> referanser = new LinkedHashSet<>();
    private List<ArbeidsforholdOverstyringDto> overstyringer = new ArrayList<>();

    public ArbeidsforholdInformasjonDto() {
    }

    public ArbeidsforholdInformasjonDto(ArbeidsforholdInformasjonDto arbeidsforholdInformasjon) {
        for (ArbeidsforholdReferanseDto arbeidsforholdReferanse : arbeidsforholdInformasjon.referanser) {
            final ArbeidsforholdReferanseDto referanseEntitet = new ArbeidsforholdReferanseDto(arbeidsforholdReferanse);
            this.referanser.add(referanseEntitet);
        }
        for (ArbeidsforholdOverstyringDto arbeidsforholdOverstyringEntitet : arbeidsforholdInformasjon.overstyringer) {
            final ArbeidsforholdOverstyringDto overstyringEntitet = new ArbeidsforholdOverstyringDto(arbeidsforholdOverstyringEntitet);
            this.overstyringer.add(overstyringEntitet);
        }
    }

    public Collection<ArbeidsforholdReferanseDto> getArbeidsforholdReferanser() {
        return Collections.unmodifiableSet(this.referanser);
    }

    public List<ArbeidsforholdOverstyringDto> getOverstyringer() {
        return Collections.unmodifiableList(this.overstyringer);
    }

    public EksternArbeidsforholdRef finnEkstern(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internReferanse) {
        if (internReferanse.getReferanse() == null) return EksternArbeidsforholdRef.nullRef();

        return referanser.stream()
            .filter(this::erIkkeMerget)
            .filter(r -> Objects.equals(r.getInternReferanse(), internReferanse) && Objects.equals(r.getArbeidsgiver(), arbeidsgiver))
            .findFirst()
            .map(ArbeidsforholdReferanseDto::getEksternReferanse)
            .orElseThrow(
                () -> new IllegalStateException("Mangler eksternReferanse for internReferanse: " + internReferanse + ", arbeidsgiver: " + arbeidsgiver));
    }

    /** @deprecated Bruk {@link ArbeidsforholdInformasjonDto#finnEkstern} i stedet. */
    @Deprecated(forRemoval = true)
    public EksternArbeidsforholdRef finnEksternRaw(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internReferanse) {
        if (internReferanse.getReferanse() == null) return EksternArbeidsforholdRef.nullRef();

        return referanser.stream()
            .filter(r -> Objects.equals(r.getInternReferanse(), internReferanse) && Objects.equals(r.getArbeidsgiver(), arbeidsgiver))
            .findFirst()
            .map(ArbeidsforholdReferanseDto::getEksternReferanse)
            .orElseThrow(
                () -> new IllegalStateException("Mangler eksternReferanse for internReferanse: " + internReferanse + ", arbeidsgiver: " + arbeidsgiver));
    }

    public Optional<InternArbeidsforholdRefDto> finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef arbeidsforholdRef) {
        // For å sike at det ikke mistes data ved sammenslåing av og innhenting av registerdataop
        final Optional<ArbeidsforholdReferanseDto> referanseEntitet = referanser.stream().filter(re -> overstyringer.stream()
            .anyMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getArbeidsgiver().equals(arbeidsgiver)
                && re.getArbeidsgiver().equals(ov.getArbeidsgiver())
                && re.getEksternReferanse().equals(arbeidsforholdRef)
                && re.getInternReferanse().equals(ov.getArbeidsforholdRef())))
            .findAny();
        if (referanseEntitet.isPresent()) {
            return Optional.ofNullable(referanseEntitet.get().getInternReferanse());
        }
        return finnForEkstern(arbeidsgiver, arbeidsforholdRef);
    }

    public Optional<InternArbeidsforholdRefDto> finnForEkstern(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef ref) {
        final List<ArbeidsforholdReferanseDto> arbeidsforholdReferanseEntitetStream = this.referanser.stream()
            .filter(this::erIkkeMerget)
            .collect(Collectors.toList());
        return arbeidsforholdReferanseEntitetStream.stream()
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getEksternReferanse().equals(ref))
            .findFirst().map(ArbeidsforholdReferanseDto::getInternReferanse);
    }

    private boolean erIkkeMerget(ArbeidsforholdReferanseDto arbeidsforholdReferanseEntitet) {
        return overstyringer.stream().noneMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            && ov.getArbeidsgiver().equals(arbeidsforholdReferanseEntitet.getArbeidsgiver())
            && ov.getArbeidsforholdRef().equals(arbeidsforholdReferanseEntitet.getInternReferanse()));
    }

    /**
     * @deprecated FIXME (FC): Trengs denne eller kan vi alltid stole på ref er den vi skal returnere? Skal egentlig returnere ref,
     * men per nå har vi antagelig interne ider som har erstattet andre interne id'er. Må isåfall avsjekke migrering av disse.
     */
    @Deprecated(forRemoval = true)
    public InternArbeidsforholdRefDto finnEllerOpprett(Arbeidsgiver arbeidsgiver, final InternArbeidsforholdRefDto ref) {
        final Optional<ArbeidsforholdOverstyringDto> erstattning = overstyringer.stream()
            .filter(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getArbeidsgiver().equals(arbeidsgiver)
                && ov.getArbeidsforholdRef().gjelderFor(ref))
            .findAny();
        if (erstattning.isPresent() && !erstattning.get().getNyArbeidsforholdRef().equals(ref)) {
            return finnEllerOpprett(arbeidsgiver, erstattning.get().getNyArbeidsforholdRef());
        } else {
            final ArbeidsforholdReferanseDto referanse = this.referanser.stream()
                .filter(this::erIkkeMerget)
                .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getInternReferanse().equals(ref))
                .findFirst().orElseThrow(() -> new IllegalStateException("InternArbeidsforholdReferanse må eksistere fra før, fant ikke: " + ref));

            return referanse.getInternReferanse();
        }
    }

    public InternArbeidsforholdRefDto finnEllerOpprett(Arbeidsgiver arbeidsgiver, final EksternArbeidsforholdRef ref) {
        final Optional<ArbeidsforholdOverstyringDto> erstattning = overstyringer.stream()
            .filter(ov -> {
                var historiskReferanse = finnForEksternBeholdHistoriskReferanse(arbeidsgiver, ref);
                return historiskReferanse.isPresent()
                    && ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                    && ov.getArbeidsgiver().equals(arbeidsgiver)
                    && ov.getArbeidsforholdRef().gjelderFor(historiskReferanse.get());
            })
            .findAny();
        if (erstattning.isPresent()) {
            return finnEllerOpprett(arbeidsgiver, erstattning.get().getNyArbeidsforholdRef());
        } else {
            final ArbeidsforholdReferanseDto referanse = finnEksisterendeInternReferanseEllerOpprettNy(arbeidsgiver, ref);
            return referanse.getInternReferanse();
        }
    }

    private Optional<ArbeidsforholdReferanseDto> referanseEksistererIkke(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto ref) {
        return this.referanser.stream()
            .filter(this::erIkkeMerget)
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getInternReferanse().equals(ref))
            .findAny();
    }

    private ArbeidsforholdReferanseDto finnEksisterendeInternReferanseEllerOpprettNy(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef eksternReferanse) {
        return finnEksisterendeReferanse(arbeidsgiver, eksternReferanse)
            .orElseGet(() -> opprettNyReferanse(arbeidsgiver, InternArbeidsforholdRefDto.nyRef(), eksternReferanse));
    }

    private Optional<ArbeidsforholdReferanseDto> finnEksisterendeReferanse(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef ref) {
        return this.referanser.stream()
            .filter(this::erIkkeMerget)
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getEksternReferanse().equals(ref))
            .findAny();
    }

    /**
     * @deprecated Bruk {@link ArbeidsforholdInformasjonDtoBuilder} i stedet.
     */
    @Deprecated(forRemoval = true)
    public ArbeidsforholdReferanseDto opprettNyReferanse(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internReferanse,
                                                         EksternArbeidsforholdRef eksternReferanse) {
        final ArbeidsforholdReferanseDto arbeidsforholdReferanse = new ArbeidsforholdReferanseDto(arbeidsgiver,
            internReferanse, eksternReferanse);
        referanser.add(arbeidsforholdReferanse);
        return arbeidsforholdReferanse;
    }

    void leggTilNyReferanse(ArbeidsforholdReferanseDto arbeidsforholdReferanse) {
        referanser.add(arbeidsforholdReferanse);
    }

    ArbeidsforholdOverstyringDtoBuilder getOverstyringBuilderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto ref) {
        return ArbeidsforholdOverstyringDtoBuilder.oppdatere(this.overstyringer
            .stream()
            .filter(ov -> ov.getArbeidsgiver().equals(arbeidsgiver)
                && ov.getArbeidsforholdRef().gjelderFor(ref))
            .findFirst())
            .medArbeidsforholdRef(ref)
            .medArbeidsgiver(arbeidsgiver);
    }

    void leggTilOverstyring(ArbeidsforholdOverstyringDto build) {
        this.overstyringer.add(build);
    }

    void tilbakestillOverstyringer() {
        this.overstyringer.clear();
    }

    void fjernOverstyringerSomGjelder(Arbeidsgiver arbeidsgiver) {
        this.overstyringer.removeIf(ov -> arbeidsgiver.equals(ov.getArbeidsgiver()));
    }

    void erstattArbeidsforhold(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto gammelRef, InternArbeidsforholdRefDto ref) {
        final Optional<ArbeidsforholdReferanseDto> referanseEntitet = referanseEksistererIkke(arbeidsgiver, gammelRef);
        referanseEntitet.ifPresent(it -> opprettNyReferanse(arbeidsgiver, ref, it.getEksternReferanse()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof ArbeidsforholdInformasjonDto))
            return false;
        ArbeidsforholdInformasjonDto that = (ArbeidsforholdInformasjonDto) o;
        return Objects.equals(referanser, that.referanser) &&
            Objects.equals(overstyringer, that.overstyringer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanser, overstyringer);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdInformasjonEntitet{" +
            "referanser=" + referanser +
            ", overstyringer=" + overstyringer +
            '}';
    }

    void fjernOverstyringVedrørende(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        overstyringer.removeIf(ov -> !Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            && ov.getArbeidsgiver().equals(arbeidsgiver)
            && ov.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef));
    }
}
