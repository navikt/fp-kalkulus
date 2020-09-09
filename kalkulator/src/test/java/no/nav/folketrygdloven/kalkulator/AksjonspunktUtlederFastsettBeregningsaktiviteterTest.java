package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.vedtak.util.FPDateUtil;


public class AksjonspunktUtlederFastsettBeregningsaktiviteterTest {

    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private KoblingReferanse ref;

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private AksjonspunktUtlederFastsettBeregningsaktiviteterFelles apUtleder = new AksjonspunktUtlederFastsettBeregningsaktiviteterFelles();
    private boolean erOverstyrt;

    private OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto();

    private InntektArbeidYtelseGrunnlagDto iayMock = mock(InntektArbeidYtelseGrunnlagDto.class);
    private AktørYtelseDto ay = mock(AktørYtelseDto.class);
    private BeregningsgrunnlagInput input;
    private static final String FUNKSJONELT_TIDSOFFSET = FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_PERIODE;
    private LocalDate tom;


    @BeforeEach
    public void setUp() {
        settSimulertNåtidTil(LocalDate.of(2020, 1, 8));
        FPDateUtil.init();

        erOverstyrt = false;

        LocalDate SKJÆRINGSTIDSPUNKT = FPDateUtil.iDag();
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(1);
        tom = SKJÆRINGSTIDSPUNKT.withDayOfMonth(1).minusDays(1);


        ref = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.FRILANSER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medBeregningsperiode(fom, tom)
            .build(periode);
        beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
            .build();
        input = new BeregningsgrunnlagInput(ref, null, null, null, null, null);
        input.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 100000);
    }

    @AfterEach
    public void teardown() {
        settSimulertNåtidTil(LocalDate.now());
        FPDateUtil.init();
    }


    @Test
    public void skalSettePåVentNårFørRapporteringsfrist() {
        // Arrange
        int rapporteringsfrist = 1000;
        LocalDateTime frist = tom.plusDays(rapporteringsfrist).plusDays(1).atStartOfDay();

        //Act
        List<BeregningAksjonspunktResultat> resultater = new AksjonspunktUtlederFastsettBeregningsaktiviteterFelles().utledAksjonspunkter(new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList()), beregningAktivitetAggregat, lagBeregningsgrunnlagInput(rapporteringsfrist), erOverstyrt, input.getFagsakYtelseType());

        // Assert
        assertThat(resultater).hasSize(1);
        BeregningAksjonspunktResultat beregningAksjonspunktResultat = resultater.get(0);
        assertThat(beregningAksjonspunktResultat.getBeregningAksjonspunktDefinisjon()).isEqualTo(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST);
        assertThat(beregningAksjonspunktResultat.getVentefrist()).isNotNull();
        assertThat(beregningAksjonspunktResultat.getVenteårsak()).isNotNull();

        assertThat(beregningAksjonspunktResultat.getVenteårsak()).isEqualTo(BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST);
        assertThat(beregningAksjonspunktResultat.getVentefrist()).isEqualTo(frist);
    }

    @Test
    public void skalUtledeAutopunktNårLøpendeYtelseOgMeldekortSomInkludererStpIkkeErMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate ytelsePeriodeFom = skjæringstidspunkt.minusMonths(2);
        YtelseDtoBuilder yb = YtelseDtoBuilder.oppdatere(Optional.empty());
        YtelseDto ytelse = yb.medYtelseType(FagsakYtelseType.DAGPENGER)
            .medPeriode(Intervall.fraOgMed(ytelsePeriodeFom))
            .leggTilYtelseAnvist(lagYtelseAnvist(yb.getAnvistBuilder(), skjæringstidspunkt.minusWeeks(3), skjæringstidspunkt.minusWeeks(1)))
            .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
            .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.DAGPENGER, Intervall.fraOgMed(ytelsePeriodeFom));

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister(any())).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        List<BeregningAksjonspunktResultat> resultater = apUtleder.utledAksjonspunkter(new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList()), beregningAktivitetAggregat, lagMockBeregningsgrunnlagInput(), erOverstyrt, input.getFagsakYtelseType());

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
            assertThat(resultat.getBeregningAksjonspunktDefinisjon()).isEqualTo(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT)
        );
    }

    private YtelseAnvistDto lagYtelseAnvist(YtelseAnvistDtoBuilder anvistBuilder, LocalDate fom, LocalDate tom) {
        return anvistBuilder.medAnvistPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).build();
    }

    @Test
    public void skalUtledeAutopunktVentPåInntektFrilansNårManSkalVentePåBådeInntekterFrilansOgAAPMeldekort() {
        // Arrange
        int rapporteringsfrist = 1000;

        // Act
        List<BeregningAksjonspunktResultat> resultater = apUtleder.utledAksjonspunkter(new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList()), beregningAktivitetAggregat, lagBeregningsgrunnlagInput(rapporteringsfrist), erOverstyrt, input.getFagsakYtelseType());

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
            assertThat(resultat.getBeregningAksjonspunktDefinisjon()).isEqualTo(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST)
        );
    }

    @Test
    public void skalIkkeUtledeAutopunktNårLøpendeYtelseOgMeldekortSomInkludererStpErMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate ytelsePeriodeFom = skjæringstidspunkt.minusMonths(2);
        YtelseDtoBuilder yb = YtelseDtoBuilder.oppdatere(Optional.empty());
        YtelseAnvistDto meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(7), skjæringstidspunkt.minusWeeks(5))).build();
        YtelseAnvistDto meldekort2 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(4), skjæringstidspunkt.minusWeeks(2))).build();
        YtelseAnvistDto meldekort3 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(1), skjæringstidspunkt)).build();
        YtelseAnvistDto meldekort4 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.plusDays(1), skjæringstidspunkt.plusWeeks(2))).build();
        YtelseDto ytelse = yb.medYtelseType(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(Intervall.fraOgMed(ytelsePeriodeFom))
                .leggTilYtelseAnvist(meldekort1)
                .leggTilYtelseAnvist(meldekort2)
                .leggTilYtelseAnvist(meldekort3)
                .leggTilYtelseAnvist(meldekort4)
                .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.ARBEIDSAVKLARING, Intervall.fraOgMed(ytelsePeriodeFom));

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister(any())).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        List<BeregningAksjonspunktResultat> resultater = apUtleder.utledAksjonspunkter(new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList()), beregningAktivitetAggregat, lagMockBeregningsgrunnlagInput(), erOverstyrt, input.getFagsakYtelseType());

        // Assert
        assertThat(resultater).hasSize(0);
    }

    @Test
    public void skalIkkeUtledeAutopunktNårYtelseOpphørerToDagerFørStp() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        Intervall periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(2));
        YtelseDtoBuilder yb = YtelseDtoBuilder.oppdatere(Optional.empty());
        YtelseAnvistDto meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(6), skjæringstidspunkt.minusWeeks(4))).build();
        YtelseDto ytelse = yb.medYtelseType(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1)
                .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.ARBEIDSAVKLARING, periodeIntervallForAktivitet);


        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister(any())).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        List<BeregningAksjonspunktResultat> resultater = apUtleder.utledAksjonspunkter(new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList()), beregningAktivitetAggregat, lagMockBeregningsgrunnlagInput(), erOverstyrt, input.getFagsakYtelseType());

        // Assert
        assertThat(resultater).hasSize(0);
    }

    @Test
    public void skalUtledeAutopunktNårYtelseOpphørerEnDagFørStpOgMeldekortIkkeMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        Intervall periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(1));
        YtelseDtoBuilder yb = YtelseDtoBuilder.oppdatere(Optional.empty());
        YtelseAnvistDto meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(4), skjæringstidspunkt.minusWeeks(2))).build();
        YtelseDto ytelse = yb.medYtelseType(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1)
                .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.ARBEIDSAVKLARING, periodeIntervallForAktivitet);

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister(any())).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        List<BeregningAksjonspunktResultat> resultater = apUtleder.utledAksjonspunkter(new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList()), beregningAktivitetAggregat, lagMockBeregningsgrunnlagInput(), erOverstyrt, input.getFagsakYtelseType());

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
                assertThat(resultat.getBeregningAksjonspunktDefinisjon()).isEqualTo(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT)
        );
    }

    @Test
    public void skalIkkeUtledeAutopunktNårYtelseOpphørerEnDagFørStpOgMeldekortErMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        Intervall periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(1));
        YtelseDtoBuilder yb = YtelseDtoBuilder.oppdatere(Optional.empty());
        YtelseAnvistDto meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(2), skjæringstidspunkt.minusDays(1))).build();
        YtelseDto ytelse = yb.medYtelseType(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1)
                .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.ARBEIDSAVKLARING, periodeIntervallForAktivitet);

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister(any())).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        List<BeregningAksjonspunktResultat> resultater = apUtleder.utledAksjonspunkter(new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList()), beregningAktivitetAggregat, lagMockBeregningsgrunnlagInput(), erOverstyrt, input.getFagsakYtelseType());

        // Assert
        assertThat(resultater).hasSize(0);
    }

    private BeregningsgrunnlagInput lagBeregningsgrunnlagInput(int rapporteringsfrist) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = new BeregningsgrunnlagInput(ref, getIAYGrunnlag(), opptjeningAktiviteter, null, List.of(), new ForeldrepengerGrunnlag(100, false));
        beregningsgrunnlagInput.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, rapporteringsfrist);
        return beregningsgrunnlagInput;
    }

    private BeregningsgrunnlagInput lagMockBeregningsgrunnlagInput() {
        return new BeregningsgrunnlagInput(ref, getMockedIAYGrunnlag(), opptjeningAktiviteter, null, List.of(), new ForeldrepengerGrunnlag(100, false));
    }

    private void settSimulertNåtidTil(LocalDate dato) {
        Period periode = Period.between(LocalDate.now(), dato);
        System.setProperty(FUNKSJONELT_TIDSOFFSET, periode.toString());
        FPDateUtil.init();
    }

    private InntektArbeidYtelseGrunnlagDto getIAYGrunnlag() {
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
    }

    private InntektArbeidYtelseGrunnlagDto getMockedIAYGrunnlag() {
        return iayMock;
    }

    private BeregningAktivitetAggregatDto lagBeregningAktivitetAggregatDto(LocalDate skjæringstidspunkt, OpptjeningAktivitetType opptjeningType, Intervall periodeForAktivitet){
        BeregningAktivitetDto beregningAktivitetDto = BeregningAktivitetDto.builder()
                .medOpptjeningAktivitetType(opptjeningType)
                .medPeriode(periodeForAktivitet)
                .build();
        return BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(beregningAktivitetDto)
                .medSkjæringstidspunktOpptjening(skjæringstidspunkt)
                .build();
    }

}
