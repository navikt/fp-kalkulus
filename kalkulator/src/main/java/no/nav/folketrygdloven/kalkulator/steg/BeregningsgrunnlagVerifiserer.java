package no.nav.folketrygdloven.kalkulator.steg;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.ytelse.fp.GraderingUtenBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public final class BeregningsgrunnlagVerifiserer {

    private BeregningsgrunnlagVerifiserer() {}

    public static void verifiserOppdatertBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
        Objects.requireNonNull(beregningsgrunnlag.getSkjæringstidspunkt(), "Skjæringstidspunkt");
        verifiserIkkeTomListe(beregningsgrunnlag.getBeregningsgrunnlagPerioder(), "BeregningsgrunnlagPerioder");
        verifiserIkkeTomListe(beregningsgrunnlag.getAktivitetStatuser(), "Aktivitetstatuser");
        verfiserBeregningsgrunnlagPerioder(beregningsgrunnlag);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(p -> verfiserBeregningsgrunnlagAndeler(p, BeregningsgrunnlagVerifiserer::verifiserOpprettetAndel));
    }

    private static void verfiserBeregningsgrunnlagPerioder(BeregningsgrunnlagDto beregningsgrunnlag) {
        List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (int i = 0; i < beregningsgrunnlagPerioder.size(); i++) {
            BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlagPerioder.get(i);
            Objects.requireNonNull(periode.getBeregningsgrunnlagPeriodeFom(), "BeregningsgrunnlagperiodeFom");
            verifiserIkkeTomListe(periode.getBeregningsgrunnlagPrStatusOgAndelList(), "BeregningsgrunnlagPrStatusOgAndelList");
            if (i > 0) {
                verifiserIkkeTomListe(periode.getPeriodeÅrsaker(), "PeriodeÅrsaker");
            }
        }
    }

    private static void verfiserBeregningsgrunnlagAndeler(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriodeDto, Consumer<BeregningsgrunnlagPrStatusOgAndelDto> verifiserAndel) {
        beregningsgrunnlagPeriodeDto.getBeregningsgrunnlagPrStatusOgAndelList().forEach(verifiserAndel);
    }

    private static void verifiserOpprettetAndel(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        Objects.requireNonNull(andel.getAktivitetStatus(), "Aktivitetstatus " + andel.toString());
        Objects.requireNonNull(andel.getAndelsnr(), "Andelsnummer " + andel.toString());
        Objects.requireNonNull(andel.getArbeidsforholdType(), "ArbeidsforholdType " + andel.toString());
        if (andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)) {
            if (andel.getArbeidsforholdType().equals(OpptjeningAktivitetType.ARBEID)) {
                verifiserOptionalPresent(andel.getBgAndelArbeidsforhold(), "BgAndelArbeidsforhold " + andel.toString());
            }
            Objects.requireNonNull(andel.getBeregningsperiodeFom(), "BeregningsperiodeFom " + andel.toString());
            Objects.requireNonNull(andel.getBeregningsperiodeTom(), "BeregningsperiodeTom " + andel.toString());
            if (andel.getBgAndelArbeidsforhold().isPresent()) {
                BGAndelArbeidsforholdDto arbFor = andel.getBgAndelArbeidsforhold().get();
                Objects.requireNonNull(arbFor.getArbeidsperiodeFom(), "arbeidsperiodeFom " + andel.toString());
                Objects.requireNonNull(arbFor.getArbeidsperiodeTom(), "arbeidsperiodeTom " + andel.toString());
            }
        }
    }

    public static void verifiserForeslåttBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
        verifiserOppdatertBeregningsgrunnlag(beregningsgrunnlag);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(p -> verfiserBeregningsgrunnlagAndeler(p , lagVerifiserForeslåttAndelConsumer(p)));
        SammenligningsgrunnlagDto sg = beregningsgrunnlag.getSammenligningsgrunnlag();
        if (sg != null) {
            Objects.requireNonNull(sg.getRapportertPrÅr(), "RapportertPrÅr");
            Objects.requireNonNull(sg.getAvvikPromilleNy(), "AvvikPromille");
            Objects.requireNonNull(sg.getSammenligningsperiodeFom(), "SammenligningsperiodeFom");
            Objects.requireNonNull(sg.getSammenligningsperiodeTom(), "SammenligningsperiodeTom");
        }
    }

    private static void verifiserFordeltBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
        verifiserOppdatertBeregningsgrunnlag(beregningsgrunnlag);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(p -> verfiserBeregningsgrunnlagAndeler(p, BeregningsgrunnlagVerifiserer::verifiserFordeltAndel));
        SammenligningsgrunnlagDto sg = beregningsgrunnlag.getSammenligningsgrunnlag();
        if (sg != null) {
            Objects.requireNonNull(sg.getRapportertPrÅr(), "RapportertPrÅr");
            Objects.requireNonNull(sg.getAvvikPromilleNy(), "AvvikPromille");
            Objects.requireNonNull(sg.getSammenligningsperiodeFom(), "SammenligningsperiodeFom");
            Objects.requireNonNull(sg.getSammenligningsperiodeTom(), "SammenligningsperiodeTom");
        }
    }

    public static void verifiserFastsattBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        verifiserFordeltBeregningsgrunnlag(beregningsgrunnlag);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(p -> verfiserBeregningsgrunnlagAndeler(p, BeregningsgrunnlagVerifiserer::verifiserFastsattAndel));
        if (ytelsespesifiktGrunnlag instanceof ForeldrepengerGrunnlag) {
            verifiserAtAndelerSomGraderesHarGrunnlag(beregningsgrunnlag, ((ForeldrepengerGrunnlag) ytelsespesifiktGrunnlag).getAktivitetGradering());
        }
    }

    private static void verifiserAtAndelerSomGraderesHarGrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, AktivitetGradering aktivitetGradering) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedGraderingUtenBG = GraderingUtenBeregningsgrunnlagTjeneste.finnAndelerMedGraderingUtenBG(beregningsgrunnlag, aktivitetGradering);
        if (!andelerMedGraderingUtenBG.isEmpty()) {
            throw BeregningsgrunnlagVerifisererFeil.FEILFACTORY.graderingUtenBG(andelerMedGraderingUtenBG.get(0).getAktivitetStatus().getKode()).toException();
        }
    }

    private static Consumer<BeregningsgrunnlagPrStatusOgAndelDto> lagVerifiserForeslåttAndelConsumer(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        return (BeregningsgrunnlagPrStatusOgAndelDto andel) -> {
            Objects.requireNonNull(andel.getInntektskategori(), "Inntektskategori");
            if (andel.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)) {
                if (!periodeOppståttGrunnetGradering(beregningsgrunnlagPeriode.getPeriodeÅrsaker())) {
                    Objects.requireNonNull(andel.getPgiSnitt(), "PgiSnitt");
                    Objects.requireNonNull(andel.getPgi1(), "PgiÅr1");
                    Objects.requireNonNull(andel.getPgi2(), "PgiÅr2");
                    Objects.requireNonNull(andel.getPgi3(), "PgiÅr3");
                }
            } else if (!andel.getArbeidsforholdType().equals(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE)) {
                LocalDate bgPeriodeFom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom();
                String andelBeskrivelse = "andel" + andel.getAktivitetStatus() + " i perioden fom " + bgPeriodeFom;
                Objects.requireNonNull(andel.getBruttoPrÅr(), "BruttoPrÅr er null for " + andelBeskrivelse);
                Objects.requireNonNull(andel.getBeregnetPrÅr(), "beregnetPrÅr er null for " + andelBeskrivelse);
            }
            if (andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER) || andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)) {
                Objects.requireNonNull(andel.getÅrsbeløpFraTilstøtendeYtelse(), "ÅrsbeløpFraTilstøtendeYtelse");
                Objects.requireNonNull(andel.getOrginalDagsatsFraTilstøtendeYtelse(), "originalDagsatsFraTilstøtendeYtelse");
            }
        };
    }

    private static boolean periodeOppståttGrunnetGradering(List<PeriodeÅrsak> periodeÅrsaker) {
        return periodeÅrsaker.stream().anyMatch(p -> p.equals(PeriodeÅrsak.GRADERING));
    }

    private static void verifiserFordeltAndel(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        Objects.requireNonNull(andel.getInntektskategori(), "Inntektskategori");
        if (!andel.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE) && !andel.getArbeidsforholdType().equals(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE)) {
            Objects.requireNonNull(andel.getBruttoPrÅr(), "BruttoPrÅr");
            if (andel.getBeregnetPrÅr() == null) {
                Objects.requireNonNull(andel.getFordeltPrÅr(), "fordeltPrÅr");
            }
        }
        if (andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER) || andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)) {
            Objects.requireNonNull(andel.getÅrsbeløpFraTilstøtendeYtelse(), "ÅrsbeløpFraTilstøtendeYtelse");
            Objects.requireNonNull(andel.getOrginalDagsatsFraTilstøtendeYtelse(), "originalDagsatsFraTilstøtendeYtelse");
        }
    }


    private static void verifiserFastsattAndel(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        Objects.requireNonNull(andel.getBruttoPrÅr(), "BruttoPrÅr");
        Objects.requireNonNull(andel.getAvkortetPrÅr(), "AvkortetPrÅr");
        Objects.requireNonNull(andel.getRedusertPrÅr(), "RedusertPrÅr");
        Objects.requireNonNull(andel.getAvkortetBrukersAndelPrÅr(), "AvkortetBrukersAndelPrÅr");
        Objects.requireNonNull(andel.getRedusertBrukersAndelPrÅr(), "RedusertBrukersAndelPrÅr");
        Objects.requireNonNull(andel.getDagsatsBruker(), "DagsatsBruker");
        Objects.requireNonNull(andel.getMaksimalRefusjonPrÅr(), "MaksimalRefusjonPrÅr");
        Objects.requireNonNull(andel.getAvkortetRefusjonPrÅr(), "AvkortetRefusjonPrÅr");
        Objects.requireNonNull(andel.getRedusertRefusjonPrÅr(), "RedusertRefusjonPrÅr");
        Objects.requireNonNull(andel.getDagsatsArbeidsgiver(), "DagsatsArbeidsgiver");
    }


    private static void verifiserIkkeTomListe(Collection<?> liste, String obj) {
        Objects.requireNonNull(liste, "Liste");
        if (liste.isEmpty()) {
            throw BeregningsgrunnlagVerifisererFeil.FEILFACTORY.verifiserIkkeTomListeFeil(obj).toException();
        }
    }

    private static void verifiserOptionalPresent(Optional<?> opt, String obj) {
        Objects.requireNonNull(opt, "Optional");
        if (!opt.isPresent()) {
            throw BeregningsgrunnlagVerifisererFeil.FEILFACTORY.verifiserOptionalPresentFeil(obj).toException();
        }
    }

    private interface BeregningsgrunnlagVerifisererFeil extends DeklarerteFeil {
        BeregningsgrunnlagVerifisererFeil FEILFACTORY = FeilFactory.create(BeregningsgrunnlagVerifisererFeil.class);

        @TekniskFeil(feilkode = "FT-370742", feilmelding = "Postcondition feilet: Beregningsgrunnlag i ugyldig tilstand etter steg. Listen %s er tom, men skulle ikke vært det.", logLevel = LogLevel.ERROR)
        Feil verifiserIkkeTomListeFeil(String obj);

        @TekniskFeil(feilkode = "FT-370743", feilmelding = "Postcondition feilet: Beregningsgrunnlag i ugyldig tilstand etter steg. Optional %s er ikke present, men skulle ha vært det.", logLevel = LogLevel.ERROR)
        Feil verifiserOptionalPresentFeil(String obj);

        @TekniskFeil(feilkode = "FT-370746", feilmelding = "Det mangler beregningsgrunnlag på en andel som skal graderes, ugyldig tilstand. Gjelder andel med status %s", logLevel = LogLevel.WARN)
        Feil graderingUtenBG(String andelStatus);

    }
}
