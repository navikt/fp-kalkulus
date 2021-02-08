package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.BeregningsgrunnlagPrStatusOgAndelDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FaktaOmBeregningDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FinnÅrsinntektvisningstall;
import no.nav.folketrygdloven.kalkulator.guitjenester.inntektsgrunnlag.InntektsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.refusjon.VurderRefusjonDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag.YtelsespesifiktGrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.SammenligningsgrunnlagDto;

@ApplicationScoped
public class BeregningsgrunnlagDtoTjeneste {

    private static final int SEKS = 6;
    private BeregningsgrunnlagPrStatusOgAndelDtoTjeneste beregningsgrunnlagPrStatusOgAndelDtoTjeneste;
    private FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste;
    private Instance<YtelsespesifiktGrunnlagTjeneste> ytelsetjenester;

    BeregningsgrunnlagDtoTjeneste() {
        // Hibernate
    }

    @Inject
    public BeregningsgrunnlagDtoTjeneste(FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste,
                                         BeregningsgrunnlagPrStatusOgAndelDtoTjeneste beregningsgrunnlagPrStatusOgAndelDtoTjeneste,
                                         @Any Instance<YtelsespesifiktGrunnlagTjeneste> ytelsetjenester) {
        this.faktaOmBeregningDtoTjeneste = faktaOmBeregningDtoTjeneste;
        this.beregningsgrunnlagPrStatusOgAndelDtoTjeneste = beregningsgrunnlagPrStatusOgAndelDtoTjeneste;
        this.ytelsetjenester = ytelsetjenester;
    }

    public BeregningsgrunnlagDto lagBeregningsgrunnlagDto(BeregningsgrunnlagGUIInput input) {
        return lagDto(input);
    }

    private BeregningsgrunnlagDto lagDto(BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagDto dto = new BeregningsgrunnlagDto();

        mapFaktaOmBeregning(input, dto);

        if (input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag().isPresent()) {
            mapOverstyring(input, dto);
            mapSkjæringstidspunkt(input, dto);
            mapFaktaOmRefusjon(input, dto);
            mapFaktaOmFordeling(input, dto);
            mapSammenligningsgrunnlag(input, dto);
            mapSammenlingingsgrunnlagPrStatus(input, dto);
            mapBeregningsgrunnlagAktivitetStatus(input, dto);
            mapBeregningsgrunnlagPerioder(input, dto);
            mapBeløp(input, dto);
            mapAktivitetGradering(input, dto);
            mapDekningsgrad(input, dto);
            mapYtelsesspesifiktGrunnlag(input, dto);
            mapInntektsgrunnlag(input, dto);
        }
        return dto;
    }

    private void mapFaktaOmRefusjon(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        VurderRefusjonDtoTjeneste.lagDto(input).ifPresent(dto::setRefusjonTilVurdering);
    }

    private void mapInntektsgrunnlag(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        if (input.isEnabled("visning-inntektsgrunnlag", false)) {
            InntektsgrunnlagTjeneste.lagDto(input).ifPresent(dto::setInntektsgrunnlag);
        }
    }

    private void mapOverstyring(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        boolean overstyrt = input.getBeregningsgrunnlag().isOverstyrt();
        dto.setErOverstyrtInntekt(overstyrt);
    }

    private void mapYtelsesspesifiktGrunnlag(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        Optional<YtelsespesifiktGrunnlagTjeneste> ytelsemapper = FagsakYtelseTypeRef.Lookup.find(ytelsetjenester, input.getFagsakYtelseType());
        ytelsemapper.flatMap(ytelsespesifiktGrunnlagTjeneste -> ytelsespesifiktGrunnlagTjeneste.map(input)).ifPresent(dto::setYtelsesspesifiktGrunnlag);
    }

    private void mapDekningsgrad(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        int dekningsgrad = input.getYtelsespesifiktGrunnlag().getDekningsgrad();
        dto.setDekningsgrad(dekningsgrad);
    }

