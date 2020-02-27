package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulator.OpprettRefusjondatoerFraInntektsmeldinger.opprett;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.vedtak.util.Tuple;


public class BeregningsgrunnlagInputTestUtil {

    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";
    private static final String TOGGLE_SPLITTE_SAMMENLIGNING = "fpsak.splitteSammenligningATFL";

    private static Map<String, Boolean> toggles = new HashMap<>();

    static {
        toggles.put(TOGGLE_SPLITTE_SAMMENLIGNING, false);
    }


    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(BehandlingReferanse behandlingReferanse,
                                                                        Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand> aktivt,
                                                                        Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand>... forrige) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(behandlingReferanse, null, null, AktivitetGradering.INGEN_GRADERING, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(aktivt.getElement1(), aktivt.getElement2());
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilBeregningsgrunnlagIHistorikk(grunnlag, aktivt.getElement2());
        for (Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand> bg : forrige) {
            BeregningsgrunnlagGrunnlagDto gr = lagGrunnlag(bg.getElement1(), bg.getElement2());
            inputMedBeregningsgrunnlag.leggTilBeregningsgrunnlagIHistorikk(gr, bg.getElement2());
        }
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        return inputMedBeregningsgrunnlag;
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(BehandlingReferanse behandlingReferanse, BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(behandlingReferanse, null, null, AktivitetGradering.INGEN_GRADERING, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(beregningsgrunnlag, beregningsgrunnlagTilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilBeregningsgrunnlagIHistorikk(grunnlag, beregningsgrunnlagTilstand);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        return inputMedBeregningsgrunnlag;
    }

    private static BeregningsgrunnlagGrunnlagDto lagGrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlag).build(beregningsgrunnlagTilstand);
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(BehandlingReferanse behandlingReferanse, BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder, BeregningsgrunnlagTilstand tilstand) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(behandlingReferanse, null, null, AktivitetGradering.INGEN_GRADERING, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilBeregningsgrunnlagIHistorikk(grunnlag, tilstand);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        return inputMedBeregningsgrunnlag;
    }

    public static BeregningsgrunnlagInput lagInputMedIAYOgOpptjeningsaktiviteter(BehandlingReferanse behandlingReferanse,
                                                                                 OpptjeningAktiviteterDto opptjeningAktiviteterDto,
                                                                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad, int grunnbeløpMilitærHarKravPå) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(dekningsgrad, false);
        foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(grunnbeløpMilitærHarKravPå);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(behandlingReferanse, iayGrunnlag, opptjeningAktiviteterDto,
            AktivitetGradering.INGEN_GRADERING, opprett(behandlingReferanse, iayGrunnlag), foreldrepengerGrunnlag);
        input.setToggles(toggles);
        BeregningsgrunnlagInput inputMedGrunnbeløp = input.medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
        inputMedGrunnbeløp.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedGrunnbeløp;
    }

    public static BeregningsgrunnlagInput lagInputMedIAYOgOpptjeningsaktiviteterMedTogglePå(BehandlingReferanse behandlingReferanse,
                                                                                 OpptjeningAktiviteterDto opptjeningAktiviteterDto,
                                                                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad, int grunnbeløpMilitærHarKravPå) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(dekningsgrad, false);
        foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(grunnbeløpMilitærHarKravPå);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(behandlingReferanse, iayGrunnlag, opptjeningAktiviteterDto,
                AktivitetGradering.INGEN_GRADERING, opprett(behandlingReferanse, iayGrunnlag), foreldrepengerGrunnlag);
        input.leggTilToggle(TOGGLE_SPLITTE_SAMMENLIGNING, true);
        BeregningsgrunnlagInput inputMedGrunnbeløp = input.medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
        inputMedGrunnbeløp.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedGrunnbeløp;
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(BehandlingReferanse behandlingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, false);
        foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(2);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(behandlingReferanse, iayGrunnlag, null,
            AktivitetGradering.INGEN_GRADERING, opprett(behandlingReferanse, iayGrunnlag), foreldrepengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilBeregningsgrunnlagIHistorikk(grunnlag, tilstand);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        BeregningsgrunnlagInput inputMedGrunnbeløp = inputMedBeregningsgrunnlag.medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
        inputMedGrunnbeløp.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedGrunnbeløp;
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(BehandlingReferanse behandlingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                             SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(behandlingReferanse, iayGrunnlag, null,
            AktivitetGradering.INGEN_GRADERING, opprett(behandlingReferanse, iayGrunnlag), svangerskapspengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilBeregningsgrunnlagIHistorikk(grunnlag, tilstand);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        BeregningsgrunnlagInput inputMedGrunnbeløp = inputMedBeregningsgrunnlag.medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
        inputMedGrunnbeløp.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedGrunnbeløp;
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(BehandlingReferanse behandlingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                             Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(behandlingReferanse, iayGrunnlag, null,
            AktivitetGradering.INGEN_GRADERING, opprett(behandlingReferanse, iayGrunnlag, førsteInnsendingAvRefusjonMap), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilBeregningsgrunnlagIHistorikk(grunnlag, tilstand);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        return inputMedBeregningsgrunnlag;
    }

    public static void leggPåStandarGrunnBeløpPakke(BeregningsgrunnlagInput input) {
        input.getGrunnbeløpsatser().addAll(GrunnbeløpMock.GRUNNBELØPSATSER);
    }
}
