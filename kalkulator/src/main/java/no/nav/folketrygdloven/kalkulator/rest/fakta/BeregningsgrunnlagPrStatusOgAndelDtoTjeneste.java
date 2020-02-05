package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static java.util.stream.Collectors.toList;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAktivitetAggregat;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAndel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.rest.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;

public class BeregningsgrunnlagPrStatusOgAndelDtoTjeneste {

    private static final int MND_I_ÅR = 12;

    private static final Map<SammenligningsgrunnlagType, AktivitetStatus> SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP;
    static {
        SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP = Map.of(
            SammenligningsgrunnlagType.SAMMENLIGNING_AT, AktivitetStatus.ARBEIDSTAKER,
            SammenligningsgrunnlagType.SAMMENLIGNING_FL, AktivitetStatus.FRILANSER,
            SammenligningsgrunnlagType.SAMMENLIGNING_SN, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private BeregningsgrunnlagPrStatusOgAndelDtoTjeneste() {
        // Skjul
    }

    public static List<BeregningsgrunnlagPrStatusOgAndelDto> lagBeregningsgrunnlagPrStatusOgAndelDto(BeregningsgrunnlagRestInput input,
                                                                                                     List<BeregningsgrunnlagPrStatusOgAndelRestDto> beregningsgrunnlagPrStatusOgAndelList) {

        List<BeregningsgrunnlagPrStatusOgAndelDto> usortertDtoList = new ArrayList<>();
        for (BeregningsgrunnlagPrStatusOgAndelRestDto andel : beregningsgrunnlagPrStatusOgAndelList) {
            BeregningsgrunnlagPrStatusOgAndelDto dto = lagDto(input, andel);
            usortertDtoList.add(dto);
        }
        // Følgende gjøres for å sortere arbeidsforholdene etter beregnet årsinntekt og deretter arbedsforholdId
        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidsarbeidstakerAndeler = usortertDtoList.stream().filter(dto -> dto.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)).collect(toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndreAndeler = usortertDtoList.stream().filter(dto -> !dto.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)).collect(toList());
        if (dtoKanSorteres(arbeidsarbeidstakerAndeler)) {
            arbeidsarbeidstakerAndeler.sort(comparatorEtterBeregnetOgArbeidsforholdId());
        }
        List<BeregningsgrunnlagPrStatusOgAndelDto> dtoList = new ArrayList<>(arbeidsarbeidstakerAndeler);
        dtoList.addAll(alleAndreAndeler);

        return dtoList;
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto lagDto(BeregningsgrunnlagRestInput input,
                                                               BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        var iayGrunnlag = input.getIayGrunnlag();
        var inntektsmeldinger = input.getInntektsmeldinger();
        var ref = input.getBehandlingReferanse();
        var beregningAktivitetAggregat = input.getBeregningsgrunnlagGrunnlag().getGjeldendeAktiviteter();
        BeregningsgrunnlagPrStatusOgAndelDto dto = LagTilpassetDtoTjeneste.opprettTilpassetDTO(ref, andel, iayGrunnlag);
        LocalDate skjæringstidspunktForBeregning = input.getSkjæringstidspunktForBeregning();
        Optional<InntektsmeldingDto> inntektsmelding = BeregningInntektsmeldingTjeneste.finnInntektsmeldingForAndel(mapAndel(andel), inntektsmeldinger);
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, inntektsmelding, iayGrunnlag).ifPresent(dto::setArbeidsforhold);
        dto.setDagsats(andel.getDagsats());
        dto.setOriginalDagsatsFraTilstøtendeYtelse(andel.getOrginalDagsatsFraTilstøtendeYtelse());
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setAktivitetStatus(andel.getAktivitetStatus());
        dto.setBeregningsperiodeFom(andel.getBeregningsperiodeFom());
        dto.setBeregningsperiodeTom(andel.getBeregningsperiodeTom());
        dto.setBruttoPrAar(andel.getBruttoPrÅr());
        dto.setFordeltPrAar(andel.getFordeltPrÅr());
        dto.setAvkortetPrAar(andel.getAvkortetPrÅr());
        dto.setRedusertPrAar(andel.getRedusertPrÅr());
        dto.setOverstyrtPrAar(andel.getOverstyrtPrÅr());
        dto.setBeregnetPrAar(andel.getBeregnetPrÅr());
        dto.setInntektskategori(andel.getInntektskategori());
        dto.setBesteberegningPrAar(andel.getBesteberegningPrÅr());
        dto.setFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler());
        dto.setErTidsbegrensetArbeidsforhold(andel.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforholdRestDto::getErTidsbegrensetArbeidsforhold).orElse(null));
        dto.setErNyIArbeidslivet(andel.getNyIArbeidslivet());
        dto.setLonnsendringIBeregningsperioden(andel.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforholdRestDto::erLønnsendringIBeregningsperioden).orElse(null));
        dto.setLagtTilAvSaksbehandler(andel.getLagtTilAvSaksbehandler());
        dto.setErTilkommetAndel(FordelBeregningsgrunnlagTjeneste.erNyttArbeidsforhold(mapAndel(andel), mapAktivitetAggregat(beregningAktivitetAggregat), skjæringstidspunktForBeregning));
        if(andel.getAktivitetStatus().erFrilanser() || andel.getAktivitetStatus().erArbeidstaker() || andel.getAktivitetStatus().erSelvstendigNæringsdrivende()){
            dto.setSkalFastsetteGrunnlag(skalGrunnlagFastsettes(input, andel));
        }
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            iayGrunnlag.getAktørInntektFraRegister(ref.getAktørId())
                .ifPresent(aktørInntekt -> {
                    var filter = new InntektFilterDto(aktørInntekt).før(skjæringstidspunktForBeregning);
                    BigDecimal årsbeløp = InntektForAndelTjeneste
                        .finnSnittinntektPrÅrForArbeidstakerIBeregningsperioden(filter, andel);
                    BigDecimal månedsbeløp = årsbeløp.divide(BigDecimal.valueOf(MND_I_ÅR), 10, RoundingMode.HALF_EVEN);
                    dto.setBelopPrMndEtterAOrdningen(månedsbeløp);
                    dto.setBelopPrAarEtterAOrdningen(årsbeløp);
                });
        } else if (andel.getAktivitetStatus().erFrilanser()) {
            InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(ref.getAktørId(),
                    iayGrunnlag, andel, skjæringstidspunktForBeregning)
                .ifPresent(dto::setBelopPrMndEtterAOrdningen);
        }
        return dto;
    }

    private static boolean skalGrunnlagFastsettes(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPrStatusOgAndelRestDto andel){
        if(finnesIngenSammenligningsgrunnlagPrStatus(input)){
            return skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(input, andel, input.getBeregningsgrunnlag().getSammenligningsgrunnlag());
        }

        Optional<SammenligningsgrunnlagPrStatusRestDto> sammenligningsgrunnlagIkkeSplittet = input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
            .filter(s -> s.getSammenligningsgrunnlagType().equals(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN))
            .findAny();

        if(sammenligningsgrunnlagIkkeSplittet.isPresent()) {
            return skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(input, andel, sammenligningsgrunnlagIkkeSplittet.get());
        }

        if(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getAktivitetStatus())){
            return skalGrunnlagFastsettesForSN(input, andel);
        }

        return input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
            .filter(s -> SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP.get(s.getSammenligningsgrunnlagType()).equals(andel.getAktivitetStatus()))
            .anyMatch(s -> erAvvikStørreEnn25Prosent(s.getAvvikPromilleNy()));

    }

    private static boolean finnesIngenSammenligningsgrunnlagPrStatus(BeregningsgrunnlagRestInput input){
        return input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().isEmpty();
    }

    private static boolean skalGrunnlagFastsettesForSN(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        Optional<SammenligningsgrunnlagPrStatusRestDto> sammenligningsgrunnlagPrStatus =  input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
            .filter(s -> AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP.get(s.getSammenligningsgrunnlagType())))
            .findAny();
        if(sammenligningsgrunnlagPrStatus.isPresent()){
            return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.get().getAvvikPromilleNy());
        }
        return Boolean.TRUE.equals(andel.getNyIArbeidslivet());
    }

    private static boolean  skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPrStatusOgAndelRestDto andel, SammenligningsgrunnlagPrStatusRestDto sammenligningsgrunnlagPrStatus){
        if(finnesSelvstendigNæringsdrivendeAndel(input)) {
            if(andel.getAktivitetStatus().erSelvstendigNæringsdrivende()){
                return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.getAvvikPromilleNy()) || Boolean.TRUE.equals(andel.getNyIArbeidslivet());
            } else {
                return false;
            }
        }
        return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.getAvvikPromilleNy());
    }

    private static boolean skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPrStatusOgAndelRestDto andel, SammenligningsgrunnlagRestDto sammenligningsgrunnlag){
        if(finnesSelvstendigNæringsdrivendeAndel(input)) {
            if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
                return finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(sammenligningsgrunnlag) || Boolean.TRUE.equals(andel.getNyIArbeidslivet());
            } else {
                return false;
            }
        }
        return finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(sammenligningsgrunnlag);
    }

    private static boolean finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(SammenligningsgrunnlagRestDto sammenligningsgrunnlag){
        if(sammenligningsgrunnlag != null){
            return erAvvikStørreEnn25Prosent(sammenligningsgrunnlag.getAvvikPromilleNy());
        }
        return false;
    }

    private static boolean erAvvikStørreEnn25Prosent(BigDecimal avvikPromille){
        return avvikPromille.compareTo(BigDecimal.valueOf(250)) > 0;
    }

    private static boolean finnesSelvstendigNæringsdrivendeAndel(BeregningsgrunnlagRestInput input){
        List<BeregningsgrunnlagPrStatusOgAndelRestDto> beregningsgrunnlagPrStatusOgAndel = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        return beregningsgrunnlagPrStatusOgAndel.stream().anyMatch(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende());
    }

    private static boolean dtoKanSorteres(List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidsarbeidstakerAndeler) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> listMedNull = arbeidsarbeidstakerAndeler
            .stream()
            .filter(a -> a.getBeregnetPrAar() == null)
            .collect(toList());
        return listMedNull.isEmpty();
    }

    private static Comparator<BeregningsgrunnlagPrStatusOgAndelDto> comparatorEtterBeregnetOgArbeidsforholdId() {
        return Comparator.comparing(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrAar)
            .reversed();
    }

}
