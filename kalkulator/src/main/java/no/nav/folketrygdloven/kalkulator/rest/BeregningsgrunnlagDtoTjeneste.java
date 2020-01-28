package no.nav.folketrygdloven.kalkulator.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.rest.fakta.BeregningsgrunnlagPrStatusOgAndelDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.rest.fakta.FaktaOmBeregningDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.rest.fakta.FinnÅrsinntektvisningstall;

@ApplicationScoped
public class BeregningsgrunnlagDtoTjeneste {

    private static final int SEKS = 6;
    private FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste;

    private final Map<FagsakYtelseType, BiConsumer<BeregningsgrunnlagRestInput, BeregningsgrunnlagDto>> ytelsespesifikkMapper = Map
        .of(FagsakYtelseType.FORELDREPENGER, this::mapDtoForeldrepenger);

    BeregningsgrunnlagDtoTjeneste() {
        // Hibernate
    }

    @Inject
    public BeregningsgrunnlagDtoTjeneste(FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste) {
        this.faktaOmBeregningDtoTjeneste = faktaOmBeregningDtoTjeneste;
    }

    public BeregningsgrunnlagDto lagBeregningsgrunnlagDto(BeregningsgrunnlagRestInput input) {
        return lagDto(input);
    }

    private BeregningsgrunnlagDto lagDto(BeregningsgrunnlagRestInput input) {
        BeregningsgrunnlagDto dto = new BeregningsgrunnlagDto();

        mapSkjæringstidspunkt(input, dto);

        mapFaktaOmBeregning(input, dto);
        mapFaktaOmFordeling(input, dto);
        mapSammenligningsgrunnlag(input, dto);
        mapSammenlingingsgrunnlagPrStatus(input, dto);

        mapBeregningsgrunnlagAktivitetStatus(input, dto);
        mapBeregningsgrunnlagPerioder(input, dto);
        mapBeløp(input, dto);

        mapAktivitetGradering(input, dto);

        mapDtoYtelsespesifikk(input, dto);

        mapDekningsgrad(input, dto);

        return dto;
    }

    private void mapDekningsgrad(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        int dekningsgrad = input.getYtelsespesifiktGrunnlag().getDekningsgrad();
        dto.setDekningsgrad(dekningsgrad);
    }

    private void mapFaktaOmFordeling(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        FaktaOmFordelingDtoTjeneste.lagDto(input).ifPresent(dto::setFaktaOmFordeling);
    }

    private void mapFaktaOmBeregning(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        faktaOmBeregningDtoTjeneste.lagDto(input).ifPresent(dto::setFaktaOmBeregning);
    }

    private void mapSammenligningsgrunnlag(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var sammenligningsgrunnlagDto = lagSammenligningsgrunnlagDto(beregningsgrunnlag);
        sammenligningsgrunnlagDto.ifPresent(dto::setSammenligningsgrunnlag);
    }

    private void mapSammenlingingsgrunnlagPrStatus(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<SammenligningsgrunnlagDto> sammenligningsgrunnlagDto = lagSammenligningsgrunnlagDtoPrStatus(beregningsgrunnlag);
        if(sammenligningsgrunnlagDto.isEmpty() && dto.getSammenligningsgrunnlag() != null){
            dto.getSammenligningsgrunnlag().setSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
            dto.getSammenligningsgrunnlag().setDifferanseBeregnet(finnDifferanseBeregnetGammeltSammenliningsgrunnlag(beregningsgrunnlag, dto.getSammenligningsgrunnlag().getRapportertPrAar()));
            dto.setSammenligningsgrunnlagPrStatus(List.of(dto.getSammenligningsgrunnlag()));
        } else {
            dto.setSammenligningsgrunnlagPrStatus(sammenligningsgrunnlagDto);
        }
    }

