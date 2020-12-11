package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulator.OpprettRefusjondatoerFraInntektsmeldinger.opprett;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.vedtak.util.Tuple;


public class BeregningsgrunnlagInputTestUtil {

    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";
    private static final String TOGGLE_SPLITTE_SAMMENLIGNING = "fpsak.splitteSammenligningATFL";

    private static Map<String, Boolean> toggles = new HashMap<>();

    static {
        toggles.put(TOGGLE_SPLITTE_SAMMENLIGNING, false);
    }


    @SafeVarargs
    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(KoblingReferanse koblingReferanse,
                                                                        Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand> aktivt,
                                                                        Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand>... forrige) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, null, AktivitetGradering.INGEN_GRADERING, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(aktivt.getElement1(), aktivt.getElement2());
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        for (Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand> bg : forrige) {
            BeregningsgrunnlagGrunnlagDto gr = lagGrunnlag(bg.getElement1(), bg.getElement2());
        }
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        return inputMedBeregningsgrunnlag;
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(KoblingReferanse koblingReferanse, BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, null, AktivitetGradering.INGEN_GRADERING, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(beregningsgrunnlag, beregningsgrunnlagTilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        return inputMedBeregningsgrunnlag;
    }

    private static BeregningsgrunnlagGrunnlagDto lagGrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlag).build(beregningsgrunnlagTilstand);
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(KoblingReferanse koblingReferanse, BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder, BeregningsgrunnlagTilstand tilstand) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, null, AktivitetGradering.INGEN_GRADERING, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        return inputMedBeregningsgrunnlag;
    }

    public static BeregningsgrunnlagInput lagInputMedIAYOgOpptjeningsaktiviteter(KoblingReferanse koblingReferanse,
                                                                                 OpptjeningAktiviteterDto opptjeningAktiviteterDto,
                                                                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad, int grunnbeløpMilitærHarKravPå) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(dekningsgrad, false);
        foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(grunnbeløpMilitærHarKravPå);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, opptjeningAktiviteterDto,
            AktivitetGradering.INGEN_GRADERING, opprett(koblingReferanse, iayGrunnlag), foreldrepengerGrunnlag);
        input.setToggles(toggles);
        input.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return input;
    }

    public static BeregningsgrunnlagInput lagInputMedIAYOgOpptjeningsaktiviteterMedTogglePå(KoblingReferanse koblingReferanse,
                                                                                            OpptjeningAktiviteterDto opptjeningAktiviteterDto,
                                                                                            InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad, int grunnbeløpMilitærHarKravPå) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(dekningsgrad, false);
        foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(grunnbeløpMilitærHarKravPå);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, opptjeningAktiviteterDto,
                AktivitetGradering.INGEN_GRADERING, opprett(koblingReferanse, iayGrunnlag), foreldrepengerGrunnlag);
        input.leggTilToggle(TOGGLE_SPLITTE_SAMMENLIGNING, true);
        input.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return input;
    }

    public static ForeslåBeregningsgrunnlagInput lagForeslåttBeregningsgrunnlagInput(KoblingReferanse koblingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, false);
        foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(2);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
                AktivitetGradering.INGEN_GRADERING, opprett(koblingReferanse, iayGrunnlag), foreldrepengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return new ForeslåBeregningsgrunnlagInput(new StegProsesseringInput(inputMedBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT))
                .medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(KoblingReferanse koblingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, false);
        foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(2);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
            AktivitetGradering.INGEN_GRADERING, opprett(koblingReferanse, iayGrunnlag), foreldrepengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagGrunnlag(KoblingReferanse koblingReferanse,
                                                                                BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                                BeregningsgrunnlagTilstand tilstand) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, false);
        foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(2);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, null,
                AktivitetGradering.INGEN_GRADERING, Collections.emptyList(), foreldrepengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }


    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(KoblingReferanse koblingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                             SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
            AktivitetGradering.INGEN_GRADERING, opprett(koblingReferanse, iayGrunnlag), svangerskapspengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }

    public static ForeslåBeregningsgrunnlagInput lagForeslåttBeregningsgrunnlagInput(KoblingReferanse koblingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                             OmsorgspengerGrunnlag omsorgspengerGrunnlag) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
                AktivitetGradering.INGEN_GRADERING, opprett(koblingReferanse, iayGrunnlag), omsorgspengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return new ForeslåBeregningsgrunnlagInput(new StegProsesseringInput(inputMedBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT))
                .medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(KoblingReferanse koblingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                             Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
            AktivitetGradering.INGEN_GRADERING, opprett(koblingReferanse, iayGrunnlag, førsteInnsendingAvRefusjonMap), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        inputMedBeregningsgrunnlag.setToggles(toggles);
        return inputMedBeregningsgrunnlag;
    }

}
