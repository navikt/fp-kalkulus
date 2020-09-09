package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;


public class AksjonspunktUtlederFordelBeregningTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MARCH, 23);

    private BeregningAktivitetAggregatDto.Builder beregningAktivitetBuilder = BeregningAktivitetAggregatDto.builder()
        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);


    @Test
    public void skal_ikke_lage_aksjonspunkt_dersom_det_ikke_er_endret_bg() {
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlagutenNyttArbeidsforhold();

        List<BeregningAksjonspunktResultat> aksjonspunktResultats = utledAksjonspunkter(koblingReferanse, grunnlag);

        assertThat(aksjonspunktResultats).isEmpty();
    }

    private List<BeregningAksjonspunktResultat> utledAksjonspunkter(KoblingReferanse ref, BeregningsgrunnlagGrunnlagDto grunnlag) {
        List<BeregningAksjonspunktResultat> aksjonspunktResultats = AksjonspunktUtlederFordelBeregning.utledAksjonspunkterFor(ref, grunnlag, AktivitetGradering.INGEN_GRADERING, List.of());
        return aksjonspunktResultats;
    }

    @Test
    public void skal_lage_aksjonspunkt_når_det_er_endring() {
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlagMedNyttArbeidsforhold();

        List<BeregningAksjonspunktResultat> aksjonspunktResultats = utledAksjonspunkter(koblingReferanse, grunnlag);

        assertThat(aksjonspunktResultats).hasSize(1);
        assertThat(aksjonspunktResultats.get(0).getBeregningAksjonspunktDefinisjon()).isEqualTo(BeregningAksjonspunktDefinisjon.FORDEL_BEREGNINGSGRUNNLAG);
    }



    private BeregningsgrunnlagGrunnlagDto lagGrunnlagMedNyttArbeidsforhold(FaktaOmBeregningTilfelle... tilfeller) {
        List<FaktaOmBeregningTilfelle> listeMedTilfeller = Arrays.asList(tilfeller);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(BigDecimal.valueOf(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
            .leggTilFaktaOmBeregningTilfeller(listeMedTilfeller)
            .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("1234534")))
            .build(periode);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(beregningAktivitetBuilder.build())
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        return grunnlag;
    }

    private BeregningsgrunnlagGrunnlagDto lagGrunnlagutenNyttArbeidsforhold(FaktaOmBeregningTilfelle... tilfeller) {
        List<FaktaOmBeregningTilfelle> listeMedTilfeller = Arrays.asList(tilfeller);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(BigDecimal.valueOf(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
            .leggTilFaktaOmBeregningTilfeller(listeMedTilfeller)
            .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet("1234534");
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
            .build(periode);

        beregningAktivitetBuilder.leggTilAktivitet(BeregningAktivitetDto.builder()
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2)))
            .medArbeidsgiver(virksomhet)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef()).build());

        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(beregningAktivitetBuilder.build())
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        return grunnlag;
    }




}
