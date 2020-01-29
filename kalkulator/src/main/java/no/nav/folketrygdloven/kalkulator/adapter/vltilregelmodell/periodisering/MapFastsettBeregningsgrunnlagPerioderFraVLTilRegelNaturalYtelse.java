package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel {

    public MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse() {
        // For CDI
    }

    @Override
    protected void mapInntektsmelding(Collection<InntektsmeldingDto>inntektsmeldinger, Collection<AndelGradering> andelGraderinger, Map<Arbeidsgiver, LocalDate> førsteIMMap, YrkesaktivitetDto ya, LocalDate startdatoPermisjon, ArbeidsforholdOgInntektsmelding.Builder builder, Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer) {
        Optional<InntektsmeldingDto> matchendeInntektsmelding = inntektsmeldinger.stream()
            .filter(im -> ya.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
            .findFirst();
        matchendeInntektsmelding.ifPresent(im -> builder.medNaturalytelser(MapNaturalytelser.mapNaturalytelser(im)));
    }

    @Override
    protected void precondition(BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder = vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        int antallPerioder = beregningsgrunnlagPerioder.size();
        if (antallPerioder != 1) {
            throw MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse.TjenesteFeil.FEILFACTORY.kanIkkeUtvideMedNyePerioder(antallPerioder).toException();
        }
    }

    @Override
    protected PeriodeModell mapPeriodeModell(BeregningsgrunnlagInput input,
                                             BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                             YrkesaktivitetFilterDto filter,
                                             LocalDate skjæringstidspunkt,
                                             List<SplittetPeriode> eksisterendePerioder,
                                             List<ArbeidsforholdOgInntektsmelding> regelInntektsmeldinger,
                                             List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGradering> regelAndelGraderinger) {
        return PeriodeModell.builder()
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medGrunnbeløp(vlBeregningsgrunnlag.getGrunnbeløp().getVerdi())
            .medInntektsmeldinger(regelInntektsmeldinger)
            .medEksisterendePerioder(eksisterendePerioder)
            .build();
    }

    @Override
    protected List<Gradering> mapGradering(Collection<AndelGradering> andelGraderinger, YrkesaktivitetDto ya) {
        // Gradering skal ikkje mappes for naturalytelse
        return Collections.emptyList();
    }

    private interface TjenesteFeil extends DeklarerteFeil {
        MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse.TjenesteFeil FEILFACTORY = FeilFactory.create(MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse.TjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-370605", feilmelding = "Kan bare utvide med nye perioder når det fra før finnes 1 periode, fant %s", logLevel = LogLevel.WARN)
        Feil kanIkkeUtvideMedNyePerioder(int antallPerioder);
    }
}
