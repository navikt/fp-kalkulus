package no.nav.folketrygdloven.kalkulus.kopiering;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.k9.felles.util.Tuple;

class BeregningsgrunnlagDiffSjekker {

    private BeregningsgrunnlagDiffSjekker() {
        // Skjul
    }

    static boolean harSignifikantDiffIAktiviteter(BeregningAktivitetAggregatDto aktivt, BeregningAktivitetAggregatDto forrige) {
        if (aktivt.getBeregningAktiviteter().size() != forrige.getBeregningAktiviteter().size()) {
            return true;
        }
        if (!aktivt.getBeregningAktiviteter().containsAll(forrige.getBeregningAktiviteter())) {
            return true;
        }
        if (!aktivt.getSkjæringstidspunktOpptjening().equals(forrige.getSkjæringstidspunktOpptjening())) {
            return true;
        }
        return false;
    }


    static boolean harSignifikantDiffIBeregningsgrunnlag(BeregningsgrunnlagDto aktivt, BeregningsgrunnlagDto forrige) {
        if (harDiffIFelterPåBeregningsgrunnlag(aktivt, forrige)) {
            return true;
        }

        List<BeregningsgrunnlagPeriodeDto> aktivePerioder = aktivt.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrige.getBeregningsgrunnlagPerioder();
        return harPeriodeDiff(aktivePerioder, forrigePerioder);
    }

    static Set<Intervall> finnPerioderUtenDiff(BeregningsgrunnlagDto aktivt, BeregningsgrunnlagDto forrige) {
        if (harDiffIFelterPåBeregningsgrunnlag(aktivt, forrige)){
            return Set.of();
        }
        List<BeregningsgrunnlagPeriodeDto> aktivePerioder = aktivt.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrige.getBeregningsgrunnlagPerioder();
        return finnPerioderUtenDiff(aktivePerioder, forrigePerioder);
    }

    private static boolean harDiffIFelterPåBeregningsgrunnlag(BeregningsgrunnlagDto aktivt, BeregningsgrunnlagDto forrige) {
        if (!erLike(aktivt.getGrunnbeløp() == null ? null : aktivt.getGrunnbeløp().getVerdi(), forrige.getGrunnbeløp() == null ? null : forrige.getGrunnbeløp().getVerdi())) {
            return true;
        }
        if (!hentStatuser(aktivt).equals(hentStatuser(forrige))) {
            return true;
        }
        if (!aktivt.getSkjæringstidspunkt().equals(forrige.getSkjæringstidspunkt())) {
            return true;
        }
        if (aktivt.getFaktaOmBeregningTilfeller().size() != forrige.getFaktaOmBeregningTilfeller().size() || !aktivt.getFaktaOmBeregningTilfeller().containsAll(forrige.getFaktaOmBeregningTilfeller())) {
            return true;
        }
        if (harSammenligningsgrunnlagDiff(aktivt.getSammenligningsgrunnlag(), forrige.getSammenligningsgrunnlag())) {
            return true;
        }

        if (harSammenligningsgrunnlagPrStatusDiff(aktivt.getSammenligningsgrunnlagPrStatusListe(), forrige.getSammenligningsgrunnlagPrStatusListe())) {
            return true;
        }
        return false;
    }


    private static boolean harSammenligningsgrunnlagDiff(SammenligningsgrunnlagDto aktivt, SammenligningsgrunnlagDto forrige) {
        if (aktivt == null || forrige == null) {
            return !Objects.equals(aktivt, forrige);
        }
        if (!erLike(aktivt.getAvvikPromilleNy(), forrige.getAvvikPromilleNy())) {
            return true;
        }
        if (!erLike(aktivt.getRapportertPrÅr(), forrige.getRapportertPrÅr())) {
            return true;
        }
        if (!Objects.equals(aktivt.getSammenligningsperiodeFom(), forrige.getSammenligningsperiodeFom())) {
            return true;
        }
        if (!Objects.equals(aktivt.getSammenligningsperiodeTom(), forrige.getSammenligningsperiodeTom())) {
            return true;
        }
        return false;
    }

