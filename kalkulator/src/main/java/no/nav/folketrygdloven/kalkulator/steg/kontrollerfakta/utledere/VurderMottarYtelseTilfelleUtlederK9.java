package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ArbeidstakerUtenInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;


@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
@FagsakYtelseTypeRef("PSB")
@FaktaOmBeregningTilfelleRef("VURDER_MOTTAR_YTELSE")
public class VurderMottarYtelseTilfelleUtlederK9 implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return skalVurdereMottattYtelse(beregningsgrunnlag, input.getIayGrunnlag()) ?
            Optional.of(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE) : Optional.empty();
    }

    public static boolean skalVurdereMottattYtelse(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        boolean erFrilanser = erFrilanser(beregningsgrunnlag);

        InntektFilterDto filter = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister());
        filter = filter.filterSammenligningsgrunnlag();

        if (erFrilanser) {
            return mottarYtelseIBeregningsperiode(beregningsgrunnlag, filter, AktivitetStatus.FRILANSER);
        }
        var arbeidstakerSomManglerInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
                .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, iayGrunnlag);
        if (!arbeidstakerSomManglerInntektsmelding.isEmpty()) {
            return mottarYtelseIBeregningsperiode(beregningsgrunnlag, filter, AktivitetStatus.ARBEIDSTAKER);
        }
        return false;
    }

    private static boolean mottarYtelseIBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag, InntektFilterDto filter, AktivitetStatus aktivitetsStatus) {
        Intervall beregningsPeriodeForStatus = finnBeregningsperiodeForAktivitetStatus(beregningsgrunnlag, aktivitetsStatus);
        return filter.getFiltrertInntektsposter().stream().anyMatch(inntektspostDto -> {
            boolean overlapperYtelseMedBeregningsgrunnlaget = beregningsPeriodeForStatus.overlapper(inntektspostDto.getPeriode());
            return InntektspostType.YTELSE.equals(inntektspostDto.getInntektspostType()) && overlapperYtelseMedBeregningsgrunnlaget;
        });
    }

    public static boolean erFrilanser(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser());
    }

    public static Intervall finnBeregningsperiodeForAktivitetStatus(BeregningsgrunnlagDto beregningsgrunnlag, AktivitetStatus aktivitetsStatus) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> aktivitetsStatus.equals(andel.getAktivitetStatus())).map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregningsperiode).findFirst().orElseThrow(() -> new IllegalStateException("Fant ingen beregningsperiode for " + aktivitetsStatus.name()));
    }
}
