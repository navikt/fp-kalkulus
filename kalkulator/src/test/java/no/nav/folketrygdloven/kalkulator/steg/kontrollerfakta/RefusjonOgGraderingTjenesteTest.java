package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt.FordelBeregningsgrunnlagTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt.FordelTilkommetArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;

public class RefusjonOgGraderingTjenesteTest {

    private static final String ORG_NUMMER = "991825827";
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.of(2018, 9, 30);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(100_000);
    private Arbeidsgiver arbeidsgiver1 = Arbeidsgiver.virksomhet(ORG_NUMMER);
    private Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet("456456456456");
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat = mock(BeregningAktivitetAggregatDto.class);
    private List<BeregningAktivitetDto> aktivitetList = new ArrayList<>();

    @BeforeEach
    public void setup() {
        when(beregningAktivitetAggregat.getBeregningAktiviteter()).thenReturn(aktivitetList);
    }

    @Test
    public void returnererFalseOmArbeidsforholdStarterFørStp() {
        // Arrange
        String orgnr = "123456780";
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        setAktivitetFørStp(arbeidsgiver, null);

        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(bg);
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10))
                .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1))
                .medArbeidsgiver(arbeidsgiver)
                .medRefusjonskravPrÅr(BigDecimal.valueOf(10000)))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(periode);
        // Act
        boolean tilkomEtter = FordelTilkommetArbeidsforholdTjeneste.erNyAktivitet(andel, beregningAktivitetAggregat, SKJÆRINGSTIDSPUNKT_BEREGNING);

        // Assert
        assertThat(tilkomEtter).isFalse();
    }

    @Test
    public void returnererTrueForArbeidsforholdSomStarterEtterSkjæringstidspunkt() {
        // Arrange
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();
        BeregningsgrunnlagPeriodeDto periode2 = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), null)
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3))
                .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(5))
                .medArbeidsgiver(arbeidsgiver1)
                .medRefusjonskravPrÅr(BigDecimal.valueOf(10000)))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(periode2);
        // Act
        boolean tilkomEtter = FordelTilkommetArbeidsforholdTjeneste.erNyAktivitet(andel, beregningAktivitetAggregat, SKJÆRINGSTIDSPUNKT_BEREGNING);
        // Assert
        assertThat(tilkomEtter).isTrue();
    }

    @Test
    public void returnererTrueForFLMedGraderingSomTilkommer() {
        // Arrange
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 1000, periode1, false, null, BigDecimal.valueOf(10), InternArbeidsforholdRefDto.nullRef());
        setAktivitetFørStp(arbeidsgiver1, InternArbeidsforholdRefDto.nullRef());
        lagFLAndel(periode1);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.FRILANSER)
            .medGradering(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusWeeks(18).minusDays(1), 50)
            .build());
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, aktivitetGradering, List.of());

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.containsValue(FordelingTilfelle.NY_AKTIVITET));
    }

    @Test
    public void returnererTrueForSNMedGraderingSomTilkommer() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 1000, periode1, false, null, BigDecimal.valueOf(10), InternArbeidsforholdRefDto.nullRef());
        setAktivitetFørStp(arbeidsgiver1, InternArbeidsforholdRefDto.nullRef());
        lagSNAndel(periode1);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(fom, tom, 50)
            .build());

        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, aktivitetGradering, List.of());

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.containsValue(FordelingTilfelle.NY_AKTIVITET));
    }

    @Test
    public void returnererFalseForNyInntektsmeldingUtenRefusjonskrav() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(orgnr);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBg();
        BeregningsgrunnlagPeriodeDto p1 = lagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusWeeks(2), Collections.emptyList(), beregningsgrunnlag);
        lagAndel(Arbeidsgiver.virksomhet(orgnr), null, p1, false, null, BigDecimal.valueOf(10), arbId);
        BeregningsgrunnlagPeriodeDto p2 = lagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusWeeks(2).plusDays(1), null, Collections.singletonList(PeriodeÅrsak.GRADERING), beregningsgrunnlag);
        lagAndel(Arbeidsgiver.virksomhet(orgnr), null, p2, false, null, BigDecimal.valueOf(10), arbId);

        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr, arbId, SKJÆRINGSTIDSPUNKT_BEREGNING);
        setAktivitetFørStp(virksomhet, arbId);


        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(beregningsgrunnlag,
            beregningAktivitetAggregat, AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();
    }

    // Gradering: Ja
    // Refusjon: Nei
    // Tilkom etter skjæringstidspunktet: Ja
    // Returnerer true
    @Test
    public void returnererTrueForGraderingOgArbeidsforholdetTilkomEtterSkjæringstidpunktet() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        var arbId = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, null, periode1, true, null, BigDecimal.valueOf(10), arbId);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbeidsgiver(arbeidsgiver1)
            .medArbeidsforholdRef(arbId)
            .medGradering(fom, tom, 50)
            .build());
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, aktivitetGradering, List.of());

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.containsValue(FordelingTilfelle.NY_AKTIVITET));
    }

    // Gradering: Ja
    // Refusjon: Nei
    // Tilkom etter skjæringstidspunktet: Nei
    // Total refusjon under 6G
    // Returnerer false
    @Test
    public void returnererTrueForGraderingGjeldendeBruttoBGStørreEnnNullBeregningsgrunnlagsandelAvkortetTilNull() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        String orgnr1 = "123456780";
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, 0);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 0, periode1, false, null, BigDecimal.valueOf(10), arbId1);
        setAktivitetFørStp(arbeidsgiver1, arbId1);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();
    }

    // Gradering: Ja
    // Refusjon: Nei
    // Total refusjon større enn 6G for alle arbeidsforhold
    // Tilkom etter skjæringstidspunktet: Nei
    // Returnerer True
    @Test
    public void returnererTrueForGraderingGjeldendeBruttoBGLikNullTotalRefusjonStørreEnn6G() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);

        int seksG = GRUNNBELØP.multiply(BigDecimal.valueOf(6)).intValue();
        int refusjon2PerÅr = seksG + 12;
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver2.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, refusjon2PerÅr / 12);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, null, periode1, false, null, BigDecimal.valueOf(10), arbId1);
        lagAndel(arbeidsgiver2, refusjon2PerÅr, periode1, false, null, BigDecimal.valueOf(10), arbId2);
        setAktivitetFørStp(arbeidsgiver1, arbId1);
        setAktivitetFørStp(arbeidsgiver2, arbId2);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbeidsgiver(arbeidsgiver1)
            .medArbeidsforholdRef(arbId1)
            .medGradering(fom, tom, 50)
            .build());
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, aktivitetGradering, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.containsValue(FordelingTilfelle.TOTALT_REFUSJONSKRAV_STØRRE_ENN_6G));

    }

    // Gradering: Ja
    // Refusjon: Nei
    // Total refusjon mindre enn 6G for alle arbeidsforhold
    // Tilkom etter skjæringstidspunktet: Nei
    // Returnerer True
    @Test
    public void returnererFalseForGraderingGjeldendeBruttoBGLikNullTotalRefusjonMindreEnn6G() {
        // Arrange
        int seksG = GRUNNBELØP.multiply(BigDecimal.valueOf(6)).intValue();
        int refusjon2 = seksG - 12;
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver2.getIdentifikator(), arbId2, SKJÆRINGSTIDSPUNKT_BEREGNING, refusjon2 / 12);
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, null, periode1, false, null, BigDecimal.valueOf(10), arbId1);
        lagAndel(arbeidsgiver2, refusjon2 / 12, periode1, false, null, BigDecimal.valueOf(10), arbId2);
        setAktivitetFørStp(arbeidsgiver1, arbId1);
        setAktivitetFørStp(arbeidsgiver2, arbId2);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, AktivitetGradering.INGEN_GRADERING, List.of(im1, im2));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();

    }
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Returnerer True

    // FIXME: Sender inn INGEN_GRADERING når testen seier den skal ha gradering
    @Test
    public void returnererTrueForGraderingOgRefusjonUtenGjeldendeBG() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1,
            SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 100, periode1, true, null, BigDecimal.valueOf(10), arbId1);
        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.containsValue(FordelingTilfelle.NY_AKTIVITET));
    }

    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Returnerer False
    // FIXME: Seier at den skal ha gradering, men sender inn INGEN_GRADERING
    @Test
    public void returnererFalseForGraderingOgRefusjon() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1,
            SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 100, periode1, false, null, BigDecimal.valueOf(10), arbId1);
        setAktivitetFørStp(arbeidsgiver1, arbId1);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();
    }
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Returnerer True

    @Test
    public void returnererTrueForRefusjonArbfholdTilkomEtterStp() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, 1000);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 1000, periode1, true, null, BigDecimal.valueOf(10), arbId1);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.containsValue(FordelingTilfelle.NY_AKTIVITET));
    }

    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Returnerer False
    @Test
    public void returnererFalseForRefusjonGjeldendeBruttoBGStørreEnn0() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, 1000);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, 1000, periode1, false, null, BigDecimal.valueOf(10), arbId1);
        setAktivitetFørStp(arbeidsgiver1, arbId1);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, AktivitetGradering.INGEN_GRADERING, List.of(im1));

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.isEmpty()).isTrue();
    }


    @Test
    public void returnererTrueNårGradertNæringMedArbeidstakerTotalRefusjonUnder6GOgBGOver6G() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        BigDecimal seksG = GRUNNBELØP.multiply(BigDecimal.valueOf(6));
        int refusjon = (seksG.intValue() - 1) / 12;
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver1.getIdentifikator(), arbId1, SKJÆRINGSTIDSPUNKT_BEREGNING, refusjon);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagAndel(arbeidsgiver1, refusjon*12, periode1, false, null, seksG.add(BigDecimal.ONE), arbId1);
        lagSNAndel(periode1);
        setAktivitetFørStp(arbeidsgiver1, arbId1);
        setAktivitetFørStp(OpptjeningAktivitetType.NÆRING);
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .leggTilGradering(fom, tom, BigDecimal.valueOf(50))
            .build());

        // Act
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(bg, beregningAktivitetAggregat, aktivitetGradering, List.of(im1));
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> andelerMedTilfeller = FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandling(fordelingInput);

        // Assert
        assertThat(andelerMedTilfeller.containsValue(FordelingTilfelle.GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0));
    }

    @Test
    public void returnererTrueForSNMedGraderingUtenBeregningsgrunnlag() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate tom = fom.plusWeeks(18).minusDays(1);
        BeregningsgrunnlagDto bg = lagBg();
        BeregningsgrunnlagPeriodeDto periode1 = lagPeriode(bg);
        lagSNAndel(periode1, 0);
        setAktivitetFørStp(OpptjeningAktivitetType.NÆRING);

        // Act
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .leggTilGradering(fom, tom, BigDecimal.valueOf(50))
            .build());

        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> manuellBehandlingForEndringAvBG = vurderManuellBehandling(bg, beregningAktivitetAggregat, aktivitetGradering, List.of());

        // Assert
        assertThat(manuellBehandlingForEndringAvBG.containsValue(FordelingTilfelle.FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0));
    }

    private void setAktivitetFørStp(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        List<BeregningAktivitetDto> aktiviteterFørStp = Collections.singletonList(BeregningAktivitetDto.builder()
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10)))
            .medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(arbeidsforholdRef).build());
        aktivitetList.addAll(aktiviteterFørStp);
    }

    private void setAktivitetFørStp(OpptjeningAktivitetType type) {
        List<BeregningAktivitetDto> aktiviteterFørStp = Collections.singletonList(BeregningAktivitetDto.builder()
            .medOpptjeningAktivitetType(type)
            .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10))).build());
        aktivitetList.addAll(aktiviteterFørStp);
    }

    private Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> vurderManuellBehandling(BeregningsgrunnlagDto bg, BeregningAktivitetAggregatDto beregningAktivitetAggregat, AktivitetGradering aktivitetGradering, Collection<InntektsmeldingDto> inntektsmeldinger) {
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(bg, beregningAktivitetAggregat, aktivitetGradering, inntektsmeldinger);
        return FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandling(fordelingInput);
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto bg) {
        return BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .build(bg);
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(LocalDate fom, LocalDate tom, Collection<PeriodeÅrsak> periodeÅrsaker, BeregningsgrunnlagDto bg) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(fom, tom);
        periodeÅrsaker.forEach(builder::leggTilPeriodeÅrsak);
        return builder
            .build(bg);
    }

    private BeregningsgrunnlagDto lagBg() {
        return BeregningsgrunnlagDto.builder().leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
    }

    private void lagAndel(Arbeidsgiver arbeidsgiver,
                          Integer refusjon2,
                          BeregningsgrunnlagPeriodeDto periode1,
                          boolean tilkomEtter,
                          BigDecimal overstyrtPrÅr,
                          BigDecimal beregnetPrÅr,
                          InternArbeidsforholdRefDto arbeidsforholdRef) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(beregnetPrÅr)
            .medOverstyrtPrÅr(overstyrtPrÅr)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsforholdRef)
                .medRefusjonskravPrÅr(refusjon2 == null ? null : BigDecimal.valueOf(refusjon2))
                .medArbeidsperiodeFom(tilkomEtter ? periode1.getBeregningsgrunnlagPeriodeFom() : SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12)))
            .build(periode1);
    }

    private void lagFLAndel(BeregningsgrunnlagPeriodeDto periode1) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.FRILANSER)
            .build(periode1);
    }

    private void lagSNAndel(BeregningsgrunnlagPeriodeDto periode1) {
        lagSNAndel(periode1, 10);
    }

    private void lagSNAndel(BeregningsgrunnlagPeriodeDto periode1, int beregnetPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr))
            .build(periode1);
    }

}
