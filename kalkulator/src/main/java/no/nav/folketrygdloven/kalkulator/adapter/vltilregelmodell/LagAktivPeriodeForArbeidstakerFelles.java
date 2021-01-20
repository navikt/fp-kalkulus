package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.util.Collection;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;

public class LagAktivPeriodeForArbeidstakerFelles {

    public static AktivPeriode lagAktivPeriodeForArbeidstaker(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                 Periode gjeldendePeriode,
                                                                 String opptjeningArbeidsgiverAktørId,
                                                                 String opptjeningArbeidsgiverOrgnummer,
                                                                 InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                 Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> alleAktiviteter) {
        if (opptjeningArbeidsgiverAktørId != null) {
            return lagAktivePerioderForArbeidstakerHosPrivatperson(opptjeningArbeidsgiverAktørId, gjeldendePeriode);
        } else if (opptjeningArbeidsgiverOrgnummer != null) {
            return lagAktivePerioderForArbeidstakerHosVirksomhet(inntektsmeldinger, gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef, alleAktiviteter);
        } else {
            throw new IllegalStateException("Må ha en arbeidsgiver som enten er aktør eller virksomhet når aktivitet er " + Aktivitet.ARBEIDSTAKERINNTEKT);
        }
    }

    private static AktivPeriode lagAktivePerioderForArbeidstakerHosPrivatperson(String aktørId, Periode gjeldendePeriode) {
        return AktivPeriode.forArbeidstakerHosPrivatperson(gjeldendePeriode, aktørId);
    }

    private static AktivPeriode lagAktivePerioderForArbeidstakerHosVirksomhet(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                              Periode gjeldendePeriode,
                                                                              String opptjeningArbeidsgiverOrgnummer,
                                                                              InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                              Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> alleAktiviteter) {
        if (harInntektsmeldingForArbeidsforhold(inntektsmeldinger, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef)) {
            return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef.getReferanse());
        } else {
            return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, null);
        }
    }

    private static boolean harInntektsmeldingForArbeidsforhold(Collection<InntektsmeldingDto> inntektsmeldinger,
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
