package no.nav.folketrygdloven.kalkulus.håndtering.faktafordeling;


import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapFordelBeregningsgrunnlagDto;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.guitjenester.FinnArbeidsprosenter;
import no.nav.folketrygdloven.kalkulator.guitjenester.FinnArbeidsprosenterFP;
import no.nav.folketrygdloven.kalkulator.guitjenester.FinnArbeidsprosenterUtbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FaktaOmFordelingHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FaktaOmFordelingHåndteringDto.class, adapter = BeregningHåndterer.class)
public class FordelBeregningsgrunnlagHåndterer implements BeregningHåndterer<FaktaOmFordelingHåndteringDto> {

    @Override
    public HåndteringResultat håndter(FaktaOmFordelingHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.avklaringsbehov.FordelBeregningsgrunnlagHåndterer.håndter(mapFordelBeregningsgrunnlagDto(dto.getFordelBeregningsgrunnlagDto()), beregningsgrunnlagInput);

        validerAtBruttoErSatt(nyttGrunnlag, beregningsgrunnlagInput);

        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.standard().utled(nyttGrunnlag, grunnlagFraSteg, beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand(), dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

    private static void validerAtBruttoErSatt(BeregningsgrunnlagGrunnlagDto nyttGrunnlag, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        FinnArbeidsprosenter finnArbeidsprosenter;
        if (beregningsgrunnlagInput.getYtelsespesifiktGrunnlag() instanceof UtbetalingsgradGrunnlag) {
            finnArbeidsprosenter = new FinnArbeidsprosenterUtbetalingsgrad();
        } else {
            finnArbeidsprosenter = new FinnArbeidsprosenterFP();
        }
        var andelerUtenGrunnlag = nyttGrunnlag.getBeregningsgrunnlag().orElseThrow()
                .getBeregningsgrunnlagPerioder()
                .stream()
                .filter(p -> harAndelMedFravær(beregningsgrunnlagInput, finnArbeidsprosenter, p))
                .collect(
                        Collectors.toMap(BeregningsgrunnlagPeriodeDto::getPeriode,
                                p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getBruttoPrÅr() == null).toList()));

        var entriesUtenGrunnlag = andelerUtenGrunnlag.entrySet().stream().filter(e -> !e.getValue().isEmpty()).toList();
        if (!entriesUtenGrunnlag.isEmpty()) {
            throw new IllegalStateException("Fant andeler uten grunnlag i perioder: " + entriesUtenGrunnlag);
        }
    }

    private static boolean harAndelMedFravær(HåndterBeregningsgrunnlagInput beregningsgrunnlagInput, FinnArbeidsprosenter finnArbeidsprosenter, BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(a -> harFraværIPerioden(beregningsgrunnlagInput, finnArbeidsprosenter, p, a));
    }

    private static boolean harFraværIPerioden(HåndterBeregningsgrunnlagInput beregningsgrunnlagInput, FinnArbeidsprosenter finnArbeidsprosenter, BeregningsgrunnlagPeriodeDto p, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return finnArbeidsprosenter.finnArbeidsprosenterIPeriode(a, beregningsgrunnlagInput.getYtelsespesifiktGrunnlag(), p.getPeriode()).stream().anyMatch(
                it -> it.compareTo(BigDecimal.valueOf(100)) != 0
        );
    }

}
