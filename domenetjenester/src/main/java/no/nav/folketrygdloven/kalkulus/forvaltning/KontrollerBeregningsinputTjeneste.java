package no.nav.folketrygdloven.kalkulus.forvaltning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.ForeslåBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulus.beregning.input.Resultat;
import no.nav.folketrygdloven.kalkulus.beregning.input.StegProsessInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class KontrollerBeregningsinputTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private StegProsessInputTjeneste stegProsessInputTjeneste;
    private ForeslåBeregningsgrunnlagFRISINN foreslåBeregningsgrunnlag;

    public KontrollerBeregningsinputTjeneste() {
        // CDI
    }

    @Inject
    public KontrollerBeregningsinputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                             StegProsessInputTjeneste stegProsessInputTjeneste,
                                             @FagsakYtelseTypeRef("FRISINN") ForeslåBeregningsgrunnlagFRISINN foreslåBeregningsgrunnlag) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.stegProsessInputTjeneste = stegProsessInputTjeneste;
        this.foreslåBeregningsgrunnlag = foreslåBeregningsgrunnlag;
    }

    public Optional<DiffResultatDto> kontrollerInputForKobling(KoblingEntitet kobling) {
        Resultat<StegProsesseringInput> res = stegProsessInputTjeneste.lagFortsettInput(Set.of(kobling.getId()),
                BeregningSteg.FORS_BERGRUNN); // Bruker dette steget fordi det er her inntekt fastsettes
        ForeslåBeregningsgrunnlagInput input = (ForeslåBeregningsgrunnlagInput) res.getResultatPrKobling().get(kobling.getId());
        BeregningsgrunnlagGrunnlagEntitet sisteGrunnlagFraKofak = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(kobling.getId(), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER).orElseThrow();
        BeregningsgrunnlagGrunnlagDto bgDto = BehandlingslagerTilKalkulusMapper.mapGrunnlag(sisteGrunnlagFraKofak);
        BeregningsgrunnlagInput beregningsgrunnlagInput = input.medBeregningsgrunnlagGrunnlag(bgDto);
        StegProsesseringInput stegProsesseringInput = new StegProsesseringInput(beregningsgrunnlagInput, BeregningsgrunnlagTilstand.FORESLÅTT);
        ForeslåBeregningsgrunnlagInput foreslåBeregningsgrunnlagInput = new ForeslåBeregningsgrunnlagInput(stegProsesseringInput)
                .medGrunnbeløpsatser(input.getGrunnbeløpsatser());
        foreslåBeregningsgrunnlagInput.leggTilToggle("feilretting-tsf-1715", true);
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(foreslåBeregningsgrunnlagInput);
        BeregningsgrunnlagDto reberegnetGrunnlag = beregningsgrunnlagRegelResultat.getBeregningsgrunnlag();
        BeregningsgrunnlagGrunnlagEntitet gjeldendeBGGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(kobling.getId(), BeregningsgrunnlagTilstand.FORESLÅTT).orElseThrow();
        BeregningsgrunnlagDto gjeldendeGrunnlag = BehandlingslagerTilKalkulusMapper.mapGrunnlag(gjeldendeBGGrunnlag).getBeregningsgrunnlag().orElseThrow();

        if (gjeldendeGrunnlag.getBeregningsgrunnlagPerioder().isEmpty() || reberegnetGrunnlag.getBeregningsgrunnlagPerioder().isEmpty()) {
            return Optional.empty();
        }
        FrisinnGrunnlag ytelsespesifiktGrunnlag = (FrisinnGrunnlag) input.getYtelsespesifiktGrunnlag();
        List<AktivitetStatus> søkteStatuser = new ArrayList<>();
        if (ytelsespesifiktGrunnlag.getFrisinnPerioder().stream().anyMatch(FrisinnPeriode::getSøkerFrilans)) {
            søkteStatuser.add(AktivitetStatus.FRILANSER);
        }
        if (ytelsespesifiktGrunnlag.getFrisinnPerioder().stream().anyMatch(FrisinnPeriode::getSøkerNæring)) {
            søkteStatuser.add(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        }

        return lagNyDto(kobling.getSaksnummer().getVerdi(),
                søkteStatuser,
                reberegnetGrunnlag.getBeregningsgrunnlagPerioder().get(0),
                gjeldendeGrunnlag.getBeregningsgrunnlagPerioder().get(0));
    }

    private Optional<DiffResultatDto> lagNyDto(String saksnummer,
                                               List<AktivitetStatus> søkteStatuser,
                                               BeregningsgrunnlagPeriodeDto reberegnetGrunnlag,
                                               BeregningsgrunnlagPeriodeDto gjeldendeGrunnlag) {
        DiffResultatDto dto = new DiffResultatDto();
        dto.setSaksnummer(saksnummer);
        dto.setReberegnetInntektSøktOm(finnInntektForSøktStatus(søkteStatuser, reberegnetGrunnlag));
        dto.setGjeldendeInntektSøktOm(finnInntektForSøktStatus(søkteStatuser, gjeldendeGrunnlag));
        dto.setReberegnetInntektIkkeSøktOm(finnInntektForIkkeSøktStatus(søkteStatuser, reberegnetGrunnlag));
        dto.setGjeldendeInntektIkkeSøktOm(finnInntektForIkkeSøktStatus(søkteStatuser, gjeldendeGrunnlag));
        dto.setStatuserDetErSøktOm(søkteStatuser);
        dto.setHarDiff(reberegnetGrunnlag.getBruttoPrÅr().compareTo(gjeldendeGrunnlag.getBruttoPrÅr()) != 0);
        return Optional.of(dto);
    }

    private BigDecimal finnInntektForSøktStatus(List<AktivitetStatus> søkteStatuser, BeregningsgrunnlagPeriodeDto reberegnetGrunnlag) {
        return reberegnetGrunnlag.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> søkteStatuser.contains(andel.getAktivitetStatus()))
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal finnInntektForIkkeSøktStatus(List<AktivitetStatus> søkteStatuser, BeregningsgrunnlagPeriodeDto reberegnetGrunnlag) {
        return reberegnetGrunnlag.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> !søkteStatuser.contains(andel.getAktivitetStatus()))
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private Map<AktivitetStatus, BigDecimal> lagBruttoPrStatusMap(BeregningsgrunnlagDto grunnlag) {
        Map<AktivitetStatus, BigDecimal> map = new HashMap<>();
        grunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
                .forEach(andel -> {
                    BigDecimal foreløpigBrutto = map.getOrDefault(andel.getAktivitetStatus(), BigDecimal.ZERO);
                    BigDecimal nyBrutto = foreløpigBrutto.add(andel.getBruttoPrÅr());
                    map.put(andel.getAktivitetStatus(), nyBrutto);
                });
        return map;
    }
}
