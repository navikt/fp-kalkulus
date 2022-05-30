package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class MapBGStatuserFraVLTilRegel {

    private MapBGStatuserFraVLTilRegel() {
        // Skjul
    }

    public static AktivitetStatusModell map(Collection<InntektsmeldingDto> inntektsmeldinger, BeregningAktivitetAggregatDto beregningAktivitetAggregat, YtelseFilterDto ytelseFilter) {
        AktivitetStatusModell regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForOpptjening(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening());
        leggTilAktiviteter(inntektsmeldinger, beregningAktivitetAggregat, regelmodell, ytelseFilter);
        return regelmodell;
    }

    private static void leggTilAktiviteter(Collection<InntektsmeldingDto> inntektsmeldinger,
                                           BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                           AktivitetStatusModell modell, YtelseFilterDto ytelseFilter) {
        List<BeregningAktivitetDto> relevanteAktiviteter = beregningAktivitetAggregat.getBeregningAktiviteter();
        relevanteAktiviteter.forEach(a -> modell.leggTilEllerOppdaterAktivPeriode(lagAktivPerioder(inntektsmeldinger, a, ytelseFilter)));
    }

    private static AktivPeriode lagAktivPerioder(Collection<InntektsmeldingDto> inntektsmeldinger, BeregningAktivitetDto ba, YtelseFilterDto ytelseFilter) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(ba.getOpptjeningAktivitetType());
        Intervall periode = ba.getPeriode();
        Periode regelPeriode = Periode.of(periode.getFomDato(), periode.getTomDato());
        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forFrilanser(regelPeriode);
        }
        if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            return lagAktivPeriodeForArbeidstaker(inntektsmeldinger, ba, aktivitetType, regelPeriode);
        }
        return AktivPeriode.forAndre(aktivitetType, regelPeriode);
    }

    private static AktivPeriode lagAktivPeriodeForArbeidstaker(Collection<InntektsmeldingDto> inntektsmeldinger, BeregningAktivitetDto beregningAktivitet,
                                                               Aktivitet aktivitetType,
                                                               Periode gjeldendePeriode) {
        if (beregningAktivitet.getArbeidsgiver().erAktørId()) {
            return lagAktivePerioderForArbeidstakerHosPrivatperson(beregningAktivitet, gjeldendePeriode);
        } else if (beregningAktivitet.getArbeidsgiver().getErVirksomhet()) {
            return lagAktivePerioderForArbeidstakerHosVirksomhet(inntektsmeldinger, beregningAktivitet, aktivitetType, gjeldendePeriode);
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet.");
    }

    private static AktivPeriode lagAktivePerioderForArbeidstakerHosPrivatperson(BeregningAktivitetDto beregningAktivitet, Periode gjeldendePeriode) {
        // Da vi ikke kan motta inntektsmeldinger ønsker vi ikke å sette arbeidsforholdId på arbeidsforholdet
        String aktørId = beregningAktivitet.getArbeidsgiver().getAktørId().getId();
        if (aktørId == null) {
            throw new IllegalArgumentException("Kan ikke lage periode for arbeidsforhold med arbeidsgiver som privatperson om aktørId er null");
        }
        return AktivPeriode.forArbeidstakerHosPrivatperson(gjeldendePeriode, aktørId);
    }

    private static AktivPeriode lagAktivePerioderForArbeidstakerHosVirksomhet(Collection<InntektsmeldingDto> inntektsmeldinger, BeregningAktivitetDto beregningAktivitet,
                                                                              Aktivitet aktivitetType,
                                                                              Periode gjeldendePeriode) {
        String orgnr = mapTilRegelmodellForOrgnr(aktivitetType, beregningAktivitet);
        String arbeidsforholdRef = beregningAktivitet.getArbeidsforholdRef().getReferanse();
        boolean harInntektsmelding = harInntektsmelding(inntektsmeldinger, orgnr, beregningAktivitet.getArbeidsforholdRef());
        return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, orgnr, arbeidsforholdRef, harInntektsmelding);
    }

    private static String mapTilRegelmodellForOrgnr(Aktivitet aktivitetType, BeregningAktivitetDto beregningAktivitet) {
        return Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType) ? beregningAktivitet.getArbeidsgiver().getOrgnr() : null;
    }

    private static boolean harInntektsmelding(Collection<InntektsmeldingDto> inntektsmeldinger,
                                              String orgnummer, InternArbeidsforholdRefDto ref) {
        return inntektsmeldinger.stream()
                .anyMatch(im -> Objects.equals(im.getArbeidsgiver().getOrgnr(), orgnummer) &&
                        im.getArbeidsforholdRef().gjelderFor(ref));
    }

}
