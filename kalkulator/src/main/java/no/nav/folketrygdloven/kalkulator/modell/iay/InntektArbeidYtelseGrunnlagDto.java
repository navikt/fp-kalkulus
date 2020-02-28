package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;

public class InntektArbeidYtelseGrunnlagDto {

    private UUID uuid;
    private InntektArbeidYtelseAggregatDto register;
    private InntektArbeidYtelseAggregatDto saksbehandlet;
    private OppgittOpptjeningDto oppgittOpptjening;
    private InntektsmeldingAggregatDto inntektsmeldinger;
    private ArbeidsforholdInformasjonDto arbeidsforholdInformasjon;
    private boolean aktiv = true;
    private List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger = new ArrayList<>();

    InntektArbeidYtelseGrunnlagDto() {
        // for hibernate
    }

    public InntektArbeidYtelseGrunnlagDto(InntektArbeidYtelseGrunnlagDto grunnlag) {
        this(UUID.randomUUID(), LocalDateTime.now());

        // NB! skal ikke lage ny versjon av oppgitt opptjening eller andre underlag! Lenker bare inn på ferskt grunnlag
        grunnlag.getOppgittOpptjening().ifPresent(this::setOppgittOpptjening);
        grunnlag.getRegisterVersjon().ifPresent(this::setRegister);
        grunnlag.getSaksbehandletVersjon().ifPresent(this::setSaksbehandlet);
        grunnlag.getInntektsmeldinger().ifPresent(this::setInntektsmeldinger);
        grunnlag.getArbeidsforholdInformasjon().ifPresent(this::setInformasjon);
    }

    public InntektArbeidYtelseGrunnlagDto(UUID grunnlagReferanse, LocalDateTime opprettetTidspunkt) {
        this.uuid = Objects.requireNonNull(grunnlagReferanse, "grunnlagReferanse");
    }

    public InntektArbeidYtelseGrunnlagDto(UUID grunnlagReferanse, LocalDateTime opprettetTidspunkt, InntektArbeidYtelseGrunnlagDto grunnlag) {
        this(grunnlag);
        this.uuid = Objects.requireNonNull(grunnlagReferanse, "grunnlagReferanse");
    }

    /** Identifisere en immutable instans av grunnlaget unikt og er egnet for utveksling (eks. til abakus eller andre systemer) */
    private UUID getEksternReferanse() {
        return uuid;
    }

    /**
     * Returnerer en overstyrt versjon av aggregat. Hvis saksbehandler har løst et aksjonspunkt i forbindele med
     * opptjening vil det finnes et overstyrt aggregat, gjelder for FØR første dag i permisjonsuttaket (skjæringstidspunktet)
     */
    private Optional<InntektArbeidYtelseAggregatDto> getSaksbehandletVersjon() {
        return Optional.ofNullable(saksbehandlet);
    }

    void setSaksbehandlet(InntektArbeidYtelseAggregatDto saksbehandletFør) {
        this.saksbehandlet = saksbehandletFør;
    }

    /**
     * Returnerer innhentede registeropplysninger som aggregat. Tar ikke hensyn til saksbehandlers overstyringer (se
     * {@link #getSaksbehandletVersjon()}.
     */
    public Optional<InntektArbeidYtelseAggregatDto> getRegisterVersjon() {
        return Optional.ofNullable(register);
    }

    /**
     * Returnerer aggregat som holder alle inntektsmeldingene som benyttes i behandlingen.
     */
    public Optional<InntektsmeldingAggregatDto> getInntektsmeldinger() {
        return Optional.ofNullable(inntektsmeldinger);
    }

    void setInntektsmeldinger(InntektsmeldingAggregatDto inntektsmeldingAggregat) {
        this.inntektsmeldinger = inntektsmeldingAggregat;
    }

    /**
     * sjekkom bekreftet annen opptjening. Oppgi aktørId for matchende behandling (dvs.normalt søker).
     */
    public Optional<AktørArbeidDto> getBekreftetAnnenOpptjening(AktørId aktørId) {
        return getSaksbehandletVersjon()
            .map(InntektArbeidYtelseAggregatDto::getAktørArbeid)
            .flatMap(it -> it.stream().filter(aa -> aa.getAktørId().equals(aktørId))
                .findFirst());
    }

    public Optional<AktørArbeidDto> getAktørArbeidFraRegister(AktørId aktørId) {
        if (register != null) {
            var aktørArbeid = register.getAktørArbeid().stream().filter(aa -> Objects.equals(aa.getAktørId(), aktørId)).collect(Collectors.toList());
            if (aktørArbeid.size() > 1) {
                throw new IllegalStateException("Kan kun ha ett innslag av AktørArbeid for aktørId:" + aktørId + " i  grunnlag " + this.getEksternReferanse());
            }
            return aktørArbeid.stream().findFirst();
        }
        return Optional.empty();
    }

