package no.nav.folketrygdloven.kalkulus.beregning;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.dbstoette.JpaExtension;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;
import no.nav.vedtak.konfig.Tid;

@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class BeregningStegTjenesteTest extends EntityManagerAwareTest {
    private static LocalDate STP = LocalDate.of(2022,3,1);
    private static final String ORGNR = "99999999";

    @Inject
    private BeregningStegTjeneste beregningStegTjeneste;

    private KoblingRepository koblingRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private AvklaringsbehovRepository avklaringsbehovRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    @BeforeEach
    void beforeEach() {
        koblingRepository = new KoblingRepository(getEntityManager());
        beregningsgrunnlagRepository = new BeregningsgrunnlagRepository(getEntityManager());
        avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        avklaringsbehovTjeneste = new AvklaringsbehovTjeneste(avklaringsbehovRepository, koblingRepository);
    }

    @Test
    void skal_gi_aksjonspunkt_ventelønn_vartpenger_fastsett_aktiviteter() {
        // Arrange
        var kobling = lagKoblingRefEntitet();
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = lagYA(ORGNR, ref);
        var iay = ferdigstillIAY(Arrays.asList(yrkesaktivitet));

        var ventelønnOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.VENTELØNN_VARTPENGER, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)));
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)), ORGNR, null, ref);
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(ventelønnOpptjening, arbeidOpptjening));

        var input = new FastsettBeregningsaktiviteterInput(lagKoblingRefDto(kobling), iay, opptjening, Collections.emptyList(), null);
        var inputMedG = input.medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));
        inputMedG.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);

        // Act
        var resultat = beregningStegTjeneste.beregnFor(BeregningSteg.FASTSETT_STP_BER, inputMedG);

        // Assert
        var bg = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat, bg, BeregningsgrunnlagTilstand.OPPRETTET, true, 2, 0);
    }

    /**
     * Tester at vi kopierer gammelt bekreftet grunnlag når dette er mulig å gjøre basert på gammelt steg grunnlag og nytt steg grunnlag
     */
    @Test
    void skal_kopiere_gammelt_grunnlag_når_dette_er_mulig_fastsett_aktiviteter() {
        // Arrange
        var kobling = lagKoblingRefEntitet();
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = lagYA(ORGNR, ref);
        var iay = ferdigstillIAY(Arrays.asList(yrkesaktivitet));

        var ventelønnOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.VENTELØNN_VARTPENGER, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)));
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)), ORGNR, null, ref);
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(ventelønnOpptjening, arbeidOpptjening));

        var input = new StegProsesseringInput(BeregningsgrunnlagTilstand.OPPRETTET, lagKoblingRefDto(kobling), iay, opptjening, Collections.emptyList(), null);
        var stegInput = new FastsettBeregningsaktiviteterInput(input).medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));
        stegInput.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);

        // Act
        var resultat = beregningStegTjeneste.beregnFor(BeregningSteg.FASTSETT_STP_BER, stegInput);

        // Assert
        var bg = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat, bg, BeregningsgrunnlagTilstand.OPPRETTET, true, 2, 0);


        // Act 2
        avbrytAvklaringsBehov(kobling);
        var saksbehandletGrunnlag = lagreSaksbehandletFjernArbeidOgDeaktiver(kobling, bg);
        var inputMedTidligereGrunnlag = input.medForrigeGrunnlagFraSteg(BehandlingslagerTilKalkulusMapper.mapGrunnlag(bg.get())).medForrigeGrunnlagFraStegUt(BehandlingslagerTilKalkulusMapper.mapGrunnlag(saksbehandletGrunnlag));
        var stegInput2 = new FastsettBeregningsaktiviteterInput(inputMedTidligereGrunnlag).medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));
        var resultat2 = beregningStegTjeneste.beregnFor(BeregningSteg.FASTSETT_STP_BER, stegInput2);

        // Assert 2
        var bg2 = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat2, bg2, BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER, true, 2, 1);
    }

    @Test
    void skal_ikke_kopiere_når_vi_ikke_har_aksjonspunkt_fastsett_aktiviteter() {
        // Arrange
        var kobling = lagKoblingRefEntitet();
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = lagYA(ORGNR, ref);
        var iay = ferdigstillIAY(Arrays.asList(yrkesaktivitet));

        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)), ORGNR, null, ref);
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening));

        var input = new FastsettBeregningsaktiviteterInput(lagKoblingRefDto(kobling), iay, opptjening, Collections.emptyList(), null);
        var inputMedG = input.medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));
        inputMedG.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);

        // Act
        var resultat = beregningStegTjeneste.beregnFor(BeregningSteg.FASTSETT_STP_BER, inputMedG);

        // Assert
        var bg = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat, bg, BeregningsgrunnlagTilstand.OPPRETTET, false, 1, 0);

        // Act 2 kjør igjen
        var resultat2 = beregningStegTjeneste.beregnFor(BeregningSteg.FASTSETT_STP_BER, inputMedG);
        var bg2 = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat2, bg2, BeregningsgrunnlagTilstand.OPPRETTET, false, 1, 0);

    }

    @Test
    void skal_ikke_kopiere_grunnlag_ved_endringer_i_input_fastsett_aktiviteter() {
        // Arrange
        var kobling = lagKoblingRefEntitet();
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = lagYA(ORGNR, ref);
        var iay = ferdigstillIAY(Arrays.asList(yrkesaktivitet));

        var ventelønnOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.VENTELØNN_VARTPENGER, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)));
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)), ORGNR, null, ref);
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(ventelønnOpptjening, arbeidOpptjening));

        var input = new StegProsesseringInput(BeregningsgrunnlagTilstand.OPPRETTET, lagKoblingRefDto(kobling), iay, opptjening, Collections.emptyList(), null);
        var stegInput = new FastsettBeregningsaktiviteterInput(input).medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));
        stegInput.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);

        // Act
        var resultat = beregningStegTjeneste.beregnFor(BeregningSteg.FASTSETT_STP_BER, stegInput);

        // Assert
        var bg = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat, bg, BeregningsgrunnlagTilstand.OPPRETTET, true, 2, 0);

        // Act 2
        avbrytAvklaringsBehov(kobling);
        var saksbehandletGrunnlag = lagreSaksbehandletFjernArbeidOgDeaktiver(kobling, bg);

        var næring = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.NÆRING, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)));
        var opptjening2 = new OpptjeningAktiviteterDto(Arrays.asList(ventelønnOpptjening, arbeidOpptjening, næring));
        var input2 = new StegProsesseringInput(BeregningsgrunnlagTilstand.OPPRETTET, lagKoblingRefDto(kobling), iay, opptjening2, Collections.emptyList(), null);
        input2.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);
        var inputMedTidligereGrunnlag = input2.medForrigeGrunnlagFraSteg(BehandlingslagerTilKalkulusMapper.mapGrunnlag(bg.get())).medForrigeGrunnlagFraStegUt(BehandlingslagerTilKalkulusMapper.mapGrunnlag(saksbehandletGrunnlag));
        var stegInput2 = new FastsettBeregningsaktiviteterInput(inputMedTidligereGrunnlag).medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));
        var resultat2 = beregningStegTjeneste.beregnFor(BeregningSteg.FASTSETT_STP_BER, stegInput2);

        // Assert 2
        var bg2 = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat2, bg2, BeregningsgrunnlagTilstand.OPPRETTET, true, 3, 0);
    }

    @Test
    void skal_gi_aksjonspunkt_om_lønnsendring_vurder_fakta() {
        // Arrange
        var kobling = lagKoblingRefEntitet();
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = lagYA(ORGNR, ref);
        var iay = ferdigstillIAY(Arrays.asList(yrkesaktivitet));
        var bg = lagBG(ORGNR, false).build(BeregningsgrunnlagTilstand.OPPRETTET);

        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)), ORGNR, null, ref);
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening));

        var input = new BeregningsgrunnlagInput(lagKoblingRefDto(kobling), iay, opptjening, Collections.emptyList(), new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false));
        input.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);
        var inputMedBG = input.medBeregningsgrunnlagGrunnlag(bg);
        var faktaInput = new FaktaOmBeregningInput(new StegProsesseringInput(inputMedBG, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER))
                .medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));

        // Act
        var resultat = beregningStegTjeneste.beregnFor(BeregningSteg.KOFAKBER, faktaInput);

        // Assert
        var nyttBG = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat, nyttBG, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, true, 1, 0);
        assertThat(nyttBG).isPresent();
        var andeler = nyttBG.get().getBeregningsgrunnlag().get()
                .getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getAndelArbeidsforhold().get().getArbeidsforholdOrgnr()).isEqualTo(ORGNR);
    }

    @Test
    void skal_kopiere_gammelt_grunnlag_ved_tilbakerulling() {
        // Arrange
        var kobling = lagKoblingRefEntitet();
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = lagYA(ORGNR, ref);
        var iay = ferdigstillIAY(Arrays.asList(yrkesaktivitet));
        var bg = lagBG(ORGNR, false).build(BeregningsgrunnlagTilstand.OPPRETTET);

        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusMonths(1)), ORGNR, null, ref);
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening));


        var input = new BeregningsgrunnlagInput(lagKoblingRefDto(kobling), iay, opptjening, Collections.emptyList(), new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false));
        input.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);
        var inputMedBG = input.medBeregningsgrunnlagGrunnlag(bg);

        var faktaInput = new FaktaOmBeregningInput(new StegProsesseringInput(inputMedBG, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER))
                .medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));

        // Act
        var resultat1 = beregningStegTjeneste.beregnFor(BeregningSteg.KOFAKBER, faktaInput);

        // Assert
        var nyttBG = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat1, nyttBG, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, true, 1, 0);
        assertThat(nyttBG).isPresent();
        var andeler = nyttBG.get().getBeregningsgrunnlag().get()
                .getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getAndelArbeidsforhold().get().getArbeidsforholdOrgnr()).isEqualTo(ORGNR);

        // Act 2
        avbrytAvklaringsBehov(kobling);
        var bekreftetBG = lagBG(ORGNR, true, FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING).build(BeregningsgrunnlagTilstand.KOFAKBER_UT);
        var faktaInput2 = new FaktaOmBeregningInput(new StegProsesseringInput(inputMedBG, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)
                .medForrigeGrunnlagFraSteg(BehandlingslagerTilKalkulusMapper.mapGrunnlag(nyttBG.get())).medForrigeGrunnlagFraStegUt(bekreftetBG))
                .medGrunnbeløpInput(List.of(new GrunnbeløpInput(Tid.TIDENES_BEGYNNELSE, Tid.TIDENES_ENDE, 100000L, 100000L)));
        var resultat2 = beregningStegTjeneste.beregnFor(BeregningSteg.KOFAKBER, faktaInput2);

        // Assert 2
        var nyttBG2 = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        assertResultatOgAktiviteter(resultat2, nyttBG2, BeregningsgrunnlagTilstand.KOFAKBER_UT, true, 1, 0);
        assertThat(nyttBG2).isPresent();
        var andeler2 = nyttBG2.get().getBeregningsgrunnlag().get()
                .getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagAndelList();
        assertThat(andeler2).hasSize(1);
        assertThat(andeler2.get(0).getAndelArbeidsforhold().get().getArbeidsforholdOrgnr()).isEqualTo(ORGNR);

    }

    private void assertResultatOgAktiviteter(TilstandResponse resultat, Optional<BeregningsgrunnlagGrunnlagEntitet> bg, BeregningsgrunnlagTilstand tilstand, boolean medAksjonspunkt, int registerAktiviteter, int saksbehandlerAktiviteter) {
        if (medAksjonspunkt) {
            assertThat(resultat.getAvklaringsbehovMedTilstandDto()).hasSize(1);
        } else {
            assertThat(resultat.getAvklaringsbehovMedTilstandDto()).isEmpty();
        }
        assertThat(bg).isPresent();
        assertThat(bg.get().getBeregningsgrunnlagTilstand()).isEqualTo(tilstand);
        assertThat(bg.get().getRegisterAktiviteter().getAktiviteter()).hasSize(registerAktiviteter);
        assertThat(bg.get().getSaksbehandletAktiviteter().map(AktivitetAggregatEntitet::getAktiviteter).orElse(Collections.emptyList())).hasSize(saksbehandlerAktiviteter);
    }

    private void avbrytAvklaringsBehov(KoblingEntitet kobling) {
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehov(kobling.getId());
    }

    private BeregningsgrunnlagGrunnlagEntitet lagreSaksbehandletFjernArbeidOgDeaktiver(KoblingEntitet kobling, Optional<BeregningsgrunnlagGrunnlagEntitet> bgMedAktiviteter) {
        var saksbehandletGrunnlag = BeregningsgrunnlagGrunnlagBuilder.kopiere(bgMedAktiviteter)
                .medSaksbehandletAktiviteter(AktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .leggTilAktivitet(AktivitetEntitet.builder()
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.VENTELØNN_VARTPENGER)
                                .medPeriode(IntervallEntitet.fraOgMedTilOgMed(STP.minusYears(1),
                                        STP.plusMonths(1)))
                                .build())
                        .build());
        beregningsgrunnlagRepository.lagre(kobling.getId(), saksbehandletGrunnlag,
                BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        var saksbehandletBg = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId()).orElseThrow();
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(kobling.getId());
        return saksbehandletBg;
    }

    private KoblingEntitet lagKoblingRefEntitet() {
        AktørId aktørId = new AktørId("9999999999999");
        no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse koblingReferanse = new no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);

        koblingRepository.lagre(koblingEntitet);
        return koblingEntitet;
    }

    private InntektArbeidYtelseGrunnlagDto ferdigstillIAY(List<YrkesaktivitetDto> yrkesaktiviteter) {
        var arbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        yrkesaktiviteter.forEach(arbeidBuilder::leggTilYrkesaktivitet);
        arbeidBuilder.build();
        var iayBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        iayBuilder.leggTilAktørArbeid(arbeidBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medData(iayBuilder).build();
    }

    private KoblingReferanse lagKoblingRefDto(KoblingEntitet koblingEntitet) {
        var skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktOpptjening(STP).medFørsteUttaksdato(STP).build();
        return KoblingReferanse.fra(FagsakYtelseType.FORELDREPENGER, new no.nav.folketrygdloven.kalkulus.typer.AktørId("999999999999"), koblingEntitet.getId(), koblingEntitet.getKoblingReferanse().getReferanse(), Optional.empty(), skjæringstidspunkt);
    }

    private YrkesaktivitetDto lagYA(String orgnr, InternArbeidsforholdRefDto ref) {
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty()).medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr)).medArbeidsforholdId(ref).medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        var ansettelsesperiode = yaBuilder.getAktivitetsAvtaleBuilder()
                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusYears(1), STP.plusYears(1)))
                .medErAnsettelsesPeriode(true);
        var aktivitetsavtale = yaBuilder.getAktivitetsAvtaleBuilder()
                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusYears(1), STP.plusYears(1)))
                .medSisteLønnsendringsdato(STP.minusMonths(2))
                .medErAnsettelsesPeriode(false);
        yaBuilder.leggTilAktivitetsAvtale(ansettelsesperiode)
                .leggTilAktivitetsAvtale(aktivitetsavtale);
        return yaBuilder.build();
    }

    private BeregningsgrunnlagGrunnlagDtoBuilder lagBG(String orgnr, boolean medFaktaAggregat, FaktaOmBeregningTilfelle... tilfeller) {
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .medGrunnbeløp(Beløp.fra(99_000))
                .leggTilFaktaOmBeregningTilfeller(List.of(tilfeller))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(STP, null).build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsperiodeFom(STP.minusYears(1))
                        .medArbeidsperiodeTom(STP.plusYears(1))
                        .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr)))
                .build(periode);

        var grBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(empty());
        if (medFaktaAggregat) {
            var faktaBuilder = FaktaAggregatDto.builder();
            var arbeiddto = faktaBuilder.getFaktaArbeidsforholdBuilderFor(Arbeidsgiver.virksomhet(orgnr), InternArbeidsforholdRefDto.nullRef())
                    .medHarLønnsendringIBeregningsperiodenFastsattAvSaksbehandler(false)
                    .build();
            faktaBuilder.erstattEksisterendeEllerLeggTil(arbeiddto);
            grBuilder.medFaktaAggregat(faktaBuilder.build());
        }

        return grBuilder
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusYears(1), STP.plusYears(1)))
                                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .build())
                        .medSkjæringstidspunktOpptjening(STP)
                        .build());
    }



}
