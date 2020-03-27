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
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Fagsystem;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
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
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.RelatertYtelseTilstand;
import no.nav.vedtak.util.FPDateUtil;


public class AksjonspunktUtlederFastsettBeregningsaktiviteterTest {

    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private BehandlingReferanse ref;

    private BeregningsgrunnlagDto beregningsgrunnlag;
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


        ref = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);
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
        List<BeregningAksjonspunktResultat> resultater = AksjonspunktUtlederFastsettBeregningsaktiviteter.utledAksjonspunkterForFelles(beregningsgrunnlag, beregningAktivitetAggregat, lagBeregningsgrunnlagInput(rapporteringsfrist), erOverstyrt);

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
    public void skalUtledeAutopunktVentPåMeldekort() {
        // Arrange
        YtelseDtoBuilder yb = YtelseDtoBuilder.oppdatere(Optional.empty());
        YtelseDto ytelse = yb.medYtelseType(FagsakYtelseType.DAGPENGER)
            .medPeriode(Intervall.fraOgMed(FPDateUtil.iDag().minusMonths(2)))
            .leggTilYtelseAnvist(lagYtelseAnvist(yb.getAnvistBuilder()))
            .build();

        LocalDate skjæringstidspunkt = FPDateUtil.iDag();
        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
            .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister(any())).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        List<BeregningAksjonspunktResultat> resultater = AksjonspunktUtlederFastsettBeregningsaktiviteter.utledAksjonspunkterForFelles(bgMedDagpenger, beregningAktivitetAggregat, lagMockBeregningsgrunnlagInput(false), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
            assertThat(resultat.getBeregningAksjonspunktDefinisjon()).isEqualTo(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT)
        );
    }

    private YtelseAnvistDto lagYtelseAnvist(YtelseAnvistDtoBuilder anvistBuilder) {
        return anvistBuilder.medAnvistPeriode(Intervall.fraOgMed(FPDateUtil.iDag().minusMonths(1))).build();
    }

    @Test
    public void skalUtledeAutopunktVentPåInntektFrilansNårManSkalVentePåBådeInntekterFrilansOgAAPMeldekort() {
        // Arrange
        int rapporteringsfrist = 1000;

        // Act
        List<BeregningAksjonspunktResultat> resultater = AksjonspunktUtlederFastsettBeregningsaktiviteter.utledAksjonspunkterForFelles(beregningsgrunnlag, beregningAktivitetAggregat, lagBeregningsgrunnlagInput(rapporteringsfrist), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
            assertThat(resultat.getBeregningAksjonspunktDefinisjon()).isEqualTo(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST)
        );
    }

    private BeregningsgrunnlagInput lagBeregningsgrunnlagInput(int rapporteringsfrist) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = new BeregningsgrunnlagInput(ref, getIAYGrunnlag(), opptjeningAktiviteter, null, List.of(), new ForeldrepengerGrunnlag(100, false));
        beregningsgrunnlagInput.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, rapporteringsfrist);
        return beregningsgrunnlagInput;
    }

    private BeregningsgrunnlagInput lagMockBeregningsgrunnlagInput(boolean toggleManglendeArbeidsforhold) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = new BeregningsgrunnlagInput(ref, getMockedIAYGrunnlag(), opptjeningAktiviteter, null, List.of(), new ForeldrepengerGrunnlag(100, false));
        return beregningsgrunnlagInput;
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

}
