package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class MapBeregningAktiviteterFraVLTilRegel {

    public static final String INGEN_AKTIVITET_MELDING = "Må ha aktiviteter for å sette status.";

    public AktivitetStatusModell mapForSkjæringstidspunkt(BeregningsgrunnlagInput input) {
        LocalDate opptjeningSkjæringstidspunkt = input.getSkjæringstidspunktOpptjening();

        AktivitetStatusModell modell = new AktivitetStatusModell();
        modell.setSkjæringstidspunktForOpptjening(opptjeningSkjæringstidspunkt);

        var relevanteAktiviteter = input.getOpptjeningAktiviteterForBeregning();

        if (relevanteAktiviteter.isEmpty()) { // For enklere feilsøking når det mangler aktiviteter
            throw new IllegalStateException(INGEN_AKTIVITET_MELDING);
        } else {
            relevanteAktiviteter.forEach(opptjeningsperiode -> modell.leggTilEllerOppdaterAktivPeriode(lagAktivPeriode(input.getInntektsmeldinger(), opptjeningsperiode)));
        }
        return modell;
    }

    protected AktivPeriode lagAktivPeriode(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(opptjeningsperiode.getOpptjeningAktivitetType());
        Periode gjeldendePeriode = opptjeningsperiode.getPeriode();

        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forFrilanser(gjeldendePeriode);
        } else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            var opptjeningArbeidsgiverAktørId = opptjeningsperiode.getArbeidsgiverAktørId();
            var opptjeningArbeidsgiverOrgnummer = opptjeningsperiode.getArbeidsgiverOrgNummer();
            var opptjeningArbeidsforhold = Optional.ofNullable(opptjeningsperiode.getArbeidsforholdId()).orElse(InternArbeidsforholdRefDto.nullRef());
            return lagAktivPeriodeForArbeidstaker(inntektsmeldinger, gjeldendePeriode, opptjeningArbeidsgiverAktørId,
                opptjeningArbeidsgiverOrgnummer, opptjeningArbeidsforhold);
        } else {
            return AktivPeriode.forAndre(aktivitetType, gjeldendePeriode);
        }
    }

    protected static AktivPeriode lagAktivPeriodeForArbeidstaker(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                               Periode gjeldendePeriode,
                                                               String opptjeningArbeidsgiverAktørId,
                                                               String opptjeningArbeidsgiverOrgnummer,
                                                               InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (opptjeningArbeidsgiverAktørId != null) {
            return lagAktivePerioderForArbeidstakerHosPrivatperson(opptjeningArbeidsgiverAktørId, gjeldendePeriode);
        } else if (opptjeningArbeidsgiverOrgnummer != null) {
            return lagAktivePerioderForArbeidstakerHosVirksomhet(inntektsmeldinger, gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef);
        } else {
            throw new IllegalStateException("Må ha en arbeidsgiver som enten er aktør eller virksomhet når aktivitet er " + Aktivitet.ARBEIDSTAKERINNTEKT);
        }
    }

    protected static AktivPeriode lagAktivePerioderForArbeidstakerHosPrivatperson(String aktørId, Periode gjeldendePeriode) {
        return AktivPeriode.forArbeidstakerHosPrivatperson(gjeldendePeriode, aktørId);
    }

    protected static AktivPeriode lagAktivePerioderForArbeidstakerHosVirksomhet(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                              Periode gjeldendePeriode,
                                                                              String opptjeningArbeidsgiverOrgnummer,
                                                                              InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (harInntektsmeldingForArbeidsforhold(inntektsmeldinger, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef)) {
            return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef.getReferanse());
        } else {
            return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, null);
        }
    }

    protected static boolean harInntektsmeldingForArbeidsforhold(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                               String orgnummer,
                                                               InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (!arbeidsforholdRef.gjelderForSpesifiktArbeidsforhold()) {
            return false;
        } else {
            return inntektsmeldinger.stream()
                .anyMatch(im -> im.gjelderForEtSpesifiktArbeidsforhold()
                    && Objects.equals(im.getArbeidsgiver().getOrgnr(), orgnummer)
                    && Objects.equals(im.getArbeidsforholdRef().getReferanse(), arbeidsforholdRef.getReferanse()));
        }
    }
}
