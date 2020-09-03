package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelTilkommetArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelDto;

@ApplicationScoped
public class BeregningsgrunnlagPrStatusOgAndelDtoTjeneste {

    private Instance<FastsettGrunnlagGenerell> fastsettGrunnlag;
    private static final int MND_I_ÅR = 12;

    public BeregningsgrunnlagPrStatusOgAndelDtoTjeneste() {
        // CDI Proxy
    }

    @Inject
    public BeregningsgrunnlagPrStatusOgAndelDtoTjeneste(@Any Instance<FastsettGrunnlagGenerell> fastsettGrunnlag) {
        this.fastsettGrunnlag = fastsettGrunnlag;
        // Skjul
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> lagBeregningsgrunnlagPrStatusOgAndelDto(BeregningsgrunnlagRestInput input,
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

    private BeregningsgrunnlagPrStatusOgAndelDto lagDto(BeregningsgrunnlagRestInput input,
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
            dto.setSkalFastsetteGrunnlag(skalGrunnlagFastsettesForYtelse(input, andel));
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

    private boolean skalGrunnlagFastsettesForYtelse(BeregningsgrunnlagRestInput input,
                                                    no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel){
        return FagsakYtelseTypeRef.Lookup.find(fastsettGrunnlag, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for om grunnlag skal fastsettes for BehandlingReferanse " + input.getBehandlingReferanse()))
                .skalGrunnlagFastsettes(input, andel);
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
