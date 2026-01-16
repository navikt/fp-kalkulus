package no.nav.folketrygdloven.kalkulus.domene.håndtering.faktafordeling;


import static no.nav.folketrygdloven.kalkulus.domene.håndtering.mapping.OppdatererDtoMapper.mapFordelBeregningsgrunnlagDto;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagGuiTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.KalkulatorGuiInterface;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.UtledEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fordeling.FaktaOmFordelingHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FaktaOmFordelingHåndteringDto.class, adapter = BeregningHåndterer.class)
public class FordelBeregningsgrunnlagHåndterer implements BeregningHåndterer<FaktaOmFordelingHåndteringDto> {

    private static final KalkulatorGuiInterface GUI_TJENESTE = new BeregningsgrunnlagGuiTjeneste();

    @Override
    public HåndteringResultat håndter(FaktaOmFordelingHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.avklaringsbehov.FordelBeregningsgrunnlagHåndterer.håndter(mapFordelBeregningsgrunnlagDto(dto.getFordelBeregningsgrunnlagDto()), beregningsgrunnlagInput);

        validerAtBruttoErSatt(nyttGrunnlag, beregningsgrunnlagInput);

        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.standard().utled(nyttGrunnlag, grunnlagFraSteg, beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand(), dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

    private static void validerAtBruttoErSatt(BeregningsgrunnlagGrunnlagDto nyttGrunnlag, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        var andelerUtenGrunnlag = nyttGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow()
                .getBeregningsgrunnlagPerioder()
                .stream()
                .filter(p -> harAndelMedFravær(beregningsgrunnlagInput, p))
                .collect(
                        Collectors.toMap(BeregningsgrunnlagPeriodeDto::getPeriode,
                                p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getBruttoPrÅr() == null).toList()));

        var entriesUtenGrunnlag = andelerUtenGrunnlag.entrySet().stream().filter(e -> !e.getValue().isEmpty()).toList();
        if (!entriesUtenGrunnlag.isEmpty()) {
            throw new IllegalStateException("Fant andeler uten grunnlag i perioder: " + entriesUtenGrunnlag);
        }
    }

    private static boolean harAndelMedFravær(HåndterBeregningsgrunnlagInput beregningsgrunnlagInput, BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(a -> harFraværIPerioden(beregningsgrunnlagInput, p, a));
    }

    private static boolean harFraværIPerioden(HåndterBeregningsgrunnlagInput beregningsgrunnlagInput, BeregningsgrunnlagPeriodeDto p, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return GUI_TJENESTE.finnArbeidsprosenterIPeriode(a, beregningsgrunnlagInput.getYtelsespesifiktGrunnlag(), p.getPeriode()).stream().anyMatch(
                it -> it.compareTo(BigDecimal.valueOf(100)) != 0
        );
    }

}
