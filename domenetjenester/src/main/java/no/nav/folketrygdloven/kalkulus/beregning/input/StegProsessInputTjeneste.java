package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class StegProsessInputTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private StegInputMapper stegInputMapper;
    private KoblingTjeneste koblingTjeneste;

    public StegProsessInputTjeneste() {
        // CDI
    }

    @Inject
    public StegProsessInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, KoblingTjeneste koblingTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.stegInputMapper = new StegInputMapper(beregningsgrunnlagRepository, koblingTjeneste);
        this.koblingTjeneste = koblingTjeneste;
    }

    public StegProsesseringInput lagBeregningsgrunnlagInput(Long koblingId,
                                                                       KalkulatorInputDto inputDto,
                                                                       BeregningSteg stegType) {
        var koblingEntitet = koblingTjeneste.hentKobling(koblingId);
        var originaltGrunnlag = finnOriginalgrunnlag(koblingEntitet);
        var grunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        return stegInputMapper.mapStegInput(koblingEntitet, inputDto, grunnlagEntitet, stegType, originaltGrunnlag);
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnOriginalgrunnlag(KoblingEntitet koblingEntitet) {
        return koblingEntitet.getOriginalKoblingReferanse()
            .map(k -> koblingTjeneste.hentKobling(k))
            .flatMap(k -> beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(k.getId()));
    }
}