    private static boolean harSammenligningsgrunnlagPrStatusDiff(List<SammenligningsgrunnlagPrStatusDto> aktivt, List<SammenligningsgrunnlagPrStatusDto> forrige) {
        if (aktivt.isEmpty() != forrige.isEmpty()) {
            return true;
        }

        if (!inneholderLikeSammenligningstyper(aktivt, forrige)) {
            return true;
        }

        for (SammenligningsgrunnlagPrStatusDto aktivSgPrStatus : aktivt) {
            SammenligningsgrunnlagPrStatusDto forrigeSgPrStatus = forrige.stream().filter(s -> aktivSgPrStatus.getSammenligningsgrunnlagType().equals(s.getSammenligningsgrunnlagType())).findFirst().get();

            if (!erLike(aktivSgPrStatus.getAvvikPromilleNy(), forrigeSgPrStatus.getAvvikPromilleNy())) {
                return true;
            }
            if (!erLike(aktivSgPrStatus.getRapportertPrÅr(), forrigeSgPrStatus.getRapportertPrÅr())) {
                return true;
            }
            if (!Objects.equals(aktivSgPrStatus.getSammenligningsperiodeFom(), forrigeSgPrStatus.getSammenligningsperiodeFom())) {
                return true;
            }
            if (!Objects.equals(aktivSgPrStatus.getSammenligningsperiodeTom(), forrigeSgPrStatus.getSammenligningsperiodeTom())) {
                return true;
            }
        }
        return false;
    }

    private static boolean harPeriodeDiff(List<BeregningsgrunnlagPeriodeDto> aktivePerioder, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        if (aktivePerioder.size() != forrigePerioder.size()) {
            return true;
        }
        // begge listene er sorter på fom dato så det er mulig å benytte indeks her
        for (int i = 0; i < aktivePerioder.size(); i++) {
            BeregningsgrunnlagPeriodeDto aktivPeriode = aktivePerioder.get(i);
            BeregningsgrunnlagPeriodeDto forrigePeriode = forrigePerioder.get(i);
            if (erDiffMellomToPerioder(aktivPeriode, forrigePeriode)) {
                return true;
            }
        }
        return false;
    }

    private static boolean erDiffMellomToPerioder(BeregningsgrunnlagPeriodeDto aktivPeriode, BeregningsgrunnlagPeriodeDto forrigePeriode) {
        if (!aktivPeriode.getBeregningsgrunnlagPeriodeFom().equals(forrigePeriode.getBeregningsgrunnlagPeriodeFom())) {
            return true;
        }
        if (!aktivPeriode.getBeregningsgrunnlagPeriodeTom().equals(forrigePeriode.getBeregningsgrunnlagPeriodeTom())) {
            return true;
        }
        if (!erLike(aktivPeriode.getBruttoPrÅr(), forrigePeriode.getBruttoPrÅr())) {
            return true;
        }
        Tuple<List<BeregningsgrunnlagPrStatusOgAndelDto>, List<BeregningsgrunnlagPrStatusOgAndelDto>> resultat = finnAndeler(aktivPeriode, forrigePeriode);
        if (resultat.getElement1().size() != resultat.getElement2().size()) {
            return true;
        }
        if (sjekkAndeler(resultat.getElement1(), resultat.getElement2())) {
            return true;
        }
        return false;
    }

    private static Set<Intervall> finnPerioderUtenDiff(List<BeregningsgrunnlagPeriodeDto> aktivePerioder, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        var perioderUtenDiff = new HashSet<Intervall>();
        // begge listene er sorter på fom dato så det er mulig å benytte indeks her
        int i = 0;
        while (i < forrigePerioder.size() && i < aktivePerioder.size() && !erDiffMellomToPerioder(aktivePerioder.get(i), forrigePerioder.get(i))) {
                perioderUtenDiff.add(aktivePerioder.get(i).getPeriode());
                i++;
        }
        return perioderUtenDiff;
    }

    private static boolean sjekkAndeler(List<BeregningsgrunnlagPrStatusOgAndelDto> aktiveAndeler, List<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndeler) {
        for (BeregningsgrunnlagPrStatusOgAndelDto aktivAndel : aktiveAndeler) {
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndelOpt = forrigeAndeler
                    .stream().filter(a -> a.getAndelsnr().equals(aktivAndel.getAndelsnr()))
                    .findFirst();
            if (forrigeAndelOpt.isEmpty()) {
                return true;
            }
            BeregningsgrunnlagPrStatusOgAndelDto forrigeAndel = forrigeAndelOpt.get();
            if (harAndelDiff(aktivAndel, forrigeAndel)) {
                return true;
            }
        }
        return false;
    }

