package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FastsettGrunnlagOmsorgspenger;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.OmsorgspengeGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class YtelsespesifiktGrunnlagTjenesteOMP implements YtelsespesifiktGrunnlagTjeneste{

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagRestInput input){
        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag();
        var omsorgspengeGrunnlagDto = new OmsorgspengeGrunnlagDto();
        var førsteBeregningsperiode = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);

        if (harForeslåttBeregning(beregningsgrunnlag) && erBrukerKunArbeidstaker(input)) {
            omsorgspengeGrunnlagDto.setSkalAvviksvurdere(FastsettGrunnlagOmsorgspenger.girDirekteUtbetalingTilBruker(input, førsteBeregningsperiode));
        }

        return Optional.of(omsorgspengeGrunnlagDto);
    }

    private boolean harForeslåttBeregning(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto){
        return !beregningsgrunnlagGrunnlagDto.getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private static boolean erBrukerKunArbeidstaker(BeregningsgrunnlagRestInput input){
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .allMatch(a -> AktivitetStatus.ARBEIDSTAKER.equals(a.getAktivitetStatus()));
    }
}
