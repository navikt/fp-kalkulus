package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.felles.FinnInntektsmeldingForAndel;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
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

    public List<BeregningsgrunnlagPrStatusOgAndelDto> lagBeregningsgrunnlagPrStatusOgAndelDto(BeregningsgrunnlagGUIInput input,
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

    private BeregningsgrunnlagPrStatusOgAndelDto lagDto(BeregningsgrunnlagGUIInput input,
                                                        no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var iayGrunnlag = input.getIayGrunnlag();
        var inntektsmeldinger = input.getInntektsmeldinger();
        var ref = input.getKoblingReferanse();
        Optional<FaktaAggregatDto> faktaAggregat = input.getFaktaAggregat();
        BeregningsgrunnlagPrStatusOgAndelDto dto = LagTilpassetDtoTjeneste.opprettTilpassetDTO(ref, andel, iayGrunnlag, faktaAggregat);
        Optional<InntektsmeldingDto> inntektsmelding = FinnInntektsmeldingForAndel.finnInntektsmelding(andel, inntektsmeldinger);
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
        dto.setInntektskategori(andel.getGjeldendeInntektskategori());
        dto.setBesteberegningPrAar(andel.getBesteberegningPrÅr());
        dto.setFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler());
        faktaAggregat.flatMap(fa -> fa.getFaktaArbeidsforhold(andel))
                .map(FaktaArbeidsforholdDto::getErTidsbegrensetVurdering).ifPresent(dto::setErTidsbegrensetArbeidsforhold);
        faktaAggregat.flatMap(FaktaAggregatDto::getFaktaAktør).map(FaktaAktørDto::getErNyIArbeidslivetSNVurdering).ifPresent(dto::setErNyIArbeidslivet);
        faktaAggregat.flatMap(fa -> fa.getFaktaArbeidsforhold(andel))
                .map(FaktaArbeidsforholdDto::getHarLønnsendringIBeregningsperiodenVurdering).ifPresent(dto::setLonnsendringIBeregningsperioden);
        dto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        dto.setErTilkommetAndel(!andel.getKilde().equals(AndelKilde.PROSESS_START));
        if (andel.getAktivitetStatus().erFrilanser()
                || andel.getAktivitetStatus().erArbeidstaker()
                || andel.getAktivitetStatus().erSelvstendigNæringsdrivende()
                || andel.getAktivitetStatus().equals(AktivitetStatus.BRUKERS_ANDEL)) {
            dto.setSkalFastsetteGrunnlag(skalGrunnlagFastsettesForYtelse(input, andel));
        }
        return dto;
    }

    private boolean skalGrunnlagFastsettesForYtelse(BeregningsgrunnlagGUIInput input,
                                                    no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return FagsakYtelseTypeRef.Lookup.find(fastsettGrunnlag, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for om grunnlag skal fastsettes for BehandlingReferanse " + input.getKoblingReferanse()))
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
