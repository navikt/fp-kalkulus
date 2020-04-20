package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.ATogFLISammeOrganisasjonDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AndelMedBeløpDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.KortvarigeArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.KunYtelseDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderBesteberegningDto;

public class FaktaOmBeregningDtoTjenesteImplTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.now();

    private FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste;
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(LocalDate.now());

    @BeforeEach
    public void setUp() {
        @SuppressWarnings("unchecked")
        Instance<FaktaOmBeregningTilfelleDtoTjeneste> tjenesteInstances = mock(Instance.class);
        List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester = new ArrayList<>();
        tjenester.add(lagDtoTjenesteMock(setFrilansAndelConsumer()));
        tjenester.add(lagDtoTjenesteMock(atflSammeOrgConsumer()));
        tjenester.add(lagDtoTjenesteMock(kunYtelseConsumer()));
        tjenester.add(lagDtoTjenesteMock(kortvarigeArbeidsforholdConsumer()));
        tjenester.add(lagDtoTjenesteMock(vurderLønnsendringConsumer()));
        tjenester.add(lagDtoTjenesteMock(vurderBesteberegningConsumer()));
        when(tjenesteInstances.iterator()).thenReturn(tjenester.iterator());
        when(tjenesteInstances.stream()).thenReturn(tjenester.stream());
        faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjeneste(tjenesteInstances);
    }

    @Test
    public void skal_kalle_dto_tjenester() {
        // Arrange
        List<FaktaOmBeregningTilfelle> tilfeller = List.of(
            FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL,
            FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON,
            FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE,
            FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD,
            FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING,
            FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE,
            FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(inntektsmeldinger).build();

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(tilfeller);
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        builder.leggTilAktivitet(BeregningAktivitetDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet("test"))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(10)))
            .build());

        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag);
        oppdatere.medRegisterAktiviteter(builder.build());

        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
            .medBeregningsgrunnlagGrunnlag(oppdatere.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));

        // Act
        Optional<FaktaOmBeregningDto> dto = faktaOmBeregningDtoTjeneste.lagDto(input);

        // Assert
        assertThat(dto.get().getFrilansAndel().getAndelsnr()).isEqualTo(1);
        assertThat(dto.get().getArbeidstakerOgFrilanserISammeOrganisasjonListe()).hasSize(1);
        assertThat(dto.get().getKunYtelse().getAndeler()).hasSize(1);
        assertThat(dto.get().getKortvarigeArbeidsforhold()).hasSize(1);
        assertThat(dto.get().getArbeidsforholdMedLønnsendringUtenIM()).hasSize(1);
    }

    @Test
    public void skal_lage_fakta_om_beregning_dto_når_man_har_tilfeller_i_fakta_om_beregning() {
        List<FaktaOmBeregningTilfelle> tilfeller = Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(tilfeller);
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        builder.leggTilAktivitet(BeregningAktivitetDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet("test"))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(10)))
            .build());

        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag);
        oppdatere.medRegisterAktiviteter(builder.build());

        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(oppdatere.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));

        Optional<FaktaOmBeregningDto> dto = faktaOmBeregningDtoTjeneste.lagDto(input);
        assertThat(dto.isPresent()).isTrue();
    }

    @Test
    public void skal_lage_fakta_om_beregning_dto_med_avklar_aktiviterer() {
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(List.of(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE));

        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        builder.leggTilAktivitet(BeregningAktivitetDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet("test"))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(10)))
            .build());

        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag);
        oppdatere.medRegisterAktiviteter(builder.build());

        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(oppdatere.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));

        Optional<FaktaOmBeregningDto> dto = faktaOmBeregningDtoTjeneste
            .lagDto(input);
        assertThat(dto).isPresent();
        assertThat(dto.orElseThrow().getAvklarAktiviteter().getAktiviteterTomDatoMapping()).isNotNull();
    }


    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(List<FaktaOmBeregningTilfelle> tilfeller) {
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty());
        BeregningsgrunnlagDto beregningsgrunnlagDto = oppdatere
            .getBeregningsgrunnlagBuilder()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .leggTilFaktaOmBeregningTilfeller(tilfeller)
            .build();

        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlagDto);

        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .build(periode);

        oppdatere.medBeregningsgrunnlag(beregningsgrunnlagDto);
        return oppdatere.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    private FaktaOmBeregningTilfelleDtoTjeneste lagDtoTjenesteMock(Consumer<FaktaOmBeregningDto> dtoConsumer) {
        return (input, faktaOmBeregningDto) -> dtoConsumer.accept(faktaOmBeregningDto);
    }

    private Consumer<FaktaOmBeregningDto> setFrilansAndelConsumer() {
        return (dto) -> {
            FaktaOmBeregningAndelDto andel = new FaktaOmBeregningAndelDto();
            andel.setAndelsnr(1L);
            dto.setFrilansAndel(andel);
        };
    }

    private Consumer<FaktaOmBeregningDto> atflSammeOrgConsumer() {
        return (dto) -> {
            ATogFLISammeOrganisasjonDto atflSammeOrgDto = new ATogFLISammeOrganisasjonDto();
            atflSammeOrgDto.setAndelsnr(1L);
            dto.setArbeidstakerOgFrilanserISammeOrganisasjonListe(Collections.singletonList(atflSammeOrgDto));
        };
    }

    private Consumer<FaktaOmBeregningDto> kunYtelseConsumer() {
        return (dto) -> {
            KunYtelseDto kunYtelseDto = new KunYtelseDto();
            AndelMedBeløpDto brukersAndelDto = new AndelMedBeløpDto();
            brukersAndelDto.setAndelsnr(1L);
            kunYtelseDto.setAndeler(Collections.singletonList(brukersAndelDto));
            dto.setKunYtelse(kunYtelseDto);
        };
    }

    private Consumer<FaktaOmBeregningDto> kortvarigeArbeidsforholdConsumer() {
        return (dto) -> {
            KortvarigeArbeidsforholdDto kortvarigeArbeidsforholdDto = new KortvarigeArbeidsforholdDto();
            kortvarigeArbeidsforholdDto.setErTidsbegrensetArbeidsforhold(true);
            dto.setKortvarigeArbeidsforhold(Collections.singletonList(kortvarigeArbeidsforholdDto));
        };
    }

    private Consumer<FaktaOmBeregningDto> vurderLønnsendringConsumer() {
        return (dto) -> {
            FaktaOmBeregningAndelDto andelDto = new FaktaOmBeregningAndelDto();
            andelDto.setAndelsnr(1L);
            dto.setArbeidsforholdMedLønnsendringUtenIM(Collections.singletonList(andelDto));
        };
    }

    private Consumer<FaktaOmBeregningDto> vurderBesteberegningConsumer() {
        return (dto) -> {
            AndelMedBeløpDto andelDto = new AndelMedBeløpDto();
            andelDto.setAndelsnr(1L);
            VurderBesteberegningDto vurderBesteberegning = new VurderBesteberegningDto();
            vurderBesteberegning.setSkalHaBesteberegning(true);
            dto.setVurderBesteberegning(vurderBesteberegning);
        };
    }

}
