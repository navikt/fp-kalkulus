package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapGraderingForYrkesaktivitet.mapGraderingForYrkesaktivitet;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.Konfigverdier;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel {

    public MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering() {
        // For CDI
    }

    @Override
    protected void mapInntektsmelding(BeregningsgrunnlagInput input,
                                      Collection<InntektsmeldingDto> inntektsmeldinger,
                                      Map<Arbeidsgiver, LocalDate> førsteIMMap,
                                      YrkesaktivitetDto ya,
                                      LocalDate startdatoPermisjon,
                                      ArbeidsforholdOgInntektsmelding.Builder builder,
                                      Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer) {
        Optional<InntektsmeldingDto> matchendeInntektsmelding = inntektsmeldinger.stream()
                .filter(ya::gjelderFor)
                .findFirst();
        List<Refusjonskrav> refusjoner = mapRefusjonskrav(input.getYtelsespesifiktGrunnlag(), ya, startdatoPermisjon, refusjonOverstyringer, matchendeInntektsmelding, input.getBeregningsgrunnlag());
        builder.medRefusjonskrav(refusjoner);


        LocalDate innsendingsdatoFørsteInntektsmeldingMedRefusjon = førsteIMMap.get(ya.getArbeidsgiver());
        if (innsendingsdatoFørsteInntektsmeldingMedRefusjon != null) {
            builder.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(innsendingsdatoFørsteInntektsmeldingMedRefusjon);
            mapFørsteGyldigeDatoForRefusjon(ya, input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()).ifPresent(builder::medOverstyrtRefusjonsFrist);
            mapRefusjonskravFrist().ifPresent(builder::medRefusjonskravFrist);
        }
    }

    protected List<Refusjonskrav> mapRefusjonskrav(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                   YrkesaktivitetDto ya,
                                                   LocalDate startdatoPermisjon,
                                                   Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer,
                                                   Optional<InntektsmeldingDto> matchendeInntektsmelding, BeregningsgrunnlagDto beregningsgrunnlag) {
        return matchendeInntektsmelding
                .map(im -> MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(
                        im,
                        startdatoPermisjon,
                        refusjonOverstyringer,
                        finnGyldigeRefusjonPerioder(startdatoPermisjon, ytelsespesifiktGrunnlag, ya, beregningsgrunnlag)))
                .orElse(Collections.emptyList());
    }


    /** Finner gyldige perioder for refusjon
     *
     * For foreldrepenger er alle perioder gyldige
     *
     * @param startdatoPermisjon Startdato permisjon
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param ya Yrkesaktivitet
     * @param beregningsgrunnlag
     * @return Gyldige perioder for refusjon
     */
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, YrkesaktivitetDto ya, BeregningsgrunnlagDto beregningsgrunnlag) {
        return List.of(Intervall.fraOgMed(startdatoPermisjon));
    }


    /**
     * Setter informasjon for vurdering av refusjonskravfrist
     *
     * Settes til defaultverdier med mindre metode overrides
     *
     */
    protected Optional<RefusjonskravFrist> mapRefusjonskravFrist() {
        return Optional.of(new RefusjonskravFrist(Konfigverdier.FRIST_MÅNEDER_ETTER_REFUSJON, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST));
    }

    protected Optional<LocalDate> mapFørsteGyldigeDatoForRefusjon(YrkesaktivitetDto ya, Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer) {
        return refusjonOverstyringer.stream().flatMap(s -> s.getRefusjonOverstyringer().stream())
                .filter(o -> o.getArbeidsgiver().equals(ya.getArbeidsgiver()))
                .map(BeregningRefusjonOverstyringDto::getFørsteMuligeRefusjonFom)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    protected List<Gradering> mapGradering(Collection<AndelGradering> andelGraderinger, YrkesaktivitetDto ya) {
        List<Gradering> graderinger = mapGraderingForYrkesaktivitet(andelGraderinger, ya);
        return graderinger;
    }

    @Override
    protected PeriodeModell mapPeriodeModell(BeregningsgrunnlagInput input,
                                             BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                             YrkesaktivitetFilterDto filter,
                                             LocalDate skjæringstidspunkt,
                                             List<SplittetPeriode> eksisterendePerioder,
                                             List<ArbeidsforholdOgInntektsmelding> regelInntektsmeldinger,
                                             List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering> regelAndelGraderinger) {
        List<PeriodisertBruttoBeregningsgrunnlag> periodiseringBruttoBg = MapPeriodisertBruttoBeregningsgrunnlag.map(vlBeregningsgrunnlag);

        return PeriodeModell.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medGrunnbeløp(vlBeregningsgrunnlag.getGrunnbeløp().getVerdi())
                .medInntektsmeldinger(regelInntektsmeldinger)
                .medAndelGraderinger(regelAndelGraderinger)
                .medEndringISøktYtelse(Collections.emptyList())
                .medEksisterendePerioder(eksisterendePerioder)
                .medPeriodisertBruttoBeregningsgrunnlag(periodiseringBruttoBg)
                .build();
    }
}
