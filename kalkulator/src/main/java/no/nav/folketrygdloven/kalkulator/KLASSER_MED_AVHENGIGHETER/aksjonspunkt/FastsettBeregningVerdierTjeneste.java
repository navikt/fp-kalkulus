package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;

public class FastsettBeregningVerdierTjeneste {

    private FastsettBeregningVerdierTjeneste() {
        // skjul
    }

    public static void fastsettVerdierForAndel(RedigerbarAndelFaktaOmBeregningDto andel,
                                               FastsatteVerdierDto fastsatteVerdier,
                                               BeregningsgrunnlagPeriodeDto periode,
                                               Optional<BeregningsgrunnlagPeriodeDto> periodeForrigeGrunnlag) {
        validerAtPåkrevdeVerdierErSatt(andel);
        if (andel.getNyAndel() || andel.getLagtTilAvSaksbehandler()) {
            if (andel.getAktivitetStatus().isPresent()) {
                fastsettBeløpForNyAndelMedAktivitetstatus(periode, andel.getAktivitetStatus().get(), fastsatteVerdier);
            } else {
                fastsettBeløpForAndelLagtTilAvSaksbehandlerFraAndelsreferanse(andel, periode, periodeForrigeGrunnlag, fastsatteVerdier);
            }
        } else {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder korrektAndel = getKorrektAndel(periode, periodeForrigeGrunnlag, andel);
            settInntektskategoriOgFastsattBeløp(andel, fastsatteVerdier, korrektAndel, periode);
        }
    }

    private static void validerAtPåkrevdeVerdierErSatt(RedigerbarAndelFaktaOmBeregningDto andel) {
        if (andel.getAndelsnr().isEmpty() && andel.getAktivitetStatus().isEmpty()) {
            throw new IllegalArgumentException("Enten andelsnr eller aktivitetstatus må vere satt.");
        }
        if (andel.getAktivitetStatus().isPresent() && !andel.getNyAndel()) {
            throw new IllegalArgumentException("Kun nye andeler kan identifiseres med aktivitetstatus");
        }
        if (!andel.getLagtTilAvSaksbehandler() && !andel.getNyAndel() && andel.getAndelsnr().isEmpty()) {
            throw new IllegalArgumentException("Eksisterende andeler som ikkje er lagt til av saksbehandler må ha andelsnr.");
        }
    }

    private static void fastsettBeløpForAndelLagtTilAvSaksbehandlerFraAndelsreferanse(RedigerbarAndelFaktaOmBeregningDto andel,
                                                                                      BeregningsgrunnlagPeriodeDto periode,
                                                                                      Optional<BeregningsgrunnlagPeriodeDto> periodeForrigeGrunnlag,
                                                                                      FastsatteVerdierDto fastsatteVerdier) {
        if (andel.getAndelsnr().isEmpty()) {
            throw new IllegalStateException("Må ha andelsnr for å fastsette beløp fra andelsnr");
        }
        Long andelsnr = andel.getAndelsnr().get();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder korrektAndel;
        if (!andel.getNyAndel() && periodeForrigeGrunnlag.isPresent()) {
            korrektAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier(MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriodePåAndelsnr(periodeForrigeGrunnlag.get(), andelsnr));
        } else {
            korrektAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier(MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriodePåAndelsnr(periode, andelsnr));
        }
        settInntektskategoriOgFastsattBeløp(andel, fastsatteVerdier, korrektAndel, periode);
    }

    private static void settInntektskategoriOgFastsattBeløp(RedigerbarAndelFaktaOmBeregningDto andel,
                                                            FastsatteVerdierDto fastsatteVerdier,
                                                            BeregningsgrunnlagPrStatusOgAndelDto.Builder korrektAndel,
                                                            BeregningsgrunnlagPeriodeDto korrektPeriode) {
        Inntektskategori nyInntektskategori = fastsatteVerdier.getInntektskategori();
        if (nyInntektskategori != null) {
            korrektAndel.medInntektskategori(nyInntektskategori);
        }
        korrektAndel
            .medBeregnetPrÅr(fastsatteVerdier.finnEllerUtregnFastsattBeløpPrÅr())
            .medBesteberegningPrÅr(Boolean.TRUE.equals(fastsatteVerdier.getSkalHaBesteberegning()) ? fastsatteVerdier.finnEllerUtregnFastsattBeløpPrÅr() : null)
            .medFastsattAvSaksbehandler(true);
        if (fastsatteVerdier.getRefusjonPrÅr() != null) {
            BGAndelArbeidsforholdDto.Builder builder = korrektAndel.getBgAndelArbeidsforholdDtoBuilder()
                .medRefusjonskravPrÅr(BigDecimal.valueOf(fastsatteVerdier.getRefusjonPrÅr()));
            korrektAndel.medBGAndelArbeidsforhold(builder);
        }
        if (andel.getNyAndel() || andel.getLagtTilAvSaksbehandler()) {
            korrektAndel.nyttAndelsnr(korrektPeriode).medLagtTilAvSaksbehandler(true).build(korrektPeriode);
        }
    }


    private static void fastsettBeløpForNyAndelMedAktivitetstatus(BeregningsgrunnlagPeriodeDto periode,
                                                                  AktivitetStatus aktivitetStatus,
                                                                  FastsatteVerdierDto fastsatteVerdier) {
        BigDecimal fastsatt = fastsatteVerdier.finnEllerUtregnFastsattBeløpPrÅr();// NOSONAR
        Inntektskategori nyInntektskategori = fastsatteVerdier.getInntektskategori();
        if (nyInntektskategori == null) {
            throw new IllegalStateException("Kan ikke sette inntektskategori lik null på ny andel.");
        }
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(aktivitetStatus)
            .medInntektskategori(nyInntektskategori)
            .medBeregnetPrÅr(fastsatt)
            .medBesteberegningPrÅr(Boolean.TRUE.equals(fastsatteVerdier.getSkalHaBesteberegning()) ? fastsatt : null)
            .medFastsattAvSaksbehandler(true)
            .medLagtTilAvSaksbehandler(true)
            .build(periode);
    }


    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder getKorrektAndel(BeregningsgrunnlagPeriodeDto periode,
                                                                        Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode,
                                                                        RedigerbarAndelFaktaOmBeregningDto andel) {
        if (andel.getAndelsnr().isEmpty()) {
            throw new IllegalArgumentException("Har ikke andelsnr når man burde ha hatt det.");
        }
        Long andelsnr = andel.getAndelsnr().get();
        if (andel.getLagtTilAvSaksbehandler() && !andel.getNyAndel() && forrigePeriode.isPresent()) {
            return BeregningsgrunnlagPrStatusOgAndelDto.kopier(MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriodePåAndelsnr(forrigePeriode.get(), andelsnr));
        }
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriodePåAndelsnr(periode, andelsnr));
    }

}
