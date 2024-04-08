package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper.mapAvklaringsbehov;
import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper.mapGrunnlag;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapTilGUIInputFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@Dependent
public class GUIBeregningsgrunnlagInputTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    @Inject
    public GUIBeregningsgrunnlagInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    /**
     * Mapper alle databaseentiteter til GUI-input (for å lage beregningsgrunnlagdto til gui)
     * <p>
     * Ingen databasekall skal gjøres her inne for å unngå at databasekall gjøres i loop.
     *
     * @param beregningsgrunnlagGrunnlagEntitet Aktivt grunnlag for kobling
     * @param avklaringsbehov            Avklaringsbehov for kobling
     * @param kalkulatorInput              KalkulatorInput for kobling
     * @param kobling                           Kobling
     * @param originaltGrunnlag          Originalt grunnlag hvis det finnes
     * @return Liste med restinput
     */
    private static BeregningsgrunnlagGUIInput mapInputListe(KoblingEntitet kobling,
                                                                       BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntitet,
                                                                       List<AvklaringsbehovEntitet> avklaringsbehov,
                                                                       KalkulatorInputDto kalkulatorInput,
                                                                       Optional<BeregningsgrunnlagGrunnlagEntitet> originaltGrunnlagEntitet) {
        BeregningsgrunnlagGUIInput input = lagInput(kobling, kalkulatorInput);
        var originaltGrunnlag = mapOriginaleGrunnlag(originaltGrunnlagEntitet);
        var oppdatertInput = input.medBeregningsgrunnlagGrunnlag(mapGrunnlag(beregningsgrunnlagGrunnlagEntitet))
            .medAvklaringsbehov(mapAvklaringsbehov(avklaringsbehov));
        if (originaltGrunnlag.isPresent()) {
            return oppdatertInput.medBeregningsgrunnlagGrunnlagFraForrigeBehandling(originaltGrunnlag.get());
        } else return oppdatertInput;
    }

    private static Optional<BeregningsgrunnlagGrunnlagDto> mapOriginaleGrunnlag(Optional<BeregningsgrunnlagGrunnlagEntitet> originaltGrunnlag) {
        return originaltGrunnlag.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag);
    }

    private static BeregningsgrunnlagGUIInput lagInput(KoblingEntitet koblingEntitet, KalkulatorInputDto kalkulatorInput) {
        return MapTilGUIInputFraKalkulator.mapFraKalkulatorInput(koblingEntitet, kalkulatorInput);
    }

    /**
     * Returnerer BeregningsgrunnlagInput for alle angitte koblinger (hvis eksisterer).
     *
     * @return
     */
    public Optional<BeregningsgrunnlagGUIInput> lagInputForKoblinger(KoblingEntitet kobling,
                                                           Optional<KoblingEntitet> originalKobling,
                                                           KalkulatorInputDto input) {
        var beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        if (beregningsgrunnlagGrunnlagEntitet.isEmpty()) {
            return Optional.empty();
        }
        var originaleGrunnlag = originalKobling.flatMap(ok -> beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(ok.getId()));
        return Optional.of(mapKalkulatorInputTilModell(kobling, beregningsgrunnlagGrunnlagEntitet.get(), originaleGrunnlag, input));

    }

    private BeregningsgrunnlagGUIInput mapKalkulatorInputTilModell(KoblingEntitet kobling,
                                                                              BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntiteter,
                                                                              Optional<BeregningsgrunnlagGrunnlagEntitet> originaleGrunnlag,
                                                                              KalkulatorInputDto koblingKalkulatorInput) {
        List<AvklaringsbehovEntitet> avklaringsbehovPrKobling = avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKobling(kobling.getId());
        return mapInputListe(kobling,
            beregningsgrunnlagGrunnlagEntiteter,
            avklaringsbehovPrKobling,
            koblingKalkulatorInput,
            originaleGrunnlag);
    }

}
