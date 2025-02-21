package no.nav.folketrygdloven.kalkulus.forvaltning;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.konfig.Konfigverdier;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.GrunnbeløpReguleringStatus;

public class Greguleringsstatusutleder {
    private static final Set<AktivitetStatus> SN_REGULERING = Set.of(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, AktivitetStatus.KOMBINERT_AT_SN,
            AktivitetStatus.KOMBINERT_FL_SN, AktivitetStatus.KOMBINERT_AT_FL_SN);


    private Greguleringsstatusutleder() {
        // SKjuler default
    }

    public static GrunnbeløpReguleringStatus utledStatus(Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                                         BigDecimal nyttGrunnbeløp,
                                                         FagsakYtelseType ytelse) {
        Optional<BeregningsgrunnlagEntitet> bgOpt = beregningsgrunnlagGrunnlagEntitet.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
        if (bgOpt.isEmpty() || beregningsgrunnlagGrunnlagEntitet.map(gr -> gr.getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT)).orElse(false)) {
            return GrunnbeløpReguleringStatus.IKKE_VURDERT;
        }
        BigDecimal grunnbeløpBenyttetIBeregningen = bgOpt.map(BeregningsgrunnlagEntitet::getGrunnbeløp).map(Beløp::getVerdi).orElse(BigDecimal.ZERO);
        if (måGreguleres(nyttGrunnbeløp, ytelse, bgOpt.get(), grunnbeløpBenyttetIBeregningen)) {
            return GrunnbeløpReguleringStatus.NØDVENDIG;
        }
        return GrunnbeløpReguleringStatus.IKKE_NØDVENDIG;
    }

    private static boolean måGreguleres(BigDecimal nyttGrunnbeløp,
                                        FagsakYtelseType ytelse,
                                        BeregningsgrunnlagEntitet bg,
                                        BigDecimal grunnbeløpBenyttetIBeregningen) {
        if (grunnbeløpBenyttetIBeregningen.compareTo(nyttGrunnbeløp) == 0) {
            return false;
        }
        Konfigverdier konfigverdier = KonfigTjeneste.forYtelse(ytelse);
        if (harGrunnlagSomBleAvkortet(bg, grunnbeløpBenyttetIBeregningen)) {
            return true;
        }
        if (erMilitærUnderMinstekravForMilitær(bg, konfigverdier, nyttGrunnbeløp)) {
            return true;
        }
        if (erBeregnetSomNæringsdrivende(bg)) {
            return true;
        }
        return false;
    }

    private static boolean harGrunnlagSomBleAvkortet(BeregningsgrunnlagEntitet bg, BigDecimal grunnbeløpBenyttetIBeregningen) {
        Beløp størsteBrutto = bg.getBeregningsgrunnlagPerioder().stream()
                .map(BeregningsgrunnlagPeriodeEntitet::getBruttoPrÅr)
                .max(Comparator.naturalOrder())
                .orElse(Beløp.ZERO);
        BigDecimal antallGØvreGrenseverdi = KonfigTjeneste.getAntallGØvreGrenseverdi();
        BigDecimal grenseverdi = antallGØvreGrenseverdi.multiply(grunnbeløpBenyttetIBeregningen);
        return størsteBrutto.compareTo(new Beløp(grenseverdi)) > 0;

    }

    private static boolean erMilitærUnderMinstekravForMilitær(BeregningsgrunnlagEntitet bg, Konfigverdier konfigverdier, BigDecimal nyG) {
        return bg.getBeregningsgrunnlagPerioder().stream()
                .flatMap(p -> p.getBeregningsgrunnlagAndelList().stream())
                .anyMatch(a -> a.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                        && (a.getBeregningsgrunnlagPeriode().getBruttoPrÅr().getVerdi()
                        .compareTo(konfigverdier.antallGMilitærHarKravPå().multiply(nyG))) < 0);
    }

    private static boolean erBeregnetSomNæringsdrivende(BeregningsgrunnlagEntitet bg) {
        return bg.getAktivitetStatuser().stream()
                .map(BeregningsgrunnlagAktivitetStatusEntitet::getAktivitetStatus)
                .anyMatch(SN_REGULERING::contains);
    }

}
