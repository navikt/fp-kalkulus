package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import static no.nav.folketrygdloven.kalkulator.ytelse.svp.AktivitetStatusMapper.mapAktivitetStatus;
import static no.nav.folketrygdloven.kalkulator.ytelse.svp.FinnAndelsnrForTilrettelegging.finnAndelsnrIFørstePeriode;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapAndelGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapPeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingMedUtbelingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.kalkulator.regelmodell.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

@FagsakYtelseTypeRef("SVP")
@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspenger
        extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering {


    public MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspenger() {
        // For CDI
    }

    @Override
    protected PeriodeModell mapPeriodeModell(BeregningsgrunnlagInput input,
                                             BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                             YrkesaktivitetFilterDto filter,
                                             LocalDate skjæringstidspunkt,
                                             List<SplittetPeriode> eksisterendePerioder,
                                             List<ArbeidsforholdOgInntektsmelding> regelInntektsmeldinger,
                                             List<no.nav.folketrygdloven.kalkulator.regelmodell.AndelGradering> regelAndelGraderinger) {

        List<PeriodisertBruttoBeregningsgrunnlag> periodiseringBruttoBg = MapPeriodisertBruttoBeregningsgrunnlag.map(vlBeregningsgrunnlag);

        List<AndelGradering> endringerSøktYtelse = hentEndringSøktYtelseSVP(input, vlBeregningsgrunnlag);
        var regelEndringerSøktYtelse = endringerSøktYtelse.stream()
            .map(andelGradering -> MapAndelGradering.mapTilRegelAndelGradering(input.getBehandlingReferanse(),
                andelGradering, filter))
            .collect(Collectors.toList());

        return PeriodeModell.builder()
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medGrunnbeløp(vlBeregningsgrunnlag.getGrunnbeløp().getVerdi())
            .medInntektsmeldinger(regelInntektsmeldinger)
            .medAndelGraderinger(regelAndelGraderinger)
            .medEndringISøktYtelse(List.copyOf(regelEndringerSøktYtelse))
            .medEksisterendePerioder(eksisterendePerioder)
            .medPeriodisertBruttoBeregningsgrunnlag(periodiseringBruttoBg)
            .build();
    }

    private List<AndelGradering> hentEndringSøktYtelseSVP(BeregningsgrunnlagInput input, BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        List<TilretteleggingMedUtbelingsgradDto> tilretteleggingMedUtbelingsgrad = ((SvangerskapspengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getTilretteleggingMedUtbelingsgrad();
        return mapTilrettelegginger(tilretteleggingMedUtbelingsgrad, vlBeregningsgrunnlag);
    }

    private AndelGradering mapUttak(TilretteleggingMedUtbelingsgradDto tilrettelegging, BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        var tilretteleggingArbeidsforhold = tilrettelegging.getTilretteleggingArbeidsforhold();
        Arbeidsgiver tilretteleggingArbeidsgiver = tilretteleggingArbeidsforhold.getArbeidsgiver().orElse(null);
        InternArbeidsforholdRefDto tilretteleggingArbeidsforholdRef = tilretteleggingArbeidsforhold.getInternArbeidsforholdRef();
        AktivitetStatus tilretteleggingAktivitetStatus = mapAktivitetStatus(tilretteleggingArbeidsforhold.getUttakArbeidType());
        AndelGradering.Builder builder = AndelGradering.builder()
            .medStatus(tilretteleggingAktivitetStatus)
            .medArbeidsgiver(tilretteleggingArbeidsgiver)
            .medArbeidsforholdRef(tilretteleggingArbeidsforholdRef)
            .medAndelsnr(finnAndelsnrIFørstePeriode(vlBeregningsgrunnlag, tilretteleggingArbeidsforhold).orElse(null));

        tilrettelegging.getPeriodeMedUtbetalingsgrad().stream()
            .filter(p -> !p.getPeriode().getTomDato().isBefore(vlBeregningsgrunnlag.getSkjæringstidspunkt()))
            .forEach(periode -> builder.leggTilGradering(mapUttakPeriode(periode, vlBeregningsgrunnlag.getSkjæringstidspunkt())));
        return builder.build();
    }

    private AndelGradering.Gradering mapUttakPeriode(PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad, LocalDate skjæringstidspunkt) {
        Intervall periode;
        if (periodeMedUtbetalingsgrad.getPeriode().getFomDato().isBefore(skjæringstidspunkt)) {
            periode = Intervall.fraOgMedTilOgMed(skjæringstidspunkt, periodeMedUtbetalingsgrad.getPeriode().getTomDato());
        } else {
            periode = periodeMedUtbetalingsgrad.getPeriode();
        }
        return new AndelGradering.Gradering(periode, periodeMedUtbetalingsgrad.getUtbetalingsgrad());
    }

    // TODO: Denne bør vere private
    List<AndelGradering> mapTilrettelegginger(List<TilretteleggingMedUtbelingsgradDto> tilrettelegginger, BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        return tilrettelegginger.stream().map(a -> mapUttak(a, vlBeregningsgrunnlag)).collect(Collectors.toList());
    }

}
