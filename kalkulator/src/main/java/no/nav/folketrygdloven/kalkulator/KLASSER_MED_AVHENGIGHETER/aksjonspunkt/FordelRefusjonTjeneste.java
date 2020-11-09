package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.fordeling.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.fordeling.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class FordelRefusjonTjeneste {

    private FordelRefusjonTjeneste() {
        // Skjul
    }

    /**
     * Lager map for å fordele refusjon mellom andeler i periode
     *
     * @param input    beregningsgrunnlagInput
     * @param fordeltPeriode periode fra dto
     * @param korrektPeriode periode fra beregningsgrunnlag
     * @return Map fra andel til refusjonsbeløp
     */
    static Map<FordelBeregningsgrunnlagAndelDto, BigDecimal> getRefusjonPrÅrMap(BeregningsgrunnlagInput input,
                                                                                FordelBeregningsgrunnlagPeriodeDto fordeltPeriode,
                                                                                BeregningsgrunnlagPeriodeDto korrektPeriode) {
        Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> beløpMap = getTotalbeløpPrArbeidsforhold(input, fordeltPeriode, korrektPeriode);
        Map<FordelBeregningsgrunnlagAndelDto, BigDecimal> refusjonMap = new HashMap<>();
        fordeltPeriode.getAndeler()
            .stream()
            .filter(a -> a.getArbeidsgiverId() != null)
            .forEach(fordeltAndel -> {
                var arbeidsforhold = getKorrektArbeidsforhold(input, fordeltAndel);
                fordelRefusjonTilAndel(beløpMap, refusjonMap, fordeltAndel, arbeidsforhold);
            });
        return refusjonMap;
    }

    private static void fordelRefusjonTilAndel(Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> beløpMap,
                                               Map<FordelBeregningsgrunnlagAndelDto, BigDecimal> refusjonMap,
                                               FordelBeregningsgrunnlagAndelDto fordeltAndel,
                                               BGAndelArbeidsforholdDto arbeidsforhold) {
        RefusjonOgFastsattBeløp refusjonOgFastsattBeløp = beløpMap.get(arbeidsforhold);
        if (refusjonOgFastsattBeløp.getTotalFastsattBeløpPrÅr().compareTo(BigDecimal.ZERO) == 0 ||
            refusjonOgFastsattBeløp.getTotalRefusjonPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            if (fordeltAndel.getFastsatteVerdier().getRefusjonPrÅr() != null) {
                refusjonMap.put(fordeltAndel, BigDecimal.valueOf(fordeltAndel.getFastsatteVerdier().getRefusjonPrÅr()));
            }
            return;
        }
        BigDecimal refusjonPrÅr = getAndelAvTotalRefusjonPrÅr(fordeltAndel, refusjonOgFastsattBeløp);
        refusjonMap.put(fordeltAndel, refusjonPrÅr);
    }

    private static BigDecimal getAndelAvTotalRefusjonPrÅr(FordelBeregningsgrunnlagAndelDto fordeltAndel,
                                                          RefusjonOgFastsattBeløp refusjonOgFastsattBeløp) {
        int fastsatt = fordeltAndel.getFastsatteVerdier().finnEllerUtregnFastsattBeløpPrÅr().intValue();
        BigDecimal totalFastsatt = refusjonOgFastsattBeløp.getTotalFastsattBeløpPrÅr();
        BigDecimal totalRefusjon = refusjonOgFastsattBeløp.getTotalRefusjonPrÅr();
        return totalRefusjon.multiply(BigDecimal.valueOf(fastsatt))
            .divide(totalFastsatt, 10, RoundingMode.HALF_UP);
    }

    private static Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> getTotalbeløpPrArbeidsforhold(BeregningsgrunnlagInput input,
                                                                                                        FordelBeregningsgrunnlagPeriodeDto fordeltPeriode,
                                                                                                        BeregningsgrunnlagPeriodeDto korrektPeriode) {
        Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap = new HashMap<>();
        fordeltPeriode.getAndeler()
            .stream()
            .filter(a -> a.getArbeidsgiverId() != null)
            .forEach(fordeltAndel -> {
                leggTilRefusjon(input, korrektPeriode, arbeidsforholdRefusjonMap, fordeltAndel);
                leggTilFastsattFordeling(input, arbeidsforholdRefusjonMap, fordeltAndel);
            });
        return arbeidsforholdRefusjonMap;
    }

    private static void leggTilFastsattFordeling(BeregningsgrunnlagInput input,
                                                 Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                 FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        BGAndelArbeidsforholdDto korrektArbeidsforhold = getKorrektArbeidsforhold(input, fordeltAndel);
        BigDecimal fastsattBeløpPrÅr = fordeltAndel.getFastsatteVerdier().finnEllerUtregnFastsattBeløpPrÅr();
        settEllerOppdaterFastsattBeløp(arbeidsforholdRefusjonMap, korrektArbeidsforhold, fastsattBeløpPrÅr);
    }

    private static void settEllerOppdaterFastsattBeløp(Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                       BGAndelArbeidsforholdDto arbeidsforhold, BigDecimal fastsattBeløpPrÅr) {
        if (arbeidsforholdRefusjonMap.containsKey(arbeidsforhold)) {
            RefusjonOgFastsattBeløp nyttBeløp = arbeidsforholdRefusjonMap.get(arbeidsforhold)
                .leggTilFastsattBeløp(fastsattBeløpPrÅr);
            arbeidsforholdRefusjonMap.put(arbeidsforhold, nyttBeløp);
        } else {
            arbeidsforholdRefusjonMap.put(arbeidsforhold, new RefusjonOgFastsattBeløp(BigDecimal.ZERO, fastsattBeløpPrÅr));
        }
    }

    private static void leggTilRefusjon(BeregningsgrunnlagInput input, BeregningsgrunnlagPeriodeDto korrektPeriode, Map<BGAndelArbeidsforholdDto,
        RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap, FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        if (fordeltAndel.getFastsatteVerdier().getRefusjonPrÅr() == null) {
            leggTilForKunEndretFordeling(korrektPeriode, arbeidsforholdRefusjonMap, fordeltAndel);
        } else {
            leggTilForEndretFordelingOgRefusjon(input, arbeidsforholdRefusjonMap, fordeltAndel);
        }
    }

    private static void leggTilForEndretFordelingOgRefusjon(BeregningsgrunnlagInput input,
                                                            Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                            FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        var korrektArbeidsforhold = getKorrektArbeidsforhold(input, fordeltAndel);
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(fordeltAndel.getFastsatteVerdier().getRefusjonPrÅr());
        settEllerOppdaterTotalRefusjon(arbeidsforholdRefusjonMap, korrektArbeidsforhold, refusjonskravPrÅr);
    }

    private static void leggTilForKunEndretFordeling(BeregningsgrunnlagPeriodeDto korrektPeriode,
                                                     Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap, FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        if (!fordeltAndel.erLagtTilAvSaksbehandler()) {
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> korrektAndelOpt = korrektPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> andel.getAndelsnr().equals(fordeltAndel.getAndelsnr())).findFirst();
            korrektAndelOpt.ifPresent(korrektAndel ->
                leggTilRefusjonForAndelIGrunnlag(arbeidsforholdRefusjonMap, korrektAndel)
            );
        }
    }

    private static void leggTilRefusjonForAndelIGrunnlag(Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap, BeregningsgrunnlagPrStatusOgAndelDto korrektAndel) {
        korrektAndel.getBgAndelArbeidsforhold().ifPresent(arbeidsforhold -> {
            BigDecimal refusjonskravPrÅr = arbeidsforhold.getGjeldendeRefusjonPrÅr() == null ? BigDecimal.ZERO : arbeidsforhold.getGjeldendeRefusjonPrÅr();
            settEllerOppdaterTotalRefusjon(arbeidsforholdRefusjonMap, arbeidsforhold, refusjonskravPrÅr);
        });
    }

    private static void settEllerOppdaterTotalRefusjon(Map<BGAndelArbeidsforholdDto, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                       BGAndelArbeidsforholdDto arbeidsforhold, BigDecimal refusjonskravPrÅr) {
        if (arbeidsforholdRefusjonMap.containsKey(arbeidsforhold)) {
            RefusjonOgFastsattBeløp nyttBeløp = arbeidsforholdRefusjonMap.get(arbeidsforhold)
                .leggTilRefusjon(refusjonskravPrÅr);
            arbeidsforholdRefusjonMap.put(arbeidsforhold, nyttBeløp);
        } else {
            arbeidsforholdRefusjonMap.put(arbeidsforhold, new RefusjonOgFastsattBeløp(refusjonskravPrÅr));
        }
    }

    private static BGAndelArbeidsforholdDto getKorrektArbeidsforhold(BeregningsgrunnlagInput input, FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        var arbeidsforholdId = fordeltAndel.getArbeidsforholdId();
        var arbeidsgiverId = fordeltAndel.getArbeidsgiverId();
        return MatchBeregningsgrunnlagTjeneste.matchArbeidsforholdIAktivtGrunnlag(input, arbeidsgiverId, arbeidsforholdId)
            .orElseThrow(() -> FordelRefusjonTjenesteFeil.FACTORY.fantIkkeArbeidsforhold(arbeidsgiverId, arbeidsforholdId.getReferanse()).toException());
    }

    private interface FordelRefusjonTjenesteFeil extends DeklarerteFeil {

        FordelRefusjonTjenesteFeil FACTORY = FeilFactory.create(FordelRefusjonTjenesteFeil.class);

        @TekniskFeil(feilkode = "FT-401711", feilmelding = "Fant ikke bgAndelArbeidsforhold for arbeidsgiverId %s og arbeidsforholdId %s", logLevel = LogLevel.WARN)
        Feil fantIkkeArbeidsforhold(String arbeidsgiverId, String arbeidsforholdId);
    }

}
