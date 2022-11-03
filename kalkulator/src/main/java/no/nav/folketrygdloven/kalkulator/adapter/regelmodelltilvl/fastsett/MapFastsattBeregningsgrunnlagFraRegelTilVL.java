package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.fastsett;


import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;

public class MapFastsattBeregningsgrunnlagFraRegelTilVL {


    public BeregningsgrunnlagDto mapFastsettBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag resultatGrunnlag,
                                                               BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        return map(resultatGrunnlag, eksisterendeVLGrunnlag);
    }

    private BeregningsgrunnlagDto map(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag resultatGrunnlag, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(eksisterendeVLGrunnlag).build();
        Objects.requireNonNull(resultatGrunnlag, "resultatGrunnlag");
        mapPerioder(nyttBeregningsgrunnlag, resultatGrunnlag.getBeregningsgrunnlagPerioder());
        return nyttBeregningsgrunnlag;
    }

    private void mapPerioder(BeregningsgrunnlagDto eksisterendeVLGrunnlag,
                             List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder) {

        int vlBGnummer = 0;
        for (var resultatBGPeriode : beregningsgrunnlagPerioder) {
            BeregningsgrunnlagPeriodeDto eksisterendePeriode = eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().get(vlBGnummer);
            for (BeregningsgrunnlagPrStatus regelAndel : resultatBGPeriode.getBeregningsgrunnlagPrStatus()) {
                if (regelAndel.getAndelNr() == null) {
                    mapAndelMedArbeidsforhold(eksisterendePeriode, regelAndel);
                } else {
                    mapAndel(eksisterendePeriode, regelAndel);
                }
            }
            vlBGnummer++;
            fastsettAgreggerteVerdier(eksisterendePeriode, eksisterendeVLGrunnlag);
        }
    }

    private static void mapAndel(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndel.getAndelNr().equals(bgpsa.getAndelsnr()))
                .forEach(resultatAndel -> mapBeregningsgrunnlagPrStatus(mappetPeriode, regelAndel, resultatAndel));
    }

    private void mapAndelMedArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold : regelAndel.getArbeidsforhold()) {
            mapEksisterendeAndelForArbeidsforhold(mappetPeriode, regelAndelForArbeidsforhold);
        }
    }

    private void mapEksisterendeAndelForArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode,
                                                       BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelOpt = mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndelForArbeidsforhold.getAndelNr().equals(bgpsa.getAndelsnr()))
                .findFirst();
        if (andelOpt.isPresent()) {
            BeregningsgrunnlagPrStatusOgAndelDto kalkulatorAndel = andelOpt.get();
            mapBeregningsgrunnlagPrStatusForATKombinert(mappetPeriode, kalkulatorAndel, regelAndelForArbeidsforhold);
        } else {
            throw new IllegalStateException("Forventer ikke ny andel fra fastsett beregning steg.");
        }
    }

    private static void fastsettAgreggerteVerdier(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        Optional<BigDecimal> bruttoPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> bgpsa.getBruttoPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .reduce(BigDecimal::add);
        Optional<BigDecimal> avkortetPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> bgpsa.getAvkortetPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getAvkortetPrÅr)
                .reduce(BigDecimal::add);
        Optional<BigDecimal> redusertPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> bgpsa.getRedusertPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getRedusertPrÅr)
                .reduce(BigDecimal::add);
        BeregningsgrunnlagPeriodeDto.oppdater(periode)
                .medBruttoPrÅr(bruttoPrÅr.orElse(null))
                .medAvkortetPrÅr(avkortetPrÅr.orElse(null))
                .medRedusertPrÅr(redusertPrÅr.orElse(null))
                .build(eksisterendeVLGrunnlag);
    }

    private void mapBeregningsgrunnlagPrStatusForATKombinert(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                             BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel,
                                                             BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold) {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(vlBGPAndel));
            settVerdierFraFastsettRegel(andelBuilder, regelArbeidsforhold);
            andelBuilder.build(vlBGPeriode);
    }

    protected static void settVerdierFraFastsettRegel(BeregningsgrunnlagPrStatusOgAndelDto.Builder builder, BeregningsgrunnlagPrArbeidsforhold regelResultat) {

        builder.medAvkortetPrÅr(verifisertBeløp(regelResultat.getAvkortetPrÅr()))
                .medRedusertPrÅr(verifisertBeløp(regelResultat.getRedusertPrÅr()))
                .medMaksimalRefusjonPrÅr(regelResultat.getMaksimalRefusjonPrÅr())
                .medAvkortetRefusjonPrÅr(regelResultat.getAvkortetRefusjonPrÅr())
                .medRedusertRefusjonPrÅr(regelResultat.getRedusertRefusjonPrÅr())
                .medAvkortetBrukersAndelPrÅr(verifisertBeløp(regelResultat.getAvkortetBrukersAndelPrÅr()))
                .medRedusertBrukersAndelPrÅr(verifisertBeløp(regelResultat.getRedusertBrukersAndelPrÅr()))
                .medAvkortetFørGraderingPrÅr(verifisertBeløp(regelResultat.getAndelsmessigFørGraderingPrAar() == null ? BigDecimal.ZERO : regelResultat.getAndelsmessigFørGraderingPrAar()));
    }

    private static BigDecimal verifisertBeløp(BigDecimal beløp) {
        return beløp == null ? null : beløp.max(BigDecimal.ZERO);
    }

    private static void mapBeregningsgrunnlagPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                      BeregningsgrunnlagPrStatus resultatBGPStatus,
                                                      BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatusOgAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(vlBGPStatusOgAndel));
        settVerdierFraFastsettRegel(builder, resultatBGPStatus);
        builder.build(vlBGPeriode);
    }

    private static void settVerdierFraFastsettRegel(BeregningsgrunnlagPrStatusOgAndelDto.Builder builder, BeregningsgrunnlagPrStatus regelResultat) {
        builder
                .medAvkortetPrÅr(verifisertBeløp(regelResultat.getAvkortetPrÅr()))
                .medRedusertPrÅr(verifisertBeløp(regelResultat.getRedusertPrÅr()))
                .medAvkortetBrukersAndelPrÅr(verifisertBeløp(regelResultat.getAvkortetPrÅr()))
                .medRedusertBrukersAndelPrÅr(verifisertBeløp(regelResultat.getRedusertPrÅr()))
                .medMaksimalRefusjonPrÅr(BigDecimal.ZERO)
                .medAvkortetRefusjonPrÅr(BigDecimal.ZERO)
                .medRedusertRefusjonPrÅr(BigDecimal.ZERO)
                .medAvkortetFørGraderingPrÅr(verifisertBeløp(regelResultat.getAndelsmessigFørGraderingPrAar() == null ? BigDecimal.ZERO : regelResultat.getAndelsmessigFørGraderingPrAar()));
    }

}