    private void mapFaktaOmFordeling(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        FaktaOmFordelingDtoTjeneste.lagDto(input).ifPresent(dto::setFaktaOmFordeling);
    }

    private void mapFaktaOmBeregning(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        faktaOmBeregningDtoTjeneste.lagDto(input).ifPresent(dto::setFaktaOmBeregning);
    }

    private void mapSammenligningsgrunnlag(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var sammenligningsgrunnlagDto = lagSammenligningsgrunnlagDto(beregningsgrunnlag);
        sammenligningsgrunnlagDto.ifPresent(dto::setSammenligningsgrunnlag);
    }

    private void mapSammenlingingsgrunnlagPrStatus(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
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

    private void mapBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        dto.setAktivitetStatus(lagAktivitetStatusListe(beregningsgrunnlag));
    }

    private void mapBeregningsgrunnlagPerioder(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlagPerioder = lagBeregningsgrunnlagPeriodeRestDto(input);
        dto.setBeregningsgrunnlagPeriode(beregningsgrunnlagPerioder);
    }

    private void mapSkjæringstidspunkt(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var skjæringstidspunktForBeregning = input.getSkjæringstidspunktForBeregning();
        dto.setSkjaeringstidspunktBeregning(skjæringstidspunktForBeregning);
        dto.setSkjæringstidspunkt(skjæringstidspunktForBeregning);
    }

    private void mapBeløp(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        Beløp grunnbeløp = Optional.ofNullable(beregningsgrunnlag.getGrunnbeløp()).orElse(Beløp.ZERO);
        double halvG = grunnbeløp.getVerdi().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP).doubleValue();
        dto.setHalvG(halvG);
        dto.setGrunnbeløp(grunnbeløp.getVerdi());
        dto.setHjemmel(beregningsgrunnlag.getHjemmel());

