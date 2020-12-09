package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;

public class InntektArbeidYtelseGrunnlagDto {

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

        // NB! skal ikke lage ny versjon av oppgitt opptjening eller andre underlag! Lenker bare inn på ferskt grunnlag
        grunnlag.getOppgittOpptjening().ifPresent(this::setOppgittOpptjening);
        grunnlag.getRegisterVersjon().ifPresent(this::setRegister);
        grunnlag.getSaksbehandletVersjon().ifPresent(this::setSaksbehandlet);
        grunnlag.getInntektsmeldinger().ifPresent(this::setInntektsmeldinger);
        grunnlag.getArbeidsforholdInformasjon().ifPresent(this::setInformasjon);
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
    public Optional<AktørArbeidDto> getBekreftetAnnenOpptjening() {
        return getSaksbehandletVersjon().map(InntektArbeidYtelseAggregatDto::getAktørArbeid);
    }

    public Optional<AktørArbeidDto> getAktørArbeidFraRegister() {
        if (register != null) {
            return Optional.ofNullable(register.getAktørArbeid());
        }
        return Optional.empty();
    }

    public Optional<AktørYtelseDto> getAktørYtelseFraRegister() {
        if (register != null) {
            return Optional.ofNullable(register.getAktørYtelse());
        }
        return Optional.empty();
    }

    public Optional<AktørInntektDto> getAktørInntektFraRegister() {
        if (register != null) {
            return Optional.ofNullable(register.getAktørInntekt());
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
                .filter(ArbeidsforholdOverstyringDto::kreverIkkeInntektsmelding)
                .map(ov -> new InntektsmeldingSomIkkeKommerDto(ov.getArbeidsgiver(), ov.getArbeidsforholdRef())) // NOSONAR
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
        if (!(o instanceof InntektArbeidYtelseGrunnlagDto))
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