    public Optional<AktørYtelseDto> getAktørYtelseFraRegister(AktørId aktørId) {
        if (register != null) {
            var aktørYtelse = register.getAktørYtelse().stream().filter(aa -> Objects.equals(aa.getAktørId(), aktørId)).collect(Collectors.toList());
            if (aktørYtelse.size() > 1) {
                throw new IllegalStateException("Kan kun ha ett innslag av AktørYtelse for aktørId:" + aktørId + " i  grunnlag " + this.getEksternReferanse());
            }
            return aktørYtelse.stream().findFirst();
        }
        return Optional.empty();
    }

    public Optional<AktørInntektDto> getAktørInntektFraRegister(AktørId aktørId) {
        if (register != null) {
            var aktørInntekt = register.getAktørInntekt().stream().filter(aa -> Objects.equals(aa.getAktørId(), aktørId)).collect(Collectors.toList());
            if (aktørInntekt.size() > 1) {
                throw new IllegalStateException("Kan kun ha ett innslag av AktørInntekt for aktørId:" + aktørId + " i  grunnlag " + this.getEksternReferanse());
            }
            return aktørInntekt.stream().findFirst();
        }
        return Optional.empty();
    }

    /**
     * Returnerer oppgitt opptjening hvis det finnes. (Inneholder opplysninger søker opplyser om i søknaden)
     */
    public Optional<OppgittOpptjeningDto> getOppgittOpptjening() {
        return Optional.ofNullable(oppgittOpptjening);
    }

    void setOppgittOpptjening(OppgittOpptjeningDto oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    public List<InntektsmeldingSomIkkeKommerDto> getInntektsmeldingerSomIkkeKommer() {
        if (arbeidsforholdInformasjon == null) {
            return Collections.emptyList();
        } else {
            var overstyringer = arbeidsforholdInformasjon.getOverstyringer();
            return overstyringer.stream()
                .filter(ov -> ov.kreverIkkeInntektsmelding())
                .map(ov -> {
                    // TODO (FC): fiks/fjern eksternRef herfra
                    EksternArbeidsforholdRef eksternRef = null; // arbeidsforholdInformasjon.finnEkstern(ov.getArbeidsgiver(), ov.getArbeidsforholdRef()); //
                                                                // NOSONAR
                    return new InntektsmeldingSomIkkeKommerDto(ov.getArbeidsgiver(), ov.getArbeidsforholdRef(), eksternRef);
                }) // NOSONAR
                .collect(Collectors.toList());
        }
    }

    public List<ArbeidsforholdOverstyringDto> getArbeidsforholdOverstyringer() {
        if (arbeidsforholdInformasjon == null) {
            return Collections.emptyList();
        }
        return arbeidsforholdInformasjon.getOverstyringer();
    }

    void setAktivt(boolean aktiv) {
        this.aktiv = aktiv;
    }

    /** Hvorvidt dette er det siste (aktive grunnlaget) for en behandling. */
    public boolean isAktiv() {
        return aktiv;
    }

    void setRegister(InntektArbeidYtelseAggregatDto registerFør) {
        this.register = registerFør;
    }

    public Optional<ArbeidsforholdInformasjonDto> getArbeidsforholdInformasjon() {
        return Optional.ofNullable(arbeidsforholdInformasjon);
    }

    void setInformasjon(ArbeidsforholdInformasjonDto informasjon) {
        this.arbeidsforholdInformasjon = informasjon;
    }

    void taHensynTilBetraktninger() {
        Optional.ofNullable(inntektsmeldinger).ifPresent(it -> it.taHensynTilBetraktninger(this.arbeidsforholdInformasjon));
    }

    void setArbeidsgiverOpplysninger(List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        this.arbeidsgiverOpplysninger = arbeidsgiverOpplysninger;
    }

    void leggTilArbeidsgiverOpplysninger(ArbeidsgiverOpplysningerDto arbeidsgiverOpplysningerDto) {
        arbeidsgiverOpplysninger.add(arbeidsgiverOpplysningerDto);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof InntektArbeidYtelseGrunnlagDto))
            return false;
        InntektArbeidYtelseGrunnlagDto that = (InntektArbeidYtelseGrunnlagDto) o;
        return aktiv == that.aktiv &&
            Objects.equals(register, that.register) &&
            Objects.equals(saksbehandlet, that.saksbehandlet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(register, saksbehandlet);
    }

    public void fjernSaksbehandlet() {
        saksbehandlet = null;
    }

    public List<ArbeidsgiverOpplysningerDto> getArbeidsgiverOpplysninger() {
        return arbeidsgiverOpplysninger;
    }
}
