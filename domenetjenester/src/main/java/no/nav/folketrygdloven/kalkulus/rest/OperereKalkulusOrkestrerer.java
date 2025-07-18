package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegTilstand;
import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegUtTilstand;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.MDC;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulus.beregning.BeregningStegTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.HåndteringInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.StegProsessInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndtererApplikasjonTjeneste;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.response.v1.KalkulusRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;

@ApplicationScoped
public class OperereKalkulusOrkestrerer {

    private BeregningStegTjeneste beregningStegTjeneste;
    private StegProsessInputTjeneste stegInputTjeneste;
    private HåndteringInputTjeneste håndteringInputTjeneste;
    private HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste;
    private RullTilbakeTjeneste rullTilbakeTjeneste;

    public OperereKalkulusOrkestrerer() {
        // for CDI
    }

    @Inject
    public OperereKalkulusOrkestrerer(BeregningStegTjeneste beregningStegTjeneste,
                                      StegProsessInputTjeneste stegInputTjeneste,
                                      HåndteringInputTjeneste håndteringInputTjeneste,
                                      HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste,
                                      RullTilbakeTjeneste rullTilbakeTjeneste) {
        this.beregningStegTjeneste = beregningStegTjeneste;
        this.stegInputTjeneste = stegInputTjeneste;
        this.håndteringInputTjeneste = håndteringInputTjeneste;
        this.håndtererApplikasjonTjeneste = håndtererApplikasjonTjeneste;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
    }

    public KalkulusRespons beregn(BeregningSteg steg, KoblingEntitet koblingEntitet, KalkulatorInputDto input) {
        var stegInput = lagInputOgRullTilbakeVedBehov(koblingEntitet.getId(), input, new InputForSteg(steg), true);

        // Operer
        return opererAlle(stegInput, new Beregner(steg));
    }

    public KalkulusRespons håndter(KoblingEntitet koblingEntitet,
                                   KalkulatorInputDto inputDto,
                                   List<HåndterBeregningDto> håndterBeregningDtoList) {
        var håndterInputPrKobling = (HåndterBeregningsgrunnlagInput) lagInputOgRullTilbakeVedBehov(koblingEntitet.getId(), inputDto, new InputForHåndtering(håndterBeregningDtoList),
            false);
        // Operer
        return opererAlle(håndterInputPrKobling, new Håndterer(håndterBeregningDtoList));
    }

    private KalkulusRespons opererAlle(BeregningsgrunnlagInput input, Opererer opererer) {
        return opererer.utfør(input);
    }

    private BeregningsgrunnlagInput lagInputOgRullTilbakeVedBehov(Long koblingId,
                                                                  KalkulatorInputDto kalkulatorInputDto,
                                                                  LagInputTjeneste lagInputTjeneste,
                                                                  boolean skalKjøreSteget) {
        rullTilbakeTjeneste.rullTilbakeTilForrigeTilstandVedBehov(koblingId, lagInputTjeneste.getTilstand(), skalKjøreSteget);
        return lagInput(koblingId, lagInputTjeneste, kalkulatorInputDto);
    }

    private BeregningsgrunnlagInput lagInput(Long koblingId, LagInputTjeneste lagInputTjeneste, KalkulatorInputDto kalkulatorInput) {
        return lagInputTjeneste.utfør(koblingId, kalkulatorInput);
    }

    private interface LagInputTjeneste {
        BeregningsgrunnlagInput utfør(Long koblingId, KalkulatorInputDto inputDto);

        BeregningsgrunnlagTilstand getTilstand();
    }

    private class InputForSteg implements LagInputTjeneste {

        private final BeregningSteg steg;

        private InputForSteg(BeregningSteg steg) {
            this.steg = steg;
        }

        @Override
        public BeregningsgrunnlagInput utfør(Long koblingId, KalkulatorInputDto inputPrKobling) {
            return stegInputTjeneste.lagBeregningsgrunnlagInput(koblingId, inputPrKobling, steg);
        }

        @Override
        public BeregningsgrunnlagTilstand getTilstand() {
            return mapTilStegTilstand(steg);
        }
    }

    private class InputForHåndtering implements LagInputTjeneste {

        private final BeregningsgrunnlagTilstand tilstand;

        private InputForHåndtering(List<HåndterBeregningDto> håndterBeregningDto) {
            this.tilstand = finnTilstandFraDto(håndterBeregningDto);
        }

        @Override
        public BeregningsgrunnlagInput utfør(Long koblingId, KalkulatorInputDto inputDto) {
            return håndteringInputTjeneste.lagBeregningsgrunnlagInput(koblingId, inputDto, tilstand);
        }

        @Override
        public BeregningsgrunnlagTilstand getTilstand() {
            return tilstand;
        }


        private BeregningsgrunnlagTilstand finnTilstandFraDto(List<HåndterBeregningDto> håndterBeregningDto) {
            var avklaringsbehov = håndterBeregningDto.getFirst();
            return mapTilStegUtTilstand(avklaringsbehov.getAvklaringsbehovDefinisjon().getStegFunnet()).orElseThrow();
        }
    }


    private interface Opererer {
        KalkulusRespons utfør(BeregningsgrunnlagInput beregningsgrunnlagInput);
    }

    private class Håndterer implements Opererer {

        private final List<HåndterBeregningDto> håndteringDtoMap;

        public Håndterer(List<HåndterBeregningDto> håndteringDtoMap) {
            this.håndteringDtoMap = håndteringDtoMap;
        }

        @Override
        public KalkulusRespons utfør(BeregningsgrunnlagInput beregningsgrunnlagInput) {
            MDC.put("prosess_koblingId", beregningsgrunnlagInput.getKoblingId().toString());
            var response = håndtererApplikasjonTjeneste.håndter((HåndterBeregningsgrunnlagInput) beregningsgrunnlagInput,
                håndteringDtoMap.getFirst());
            MDC.remove("prosess_koblingId");
            return response;
        }
    }

    private class Beregner implements Opererer {

        private final BeregningSteg beregningSteg;

        public Beregner(BeregningSteg beregningSteg) {
            this.beregningSteg = beregningSteg;
        }

        @Override
        public KalkulusRespons utfør(BeregningsgrunnlagInput beregningsgrunnlagInput) {
            KalkulusRespons response;
            MDC.put("prosess_koblingId", beregningsgrunnlagInput.getKoblingId().toString());
            response = beregningStegTjeneste.beregnFor(beregningSteg, (StegProsesseringInput) beregningsgrunnlagInput);
            MDC.remove("prosess_koblingId");
            return response;
        }
    }


}
