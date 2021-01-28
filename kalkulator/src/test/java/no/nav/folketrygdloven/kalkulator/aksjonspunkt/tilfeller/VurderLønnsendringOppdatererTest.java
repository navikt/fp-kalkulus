package no.nav.folketrygdloven.kalkulator.aksjonspunkt.tilfeller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.VurderLønnsendringDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class VurderLønnsendringOppdatererTest {
    private static final Long ANDELSNR_ARBEIDSTAKER = 2L;
    private static final List<FaktaOmBeregningTilfelle> FAKTA_OM_BEREGNING_TILFELLER = Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING);
    public static final String ORGNR = "8934232423";
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);

    private VurderLønnsendringOppdaterer vurderLønnsendringOppdaterer;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPrStatusOgAndelDto frilansAndel;
    private BeregningsgrunnlagPrStatusOgAndelDto arbeidstakerAndel;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setup() {
        vurderLønnsendringOppdaterer = new VurderLønnsendringOppdaterer();
        Arbeidsgiver virksomheten = Arbeidsgiver.virksomhet(ORGNR);
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()

                .medGrunnbeløp(GRUNNBELØP)
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
                .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
                .build(beregningsgrunnlag);
        frilansAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(3252L)
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .build(periode1);
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT.minusMonths(3), SKJÆRINGSTIDSPUNKT.minusDays(1))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomheten))
                .medAndelsnr(ANDELSNR_ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode1);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                        .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                                        .medArbeidsgiver(virksomheten)
                                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medErAnsettelsesPeriode(true)
                                                .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10))))
                                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medErAnsettelsesPeriode(false)
                                                .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10)))
                                                .medSisteLønnsendringsdato(SKJÆRINGSTIDSPUNKT.minusMonths(1))))))
                .medInformasjon(ArbeidsforholdInformasjonDtoBuilder
                        .oppdatere(Optional.empty())
                        .leggTil(ArbeidsforholdOverstyringDtoBuilder.oppdatere(Optional.empty())
                                .medHandling(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING)
                                .medArbeidsgiver(virksomheten)).build()).build();
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                        .medBeregningsgrunnlag(beregningsgrunnlag),
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);
    }

    @Test
    public void skal_sette_lønnsendring_til_true_på_arbeidstakerandel() {
        // Arrange
        VurderLønnsendringDto lønnsendringDto = new VurderLønnsendringDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurdertLonnsendring(lønnsendringDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        vurderLønnsendringOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat).isPresent();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold().size()).isEqualTo(1);
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(arbeidstakerAndel).get().getHarLønnsendringIBeregningsperioden()).isTrue();
        assertThat(frilansAndel.getBgAndelArbeidsforhold()).isNotPresent();
    }

    @Test
    public void skal_ikkje_sette_lønnsendring_til_true_på_arbeidstakerandel() {
        // Arrange
        VurderLønnsendringDto lønnsendringDto = new VurderLønnsendringDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurdertLonnsendring(lønnsendringDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        vurderLønnsendringOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);
        Optional<FaktaAggregatDto> faktaAggregat = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat();

        // Assert
        assertThat(faktaAggregat).isPresent();
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold().size()).isEqualTo(1);
        assertThat(faktaAggregat.get().getFaktaArbeidsforhold(arbeidstakerAndel).get().getHarLønnsendringIBeregningsperioden()).isFalse();
        assertThat(frilansAndel.getBgAndelArbeidsforhold()).isNotPresent();
    }
}
