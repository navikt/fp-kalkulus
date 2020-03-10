package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import static no.nav.folketrygdloven.kalkulator.ytelse.svp.AktivitetStatusMapper.mapAktivitetStatus;
import static no.nav.folketrygdloven.kalkulator.ytelse.svp.FinnAndelsnrForTilrettelegging.finnAndelsnrIFørstePeriode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterBekreftetPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapAndelGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapPeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

@FagsakYtelseTypeRef("SVP")
@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspenger
        extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering {


    public MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspenger() {
        // For CDI
    }

    @Override
    protected Optional<LocalDate> utledStartdatoPermisjon(BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning, Collection<InntektsmeldingDto> inntektsmeldinger, YrkesaktivitetDto ya, Periode ansettelsesPeriode, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        Optional<LocalDate> førsteSøktePermisjonsdag = finnFørsteSøktePermisjonsdag(input, ya, ansettelsesPeriode);
        return førsteSøktePermisjonsdag.map(dato -> skjæringstidspunktBeregning.isAfter(dato) ? skjæringstidspunktBeregning : dato);
    }

    @Override
    protected Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, YrkesaktivitetDto ya, Periode ansettelsesPeriode) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
                .getSkjæringstidspunkt();
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterBekreftetPermisjon.finn(input.getIayGrunnlag(), ya, ansettelsesPeriode, skjæringstidspunktBeregning);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = input.getYtelsespesifiktGrunnlag();
        List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad = svangerskapspengerGrunnlag.getUtbetalingsgradPrAktivitet();
        Optional<LocalDate> førsteDatoMedUtbetalingOpt = tilretteleggingMedUtbelingsgrad.stream()
                .filter(trl -> trl.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().map(ag -> ag.getIdentifikator().equals(ya.getArbeidsgiver().getIdentifikator())).orElse(false)
                        && trl.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(ya.getArbeidsforholdRef()))
                .flatMap(trl -> trl.getPeriodeMedUtbetalingsgrad().stream())
                .filter(periodeMedUtbetalingsgradDto -> periodeMedUtbetalingsgradDto.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) != 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder());

        if (førsteDatoMedUtbetalingOpt.isEmpty()) {
            return Optional.empty();
        }

        LocalDate førsteDagEtterPermisjon = førstedagEtterBekreftetPermisjonOpt.get();
        LocalDate førsteDatoMedUtbetaling = førsteDatoMedUtbetalingOpt.get();
        return førsteDagEtterPermisjon.isAfter(førsteDatoMedUtbetaling) ? førstedagEtterBekreftetPermisjonOpt : førsteDatoMedUtbetalingOpt;
    }

    @Override
    protected PeriodeModell mapPeriodeModell(BeregningsgrunnlagInput input,
                                             BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                             YrkesaktivitetFilterDto filter,
                                             LocalDate skjæringstidspunkt,
                                             List<SplittetPeriode> eksisterendePerioder,
                                             List<ArbeidsforholdOgInntektsmelding> regelInntektsmeldinger,
                                             List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGradering> regelAndelGraderinger) {

        List<PeriodisertBruttoBeregningsgrunnlag> periodiseringBruttoBg = MapPeriodisertBruttoBeregningsgrunnlag.map(vlBeregningsgrunnlag);

        List<AndelGradering> endringerSøktYtelse = hentEndringSøktYtelseSVP(input, vlBeregningsgrunnlag);
        var regelEndringerSøktYtelse = endringerSøktYtelse.stream()
                .filter(g -> erAnsattIPerioden(input.getBehandlingReferanse(), g, filter))
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

    private boolean erAnsattIPerioden(BehandlingReferanse ref, AndelGradering g, YrkesaktivitetFilterDto filter) {
        if (g.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            // Antar SN er aktiv i hele uttaksperioden
            return true;
        }
        Optional<YrkesaktivitetDto> yrkesaktivitet = FinnYrkesaktiviteterForBeregningTjeneste.finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(ref, filter)
                .stream()
                .filter(ya -> ya.gjelderFor(g.getArbeidsgiver(), g.getArbeidsforholdRef()))
                .findFirst();
        if (yrkesaktivitet.isEmpty()) {
            return false;
        }
        Optional<Periode> ansettelsesPeriodeSomSlutterVedEllerEtterStp = FinnAnsettelsesPeriode.finnMinMaksPeriode(filter.getAnsettelsesPerioder(yrkesaktivitet.get()),
                ref.getSkjæringstidspunktBeregning());
        return ansettelsesPeriodeSomSlutterVedEllerEtterStp.isPresent();
    }

    private List<AndelGradering> hentEndringSøktYtelseSVP(BeregningsgrunnlagInput input, BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad = ((SvangerskapspengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getUtbetalingsgradPrAktivitet();
        return mapTilrettelegginger(tilretteleggingMedUtbelingsgrad, vlBeregningsgrunnlag);
    }

    private AndelGradering mapUttak(UtbetalingsgradPrAktivitetDto tilrettelegging, BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        var tilretteleggingArbeidsforhold = tilrettelegging.getUtbetalingsgradArbeidsforhold();
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
    List<AndelGradering> mapTilrettelegginger(List<UtbetalingsgradPrAktivitetDto> tilrettelegginger, BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        return tilrettelegginger.stream().map(a -> mapUttak(a, vlBeregningsgrunnlag)).collect(Collectors.toList());
    }

}