    private static boolean harAndelDiff(BeregningsgrunnlagPrStatusOgAndelDto aktivAndel, BeregningsgrunnlagPrStatusOgAndelDto forrigeAndel) {
        if (!aktivAndel.getAktivitetStatus().equals(forrigeAndel.getAktivitetStatus())) {
            return true;
        }
        if (hvisArbforManglerHosKunEn(aktivAndel, forrigeAndel)) {
            return true;
        }

        Optional<BGAndelArbeidsforholdDto> aktivArbeidsforhold = aktivAndel.getBgAndelArbeidsforhold();
        Optional<BGAndelArbeidsforholdDto> forrigeArbeidsforhold = forrigeAndel.getBgAndelArbeidsforhold();

        if (aktivArbeidsforhold.isPresent() && forrigeArbeidsforhold.isPresent()) {
            return aktivArbeidsforholdFørerTilDiff(aktivArbeidsforhold.get(), forrigeArbeidsforhold.get());
        }
        if (!aktivAndel.getInntektskategori().equals(forrigeAndel.getInntektskategori())) {
            return true;
        }
        if (!erLike(aktivAndel.getBruttoPrÅr(), forrigeAndel.getBruttoPrÅr())) {
            return true;
        }
        return false;
    }

    private static boolean hvisArbforManglerHosKunEn(BeregningsgrunnlagPrStatusOgAndelDto aktivAndel, BeregningsgrunnlagPrStatusOgAndelDto forrigeAndel) {
        return aktivAndel.getBgAndelArbeidsforhold().isPresent() != forrigeAndel.getBgAndelArbeidsforhold().isPresent();
    }

    private static boolean aktivArbeidsforholdFørerTilDiff(BGAndelArbeidsforholdDto aktivArbeidsforhold, BGAndelArbeidsforholdDto forrigeArbeidsforhold) {
        if (!aktivArbeidsforhold.getArbeidsgiver().equals(forrigeArbeidsforhold.getArbeidsgiver())) {
            return true;
        }
        if (!erLike(aktivArbeidsforhold.getRefusjonskravPrÅr(), forrigeArbeidsforhold.getRefusjonskravPrÅr())) {
            return true;
        }
        if (!erLike(aktivArbeidsforhold.getSaksbehandletRefusjonPrÅr(), forrigeArbeidsforhold.getSaksbehandletRefusjonPrÅr())) {
            return true;
        }
        if (!erLike(aktivArbeidsforhold.getFordeltRefusjonPrÅr(), forrigeArbeidsforhold.getFordeltRefusjonPrÅr())) {
            return true;
        }
        return false;
    }

    private static boolean inneholderLikeSammenligningstyper(List<SammenligningsgrunnlagPrStatusDto> aktivt, List<SammenligningsgrunnlagPrStatusDto> forrige) {
        EnumSet<SammenligningsgrunnlagType> sammenligningsgrunnlagTyper = EnumSet.allOf(SammenligningsgrunnlagType.class);

        for (SammenligningsgrunnlagType sgType : sammenligningsgrunnlagTyper) {
            if (forrige.stream().anyMatch(s -> sgType.equals(s.getSammenligningsgrunnlagType())) !=
                    aktivt.stream().anyMatch(s -> sgType.equals(s.getSammenligningsgrunnlagType()))) {
                return false;
            }
        }
        return true;
    }

    private static List<AktivitetStatus> hentStatuser(BeregningsgrunnlagDto aktivt) {
        return aktivt.getAktivitetStatuser().stream().map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus).collect(Collectors.toList());
    }

    private static Tuple<List<BeregningsgrunnlagPrStatusOgAndelDto>, List<BeregningsgrunnlagPrStatusOgAndelDto>> finnAndeler(BeregningsgrunnlagPeriodeDto aktivPeriode, BeregningsgrunnlagPeriodeDto forrigePeriode) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> aktiveAndeler = aktivPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        List<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndeler = forrigePeriode
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> !a.erLagtTilAvSaksbehandler())
                .collect(Collectors.toList());
        return new Tuple<>(aktiveAndeler, forrigeAndeler);
    }

    private static boolean erLike(BigDecimal verdi1, BigDecimal verdi2) {
        return verdi1 == null && verdi2 == null || verdi1 != null && verdi2 != null && verdi1.compareTo(verdi2) == 0;
    }
}