        // Det skal vises et tall som "oppsumering" av årsinntekt i GUI, innebærer nok logikk til at det bør utledes backend
        FinnÅrsinntektvisningstall.finn(beregningsgrunnlag,
                input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat().flatMap(FaktaAggregatDto::getFaktaAktør)).ifPresent(dto::setÅrsinntektVisningstall);
    }

    private void mapAktivitetGradering(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var aktivitetGradering = input.getAktivitetGradering();
        var andelerMedGraderingUtenBG = GraderingUtenBeregningsgrunnlagTjeneste.finnAndelerMedGraderingUtenBG(beregningsgrunnlag,
            aktivitetGradering);

        if (!andelerMedGraderingUtenBG.isEmpty()) {
            dto.setAndelerMedGraderingUtenBG(beregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input, andelerMedGraderingUtenBG));
        }
    }

    private Optional<SammenligningsgrunnlagDto> lagSammenligningsgrunnlagDto(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        if (beregningsgrunnlag.getSammenligningsgrunnlag() == null) {
            return Optional.empty();
        }
        no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto sammenligningsgrunnlag = beregningsgrunnlag.getSammenligningsgrunnlag();
        SammenligningsgrunnlagDto dto = new SammenligningsgrunnlagDto();
        dto.setSammenligningsgrunnlagFom(sammenligningsgrunnlag.getSammenligningsperiodeFom());
        dto.setSammenligningsgrunnlagTom(sammenligningsgrunnlag.getSammenligningsperiodeTom());
        dto.setRapportertPrAar(sammenligningsgrunnlag.getRapportertPrÅr());
        dto.setAvvikPromille(sammenligningsgrunnlag.getAvvikPromilleNy());
        BigDecimal avvikProsent = sammenligningsgrunnlag.getAvvikPromilleNy() == null ? null : sammenligningsgrunnlag.getAvvikPromilleNy().scaleByPowerOfTen(-1);
        dto.setAvvikProsent(avvikProsent);
        return Optional.of(dto);
    }

    private List<SammenligningsgrunnlagDto> lagSammenligningsgrunnlagDtoPrStatus(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
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

    private BigDecimal finnDifferanseBeregnetGammeltSammenliningsgrunnlag(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag, BigDecimal rapportertPrÅr){
        BigDecimal beregnet;
        if(finnesAndelMedSN(beregningsgrunnlag)){
            beregnet = hentBeregnetSelvstendigNæringsdrivende(beregningsgrunnlag);
        } else {
            beregnet = hentBeregnetSamletArbeidstakerOgFrilanser(beregningsgrunnlag);
        }
        return beregnet.subtract(rapportertPrÅr);
    }

    private BigDecimal finnDifferanseBeregnet(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag,
                                              SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus){
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

    private BigDecimal hentBeregnetArbeidstaker(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(b -> b.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal hentBeregnetFrilanser(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(b -> b.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal hentBeregnetSelvstendigNæringsdrivende(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(b -> b.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getPgiSnitt)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal hentBeregnetSamletArbeidstakerOgFrilanser(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(b -> b.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER) || b.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private boolean finnesAndelMedSN(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag){
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .anyMatch(b -> b.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
    }

    private List<BeregningsgrunnlagPeriodeDto> lagBeregningsgrunnlagPeriodeRestDto(BeregningsgrunnlagGUIInput input) {
        List<BeregningsgrunnlagPeriodeDto> dtoList = new ArrayList<>();
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode : beregningsgrunnlagPerioder) {
            BeregningsgrunnlagPeriodeDto dto = lagBeregningsgrunnlagPeriode(input, periode);
            dtoList.add(dto);
        }
        return dtoList;
    }

    private BeregningsgrunnlagPeriodeDto lagBeregningsgrunnlagPeriode(BeregningsgrunnlagGUIInput input,
                                                                      no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode) {
        BeregningsgrunnlagPeriodeDto dto = new BeregningsgrunnlagPeriodeDto();
        dto.setBeregningsgrunnlagPeriodeFom(periode.getBeregningsgrunnlagPeriodeFom());
        dto.setBeregningsgrunnlagPeriodeTom(periode.getBeregningsgrunnlagPeriodeTom() == TIDENES_ENDE ? null : periode.getBeregningsgrunnlagPeriodeTom());
        dto.setBeregnetPrAar(periode.getBeregnetPrÅr());
        dto.setBruttoPrAar(periode.getBruttoPrÅr());
        BigDecimal bruttoInkludertBortfaltNaturalytelsePrAar = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoInkludertNaturalYtelser)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
        dto.setBruttoInkludertBortfaltNaturalytelsePrAar(bruttoInkludertBortfaltNaturalytelsePrAar);
        dto.setAvkortetPrAar(periode.getAvkortetPrÅr() == null ? null : finnAvkortetUtenGraderingPrÅr(bruttoInkludertBortfaltNaturalytelsePrAar, input.getBeregningsgrunnlag().getGrunnbeløp()));
        dto.setRedusertPrAar(periode.getRedusertPrÅr());
        dto.setDagsats(periode.getDagsats());
        dto.leggTilPeriodeAarsaker(periode.getPeriodeÅrsaker());
        dto.setAndeler(
                beregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input, periode.getBeregningsgrunnlagPrStatusOgAndelList()));
        return dto;
    }

    private BigDecimal finnAvkortetUtenGraderingPrÅr(BigDecimal bruttoInkludertBortfaltNaturalytelsePrAar, Beløp grunnbeløp) {
        if (bruttoInkludertBortfaltNaturalytelsePrAar == null) {
            return null;
        }
        BigDecimal seksG = grunnbeløp.multipliser(SEKS).getVerdi();
        return bruttoInkludertBortfaltNaturalytelsePrAar.compareTo(seksG) > 0 ? seksG : bruttoInkludertBortfaltNaturalytelsePrAar;
    }

    private List<no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus> lagAktivitetStatusListe(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        ArrayList<no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus> statusListe = new ArrayList<>();
        for (var status : beregningsgrunnlag.getAktivitetStatuser()) {
            statusListe.add(status.getAktivitetStatus());
        }
        return statusListe;
    }

}
