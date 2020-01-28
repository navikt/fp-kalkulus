package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapGraderingForYrkesaktivitet.mapGraderingForYrkesaktivitet;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

/** Default mapper for alle ytelser. */
@FagsakYtelseTypeRef()
@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel {

    public MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering() {
        // For CDI
    }

    @Override
    protected void mapInntektsmelding(Collection<InntektsmeldingDto>inntektsmeldinger,
                                      Collection<AndelGradering> andelGraderinger,
                                      Map<Arbeidsgiver, LocalDate> førsteIMMap,
                                      YrkesaktivitetDto ya,
                                      LocalDate startdatoPermisjon,
                                      ArbeidsforholdOgInntektsmelding.Builder builder,
                                      Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer) {
        Optional<InntektsmeldingDto> matchendeInntektsmelding = inntektsmeldinger.stream()
            .filter(im -> ya.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
            .findFirst();
        matchendeInntektsmelding.ifPresent(im ->
            builder.medRefusjonskrav(MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(im, startdatoPermisjon))
        );
        Optional<LocalDate> førsteMuligeRefusjonsdato = mapFørsteGyldigeDatoForRefusjon(ya, refusjonOverstyringer);
        førsteMuligeRefusjonsdato.ifPresent(builder::medOverstyrtRefusjonsFrist);
        builder.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(førsteIMMap.get(ya.getArbeidsgiver()));
    }

    @Override
    protected List<Gradering> mapGradering(Collection<AndelGradering> andelGraderinger, YrkesaktivitetDto ya) {
        List<Gradering> graderinger = mapGraderingForYrkesaktivitet(andelGraderinger, ya);
        return graderinger;
    }

    private Optional<LocalDate> mapFørsteGyldigeDatoForRefusjon(YrkesaktivitetDto ya, Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer) {
        return refusjonOverstyringer.stream().flatMap(s -> s.getRefusjonOverstyringer().stream())
            .filter(o -> o.getArbeidsgiver().equals(ya.getArbeidsgiver()))
            .map(BeregningRefusjonOverstyringDto::getFørsteMuligeRefusjonFom)
            .findFirst();
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
