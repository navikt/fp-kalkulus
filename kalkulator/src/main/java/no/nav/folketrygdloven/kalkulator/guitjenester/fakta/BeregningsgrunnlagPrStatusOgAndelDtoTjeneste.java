package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelTilkommetArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;

public class BeregningsgrunnlagPrStatusOgAndelDtoTjeneste {

    private static final int MND_I_ÅR = 12;

    private static final Map<SammenligningsgrunnlagType, no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus> SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP;
    static {
        SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP = Map.of(
            SammenligningsgrunnlagType.SAMMENLIGNING_AT, no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.ARBEIDSTAKER,
            SammenligningsgrunnlagType.SAMMENLIGNING_FL, no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.FRILANSER,
            SammenligningsgrunnlagType.SAMMENLIGNING_SN, no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private BeregningsgrunnlagPrStatusOgAndelDtoTjeneste() {
        // Skjul
    }

    public static List<BeregningsgrunnlagPrStatusOgAndelDto> lagBeregningsgrunnlagPrStatusOgAndelDto(BeregningsgrunnlagRestInput input,
                                                                                                     List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList) {

        List<BeregningsgrunnlagPrStatusOgAndelDto> usortertDtoList = new ArrayList<>();
        for (no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel : beregningsgrunnlagPrStatusOgAndelList) {
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
                                                               no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var iayGrunnlag = input.getIayGrunnlag();
        var inntektsmeldinger = input.getInntektsmeldinger();
        var ref = input.getBehandlingReferanse();
        var beregningAktivitetAggregat = input.getBeregningsgrunnlagGrunnlag().getGjeldendeAktiviteter();
        BeregningsgrunnlagPrStatusOgAndelDto dto = LagTilpassetDtoTjeneste.opprettTilpassetDTO(ref, andel, iayGrunnlag);
        LocalDate skjæringstidspunktForBeregning = input.getSkjæringstidspunktForBeregning();
        Optional<InntektsmeldingDto> inntektsmelding = BeregningInntektsmeldingTjeneste.finnInntektsmeldingForAndel(andel, inntektsmeldinger);
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, inntektsmelding, iayGrunnlag).ifPresent(dto::setArbeidsforhold);
        dto.setDagsats(andel.getDagsats());
        dto.setOriginalDagsatsFraTilstøtendeYtelse(andel.getOrginalDagsatsFraTilstøtendeYtelse());
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setAktivitetStatus(new no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus(andel.getAktivitetStatus().getKode()));
        dto.setBeregningsperiodeFom(andel.getBeregningsperiodeFom());
        dto.setBeregningsperiodeTom(andel.getBeregningsperiodeTom());
        dto.setBruttoPrAar(andel.getBruttoPrÅr());
        dto.setFordeltPrAar(andel.getFordeltPrÅr());
        dto.setAvkortetPrAar(andel.getAvkortetPrÅr());
        dto.setRedusertPrAar(andel.getRedusertPrÅr());
        dto.setOverstyrtPrAar(andel.getOverstyrtPrÅr());
        dto.setBeregnetPrAar(andel.getBeregnetPrÅr());
        dto.setInntektskategori(new Inntektskategori(andel.getInntektskategori().getKode()));
        dto.setBesteberegningPrAar(andel.getBesteberegningPrÅr());
        dto.setFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler());
        dto.setErTidsbegrensetArbeidsforhold(andel.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforholdDto::getErTidsbegrensetArbeidsforhold).orElse(null));
        dto.setErNyIArbeidslivet(andel.getNyIArbeidslivet());
        dto.setLonnsendringIBeregningsperioden(andel.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforholdDto::erLønnsendringIBeregningsperioden).orElse(null));
        dto.setLagtTilAvSaksbehandler(andel.getLagtTilAvSaksbehandler());
        dto.setErTilkommetAndel(FordelTilkommetArbeidsforholdTjeneste.erNyAktivitet(andel, beregningAktivitetAggregat, skjæringstidspunktForBeregning));
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

    private static boolean skalGrunnlagFastsettes(BeregningsgrunnlagRestInput input, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel){
        if(finnesIngenSammenligningsgrunnlagPrStatus(input)){
            return skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(input, andel, input.getBeregningsgrunnlag().getSammenligningsgrunnlag());
        }

        Optional<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagIkkeSplittet = input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
            .filter(s -> s.getSammenligningsgrunnlagType().equals(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN))
            .findAny();

        if(sammenligningsgrunnlagIkkeSplittet.isPresent()) {
            return skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(input, andel, sammenligningsgrunnlagIkkeSplittet.get());
        }

        if(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getAktivitetStatus())){
            return skalGrunnlagFastsettesForSN(input, andel);
        }

        return input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
            .filter(s -> SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP.get(s.getSammenligningsgrunnlagType()).equals(andel.getAktivitetStatus()))
            .anyMatch(s -> erAvvikStørreEnn25Prosent(s.getAvvikPromilleNy()));

    }

    private static boolean finnesIngenSammenligningsgrunnlagPrStatus(BeregningsgrunnlagRestInput input){
        return input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().isEmpty();
    }

    private static boolean skalGrunnlagFastsettesForSN(BeregningsgrunnlagRestInput input, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        Optional<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatus =  input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
            .filter(s -> no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP.get(s.getSammenligningsgrunnlagType())))
            .findAny();
        if(sammenligningsgrunnlagPrStatus.isPresent()){
            return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.get().getAvvikPromilleNy());
        }
        return Boolean.TRUE.equals(andel.getNyIArbeidslivet());
    }

    private static boolean  skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(BeregningsgrunnlagRestInput input,
                                                                                   no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                   SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus){
        if(finnesSelvstendigNæringsdrivendeAndel(input)) {
            if(andel.getAktivitetStatus().erSelvstendigNæringsdrivende()){
                return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.getAvvikPromilleNy()) || Boolean.TRUE.equals(andel.getNyIArbeidslivet());
            } else {
                return false;
            }
        }
        return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.getAvvikPromilleNy());
    }

    private static boolean skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(BeregningsgrunnlagRestInput input,
                                                                                  no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                  SammenligningsgrunnlagDto sammenligningsgrunnlag){
        if(finnesSelvstendigNæringsdrivendeAndel(input)) {
            if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
                return finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(sammenligningsgrunnlag) || Boolean.TRUE.equals(andel.getNyIArbeidslivet());
            } else {
                return false;
            }
        }
        return finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(sammenligningsgrunnlag);
    }

    private static boolean finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(SammenligningsgrunnlagDto sammenligningsgrunnlag){
        if(sammenligningsgrunnlag != null){
            return erAvvikStørreEnn25Prosent(sammenligningsgrunnlag.getAvvikPromilleNy());
        }
        return false;
    }

    private static boolean erAvvikStørreEnn25Prosent(BigDecimal avvikPromille){
        return avvikPromille.compareTo(BigDecimal.valueOf(250)) > 0;
    }

    private static boolean finnesSelvstendigNæringsdrivendeAndel(BeregningsgrunnlagRestInput input){
        List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndel = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
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
