package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import static no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteAndelerTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsattePerioderTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBGTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Virksomhet;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

public class FastsettBGTidsbegrensetArbeidsforholdHåndtererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);


    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private List<FastsattePerioderTidsbegrensetDto> fastsatteInnteker;
    private final LocalDate FØRSTE_PERIODE_FOM = LocalDate.now().minusDays(100);
    private final LocalDate FØRSTE_PERIODE_TOM = LocalDate.now().minusDays(50);
    private final LocalDate ANDRE_PERIODE_FOM = LocalDate.now().minusDays(49);
    private final LocalDate ANDRE_PERIODE_TOM = LocalDate.now();
    private final Long FØRSTE_ANDELSNR = 1L;
    private final Long ANDRE_ANDELSNR = 2L;
    private final Integer FØRSTE_PERIODE_FØRSTE_ANDEL_INNTEKT = 100000;
    private final Integer FØRSTE_PERIODE_ANDRE_ANDEL_INNTEKT = 200000;
    private final Integer ANDRE_PERIODE_FØRSTE_ANDEL_INNTEKT = 300000;
    private final Integer ANDRE_PERIODE_ANDRE_ANDEL_INNTEKT = 400000;
    private VirksomhetEntitet virksomhet1;
    private VirksomhetEntitet virksomhet2;
    private BeregningsgrunnlagInput input;


    @BeforeEach
    public void setup() {
        fastsatteInnteker = lagFastsatteAndelerListe();
        virksomhet1 = new VirksomhetEntitet.Builder()
                .medOrgnr("123")
                .medNavn("VirksomhetNavn1")
                .oppdatertOpplysningerNå()
                .build();
        virksomhet2 = new VirksomhetEntitet.Builder()
                .medOrgnr("456")
                .medNavn("VirksomhetNavn2")
                .oppdatertOpplysningerNå()
                .build();
    }

    private List<FastsattePerioderTidsbegrensetDto> lagFastsatteAndelerListe() {
        FastsatteAndelerTidsbegrensetDto andelEnPeriodeEn = new FastsatteAndelerTidsbegrensetDto(FØRSTE_ANDELSNR, FØRSTE_PERIODE_FØRSTE_ANDEL_INNTEKT);
        FastsatteAndelerTidsbegrensetDto andelToPeriodeEn = new FastsatteAndelerTidsbegrensetDto(ANDRE_ANDELSNR, FØRSTE_PERIODE_ANDRE_ANDEL_INNTEKT);

        FastsattePerioderTidsbegrensetDto førstePeriode = new FastsattePerioderTidsbegrensetDto(
            FØRSTE_PERIODE_FOM,
            FØRSTE_PERIODE_TOM,
            List.of(andelEnPeriodeEn, andelToPeriodeEn)
        );

        FastsatteAndelerTidsbegrensetDto andelEnPeriodeTo = new FastsatteAndelerTidsbegrensetDto(FØRSTE_ANDELSNR, ANDRE_PERIODE_FØRSTE_ANDEL_INNTEKT);
        FastsatteAndelerTidsbegrensetDto andelToPeriodeTo = new FastsatteAndelerTidsbegrensetDto(ANDRE_ANDELSNR, ANDRE_PERIODE_ANDRE_ANDEL_INNTEKT);

        FastsattePerioderTidsbegrensetDto andrePeriode = new FastsattePerioderTidsbegrensetDto(
            ANDRE_PERIODE_FOM,
            ANDRE_PERIODE_TOM,
            List.of(andelEnPeriodeTo, andelToPeriodeTo)
        );

        return List.of(førstePeriode, andrePeriode);
    }


    @Test
    public void skal_sette_korrekt_overstyrtSum_på_korrekt_periode_og_korrekt_andel() {
        //Arrange
        lagBehandlingMedBeregningsgrunnlag();

        //Dto
        var dto = new FastsettBGTidsbegrensetArbeidsforholdDto(fastsatteInnteker,null);

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FastsettBGTidsbegrensetArbeidsforholdHåndterer.håndter(input, dto);

        //Assert
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag = grunnlag.getBeregningsgrunnlag();
        Assertions.assertThat(beregningsgrunnlag.isPresent()).isTrue();
        BeregningsgrunnlagPeriodeDto førstePeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPeriodeDto andrePeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(1);
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getOverstyrtPrÅr()).isEqualTo(BigDecimal.valueOf(FØRSTE_PERIODE_FØRSTE_ANDEL_INNTEKT));
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1).getOverstyrtPrÅr()).isEqualTo(BigDecimal.valueOf(FØRSTE_PERIODE_ANDRE_ANDEL_INNTEKT));
        assertThat(andrePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getOverstyrtPrÅr()).isEqualTo(BigDecimal.valueOf(ANDRE_PERIODE_FØRSTE_ANDEL_INNTEKT));
        assertThat(andrePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1).getOverstyrtPrÅr()).isEqualTo(BigDecimal.valueOf(ANDRE_PERIODE_ANDRE_ANDEL_INNTEKT));
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Virksomhet virksomhet) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet.getOrgnr()));
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

    private void lagBehandlingMedBeregningsgrunnlag() {


        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();


        BeregningsgrunnlagPeriodeDto førstePeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            FØRSTE_PERIODE_FOM, FØRSTE_PERIODE_TOM);
        buildBgPrStatusOgAndel(førstePeriode, virksomhet1);
        buildBgPrStatusOgAndel(førstePeriode, virksomhet2);

        BeregningsgrunnlagPeriodeDto andrePeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            ANDRE_PERIODE_FOM, ANDRE_PERIODE_TOM);
        buildBgPrStatusOgAndel(andrePeriode, virksomhet1);
        buildBgPrStatusOgAndel(andrePeriode, virksomhet2);


        input = lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT);
    }
}
