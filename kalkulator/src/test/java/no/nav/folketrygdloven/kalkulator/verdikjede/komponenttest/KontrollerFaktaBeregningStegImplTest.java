package no.nav.folketrygdloven.kalkulator.verdikjede.komponenttest;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaBeregningStegImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.DECEMBER, 23);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);

    @Inject
    private BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste;

    @Test
    public void skal_kunne_opprette_kombinerte_aksjonpunkter_med_tidsbegrenset_atfl_i_samme_org_nyoppstartet_fl_lønnsendring() {
        // Arrange
        LocalDate stp = SKJÆRINGSTIDSPUNKT_OPPTJENING;
        String orgnr = "915933149";
        String orgnr2 = "974760673";
        String orgnr3 = "971032081";
        InternArbeidsforholdRefDto arbId = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto arbId2 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto arbId3 = InternArbeidsforholdRefDto.nyRef();

        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

        // opptjeningaktiviteter
        var periode = Periode.of(stp.minusMonths(12), stp);
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, periode, orgnr),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, periode, orgnr2),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, periode, orgnr3),
            OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS, periode),
            OpptjeningAktiviteterDto.nyPeriodeAktør(OpptjeningAktivitetType.SYKEPENGER, periode, behandlingReferanse.getAktørId().getId())));

        BeregningIAYTestUtil.leggTilOppgittOpptjeningForFL(true, List.of(Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5))), iayGrunnlagBuilder);

        // Legg til IAY aktiviteter
        leggTilAT(arbId, orgnr, iayGrunnlagBuilder);
        leggTilTidsbegrenset(arbId2, orgnr2, iayGrunnlagBuilder);
        leggTilFrilans(arbId2, orgnr2, iayGrunnlagBuilder);
        leggTilATMedLønnsendring(arbId3, orgnr3, iayGrunnlagBuilder);

        // Act 1
        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(behandlingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);
        var resultat1 = doStegFastsettSkjæringstidspunkt(input);
        BeregningsgrunnlagInput utvidet = utvidMedBeregningsgrunnlagGrunnlagDto(input, resultat1.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);

        // Assert
        assertThat(resultat1.getBeregningAksjonspunktResultater()).isEmpty();

        // Act 2
        var resultat2 = doStegKontrollerFaktaBeregning(utvidet);
        utvidMedBeregningsgrunnlagGrunnlagDto(utvidet, resultat2.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Assert
        assertThat(resultat2.getBeregningAksjonspunktResultater().stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon)).containsExactly(BeregningAksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        var bg = resultat2.getBeregningsgrunnlag();

        // sjekk at vi har trigget på flere tilfeller
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(
            FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON,
            FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD,
            FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL,
            FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING,
            FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto bgPeriode = bg.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(4);
        assertThat(andeler.stream().filter(andel -> andel.getAktivitetStatus().erFrilanser()).count()).isEqualTo(1);
        assertArbeidstakerAndeler(List.of(orgnr, orgnr2, orgnr3), andeler);
    }

    @Test
    public void skal_kunne_opprette_kombinerte_aksjonpunkter_med_SN_ny_i_arbeidslivet_nyoppstartet_fl() {
        // Arrange
        LocalDate stp = SKJÆRINGSTIDSPUNKT_OPPTJENING;
        InternArbeidsforholdRefDto arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "915933149";
        String orgnr2 = "974760673";
        String orgnr3 = "971032081";

        // opptjeningaktiviteter
        var periode = Periode.of(stp.minusMonths(12), stp);
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, periode, orgnr),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.FRILANS, periode, orgnr2),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.NÆRING, periode, orgnr3),
            OpptjeningAktiviteterDto.nyPeriodeAktør(OpptjeningAktivitetType.SYKEPENGER, periode, behandlingReferanse.getAktørId().getId())));

        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

        BeregningIAYTestUtil.leggTilOppgittOpptjeningForFLOgSN(stp, true, true, iayGrunnlagBuilder);

        // legg til IAY aktiviteter
        leggTilAT(arbId, orgnr, iayGrunnlagBuilder);

        // Act
        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(behandlingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);
        var resultat1 = doStegFastsettSkjæringstidspunkt(input);
        BeregningsgrunnlagInput utvidet = utvidMedBeregningsgrunnlagGrunnlagDto(input, resultat1.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);

        // Assert
        assertThat(resultat1.getBeregningAksjonspunktResultater()).isEmpty();

        // Act
        var resultat = doStegKontrollerFaktaBeregning(utvidet);
        utvidMedBeregningsgrunnlagGrunnlagDto(utvidet, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);

        // Assert
        assertThat(resultat.getBeregningAksjonspunktResultater().stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon)).containsExactly(BeregningAksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        var bg = resultat.getBeregningsgrunnlag();

        // Assert - har fått riktig tilfeller utledet
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(
            FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL,
            FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET,
            FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto bgPeriode = bg.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(3);
        assertThat(andeler.stream().filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende()).count()).isEqualTo(1);
        assertArbeidstakerAndeler(List.of(orgnr), andeler);
    }

    private void leggTilAT(InternArbeidsforholdRefDto arbId, String orgnr, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
    }

    private void leggTilATMedLønnsendring(InternArbeidsforholdRefDto arbId, String orgnr, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, Arbeidsgiver.virksomhet(orgnr), Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2L)), iayGrunnlagBuilder);
    }

    private void leggTilFrilans(InternArbeidsforholdRefDto arbId2, String orgnr2, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), arbId2, Arbeidsgiver.virksomhet(orgnr2),
            ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER, singletonList(BigDecimal.TEN), false, Optional.empty(), iayGrunnlagBuilder);
    }

    private void leggTilTidsbegrenset(InternArbeidsforholdRefDto arbId2, String orgnr2, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), arbId2, Arbeidsgiver.virksomhet(orgnr2), iayGrunnlagBuilder);
    }

    private BeregningResultatAggregat doStegKontrollerFaktaBeregning(BeregningsgrunnlagInput input) {
        return kontrollerFaktaBeregningsgrunnlag(input);
    }

    private BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        return beregningsgrunnlagTjeneste.kontrollerFaktaBeregningsgrunnlag(input);
    }

    private BeregningResultatAggregat doStegFastsettSkjæringstidspunkt(BeregningsgrunnlagInput input) {
        return beregningsgrunnlagTjeneste.fastsettBeregningsaktiviteter(input);
    }

    private static void assertArbeidstakerAndeler(List<String> orgnrs, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        orgnrs.forEach(
            orgnr -> assertThat(andeler.stream().filter(andel -> andel.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsgiver)
                .map(Arbeidsgiver::getOrgnr)
                .filter(orgnr::equals)
                .isPresent()).count()).isEqualTo(1));
    }


    private BeregningsgrunnlagInput utvidMedBeregningsgrunnlagGrunnlagDto(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto grunnlagDto, BeregningsgrunnlagTilstand tilstand) {
        BeregningsgrunnlagInput newInput = input.medBeregningsgrunnlagGrunnlag(grunnlagDto);
        newInput.leggTilBeregningsgrunnlagIHistorikk(grunnlagDto, tilstand);
        return newInput;
    }

    private BeregningsgrunnlagInput lagInput(BehandlingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad) {
        return BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(ref, opptjeningAktiviteter, iayGrunnlag, dekningsgrad, 2);
    }

}
