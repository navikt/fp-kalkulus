package no.nav.folketrygdloven.kalkulator.ytelse.fp;


import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.beregningsgrunnlag.BevegeligeHelligdagerUtil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FORELDREPENGER)
public class FastsettBeregningsperiodeTjenesteFP implements FastsettBeregningsperiode {

    private final BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();

    public FastsettBeregningsperiodeTjenesteFP() {
    }

    public BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(a -> a.getAktivitetStatus().erArbeidstaker() || a.getAktivitetStatus().erFrilanser())
                    .forEach(a -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(a).medBeregningsperiode(finnBeregningsperiode(a, inntektsmeldinger, beregningsgrunnlag.getSkjæringstidspunkt())));
        });
        return nyttBeregningsgrunnlag;
    }

    private Intervall finnBeregningsperiode(BeregningsgrunnlagPrStatusOgAndelDto andel, Collection<InntektsmeldingDto> inntektsmeldinger, LocalDate skjæringstidspunkt) {
        var harIM = inntektsmeldinger.stream().anyMatch(im -> andel.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()));
        var beregningsperiodeUjustert = beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);
        if (harIM) {
            return beregningsperiodeUjustert;
        }
        LocalDate inntektsrapporteringsfrist = beregningsperiodeUjustert.getTomDato().plusDays(5);
        LocalDate fristMedHelligdagerInkl = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(inntektsrapporteringsfrist);
        LocalDate dagensDato = LocalDate.now();
        if (dagensDato.isAfter(fristMedHelligdagerInkl)) {
            return beregningsperiodeUjustert;
        }
        // Flytter beregningsperioden en måned tilbake så vi kan få med alle rapporterte inntekter.
        var justertFom = beregningsperiodeUjustert.getFomDato().minusMonths(1).withDayOfMonth(1);
        var justertTom = beregningsperiodeUjustert.getTomDato().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return Intervall.fraOgMedTilOgMed(justertFom, justertTom);
    }
}