    private void mapBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        dto.setAktivitetStatus(lagAktivitetStatusListe(beregningsgrunnlag));
    }

    private void mapBeregningsgrunnlagPerioder(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlagPerioder = lagBeregningsgrunnlagPeriodeRestDto(input);
        dto.setBeregningsgrunnlagPeriode(beregningsgrunnlagPerioder);
    }

    private void mapSkjæringstidspunkt(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        var skjæringstidspunktForBeregning = input.getSkjæringstidspunktForBeregning();
        dto.setSkjaeringstidspunktBeregning(skjæringstidspunktForBeregning);
        dto.setSkjæringstidspunkt(skjæringstidspunktForBeregning);
        dto.setLedetekstBrutto("Brutto beregningsgrunnlag");
    }

    private void mapBeløp(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        Beløp grunnbeløp = Optional.ofNullable(beregningsgrunnlag.getGrunnbeløp()).orElse(Beløp.ZERO);
        long seksG = Math.round(grunnbeløp.getVerdi().multiply(BigDecimal.valueOf(6)).doubleValue());
        double halvG = grunnbeløp.getVerdi().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP).doubleValue();
        dto.setHalvG(halvG);
        dto.setGrunnbeløp(grunnbeløp.getVerdi());
        dto.setHjemmel(beregningsgrunnlag.getHjemmel());
        dto.setLedetekstAvkortet("Avkortet beregningsgrunnlag (6G=" + seksG + ")");

        // Det skal vises et tall som "oppsumering" av årsinntekt i GUI, innebærer nok logikk til at det bør utledes backend
        FinnÅrsinntektvisningstall.finn(beregningsgrunnlag).ifPresent(dto::setÅrsinntektVisningstall);
    }

    private void mapAktivitetGradering(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var aktivitetGradering = input.getAktivitetGradering();
        var andelerMedGraderingUtenBG = GraderingUtenBeregningsgrunnlagTjeneste.finnAndelerMedGraderingUtenBG(beregningsgrunnlag,
            aktivitetGradering);

        if (!andelerMedGraderingUtenBG.isEmpty()) {
            dto.setAndelerMedGraderingUtenBG(BeregningsgrunnlagPrStatusOgAndelDtoTjeneste
                .lagBeregningsgrunnlagPrStatusOgAndelDto(input, andelerMedGraderingUtenBG));
        }
    }

    private void mapDtoYtelsespesifikk(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        this.ytelsespesifikkMapper.getOrDefault(input.getFagsakYtelseType(), (in, dt) -> {
        }).accept(input, dto);
    }

    private void mapDtoForeldrepenger(BeregningsgrunnlagRestInput input, BeregningsgrunnlagDto dto) {
        int dekningsgrad = input.getYtelsespesifiktGrunnlag().getDekningsgrad();
        dto.setLedetekstRedusert("Redusert beregningsgrunnlag (" + dekningsgrad + "%)");
    }

    private Optional<SammenligningsgrunnlagDto> lagSammenligningsgrunnlagDto(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        if (beregningsgrunnlag.getSammenligningsgrunnlag() == null) {
            return Optional.empty();
        }
        SammenligningsgrunnlagRestDto sammenligningsgrunnlag = beregningsgrunnlag.getSammenligningsgrunnlag();
        SammenligningsgrunnlagDto dto = new SammenligningsgrunnlagDto();
        dto.setSammenligningsgrunnlagFom(sammenligningsgrunnlag.getSammenligningsperiodeFom());
        dto.setSammenligningsgrunnlagTom(sammenligningsgrunnlag.getSammenligningsperiodeTom());
        dto.setRapportertPrAar(sammenligningsgrunnlag.getRapportertPrÅr());
        dto.setAvvikPromille(sammenligningsgrunnlag.getAvvikPromilleNy());
        BigDecimal avvikProsent = sammenligningsgrunnlag.getAvvikPromilleNy() == null ? null : sammenligningsgrunnlag.getAvvikPromilleNy().scaleByPowerOfTen(-1);
        dto.setAvvikProsent(avvikProsent);
        return Optional.of(dto);
    }

    private List<SammenligningsgrunnlagDto> lagSammenligningsgrunnlagDtoPrStatus(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        List<SammenligningsgrunnlagDto> sammenligningsgrunnlagDtos = new ArrayList<>();
        beregningsgrunnlag.getSammenligningsgrunnlagPrStatusListe().forEach(s -> {
            SammenligningsgrunnlagDto dto = new SammenligningsgrunnlagDto();
            dto.setSammenligningsgrunnlagFom(s.getSammenligningsperiodeFom());
            dto.setSammenligningsgrunnlagTom(s.getSammenligningsperiodeTom());
            dto.setRapportertPrAar(s.getRapportertPrÅr());
            dto.setAvvikPromille(s.getAvvikPromilleNy());
            BigDecimal avvikProsent = s.getAvvikPromilleNy() == null ? null : s.getAvvikPromilleNy().scaleByPowerOfTen(-1);
            dto.setAvvikProsent(avvikProsent);
            dto.setSammenligningsgrunnlagType(s.getSammenligningsgrunnlagType());
            dto.setDifferanseBeregnet(finnDifferanseBeregnet(beregningsgrunnlag, s));
            sammenligningsgrunnlagDtos.add(dto);
        });
        return sammenligningsgrunnlagDtos;

    }

    private BigDecimal finnDifferanseBeregnetGammeltSammenliningsgrunnlag(BeregningsgrunnlagRestDto beregningsgrunnlag, BigDecimal rapportertPrÅr){
        BigDecimal beregnet;
        if(finnesAndelMedSN(beregningsgrunnlag)){
            beregnet = hentBeregnetSelvstendigNæringsdrivende(beregningsgrunnlag);
        } else {
            beregnet = hentBeregnetSamletArbeidstakerOgFrilanser(beregningsgrunnlag);
        }
        return beregnet.subtract(rapportertPrÅr);
    }

    private BigDecimal finnDifferanseBeregnet(BeregningsgrunnlagRestDto beregningsgrunnlag, SammenligningsgrunnlagPrStatusRestDto sammenligningsgrunnlagPrStatus){
        BigDecimal beregnet;
        if(SammenligningsgrunnlagType.SAMMENLIGNING_AT.equals(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType())){
            beregnet = hentBeregnetArbeidstaker(beregningsgrunnlag);
        } else if(SammenligningsgrunnlagType.SAMMENLIGNING_FL.equals(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType())){
            beregnet = hentBeregnetFrilanser(beregningsgrunnlag);
        } else if(SammenligningsgrunnlagType.SAMMENLIGNING_SN.equals(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType())){
            beregnet = hentBeregnetSelvstendigNæringsdrivende(beregningsgrunnlag);
        } else {
            return finnDifferanseBeregnetGammeltSammenliningsgrunnlag(beregningsgrunnlag, sammenligningsgrunnlagPrStatus.getRapportertPrÅr());
        }
        return beregnet.subtract(sammenligningsgrunnlagPrStatus.getRapportertPrÅr());
    }

    private BigDecimal hentBeregnetArbeidstaker(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(b -> b.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
            .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getBeregnetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal hentBeregnetFrilanser(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(b -> b.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getBeregnetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal hentBeregnetSelvstendigNæringsdrivende(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(b -> b.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
            .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getPgiSnitt)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal hentBeregnetSamletArbeidstakerOgFrilanser(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(b -> b.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER) || b.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getBeregnetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private boolean finnesAndelMedSN(BeregningsgrunnlagRestDto beregningsgrunnlag){
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .anyMatch(b -> b.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
    }

    private List<BeregningsgrunnlagPeriodeDto> lagBeregningsgrunnlagPeriodeRestDto(BeregningsgrunnlagRestInput input) {
        List<BeregningsgrunnlagPeriodeDto> dtoList = new ArrayList<>();
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<BeregningsgrunnlagPeriodeRestDto> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriodeRestDto periode : beregningsgrunnlagPerioder) {
            BeregningsgrunnlagPeriodeDto dto = lagBeregningsgrunnlagPeriode(input, periode);
            dtoList.add(dto);
        }
        return dtoList;
    }

    private BeregningsgrunnlagPeriodeDto lagBeregningsgrunnlagPeriode(BeregningsgrunnlagRestInput input,
                                                                      BeregningsgrunnlagPeriodeRestDto periode) {
        BeregningsgrunnlagPeriodeDto dto = new BeregningsgrunnlagPeriodeDto();
        dto.setBeregningsgrunnlagPeriodeFom(periode.getBeregningsgrunnlagPeriodeFom());
        dto.setBeregningsgrunnlagPeriodeTom(periode.getBeregningsgrunnlagPeriodeTom());
        dto.setBeregnetPrAar(periode.getBeregnetPrÅr());
        dto.setBruttoPrAar(periode.getBruttoPrÅr());
        BigDecimal bruttoInkludertBortfaltNaturalytelsePrAar = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getBruttoInkludertNaturalYtelser)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
        dto.setBruttoInkludertBortfaltNaturalytelsePrAar(bruttoInkludertBortfaltNaturalytelsePrAar);
        dto.setAvkortetPrAar(finnAvkortetUtenGraderingPrÅr(bruttoInkludertBortfaltNaturalytelsePrAar, input.getBeregningsgrunnlag().getGrunnbeløp()));
        dto.setRedusertPrAar(periode.getRedusertPrÅr());
        dto.setDagsats(periode.getDagsats());
        dto.leggTilPeriodeAarsaker(periode.getPeriodeÅrsaker());
        dto.setAndeler(
            BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input, periode.getBeregningsgrunnlagPrStatusOgAndelList()));
        return dto;
    }

    private BigDecimal finnAvkortetUtenGraderingPrÅr(BigDecimal bruttoInkludertBortfaltNaturalytelsePrAar, Beløp grunnbeløp) {
        if (bruttoInkludertBortfaltNaturalytelsePrAar == null) {
            return null;
        }
        BigDecimal seksG = grunnbeløp.multipliser(SEKS).getVerdi();
        return bruttoInkludertBortfaltNaturalytelsePrAar.compareTo(seksG) > 0 ? seksG : bruttoInkludertBortfaltNaturalytelsePrAar;
    }

    private List<AktivitetStatus> lagAktivitetStatusListe(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        ArrayList<AktivitetStatus> statusListe = new ArrayList<>();
        for (BeregningsgrunnlagAktivitetStatusRestDto status : beregningsgrunnlag.getAktivitetStatuser()) {
            statusListe.add(status.getAktivitetStatus());
        }
        return statusListe;
    }

}
