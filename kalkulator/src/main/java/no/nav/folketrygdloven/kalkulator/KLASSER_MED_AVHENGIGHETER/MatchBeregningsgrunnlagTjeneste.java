package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

/**
 * Tjeneste som finner andeler basert på informasjon om andelen (arbeidsforholdId, andelsnr)
 */
public class MatchBeregningsgrunnlagTjeneste {

    private MatchBeregningsgrunnlagTjeneste() {
        // Skjul
    }


    public static Optional<BeregningsgrunnlagPeriodeDto> finnOverlappendePeriodeOmKunEnFinnes(BeregningsgrunnlagPeriodeDto periode,
                                                                                              Optional<BeregningsgrunnlagDto> forrigeGrunnlag) {
        List<BeregningsgrunnlagPeriodeDto> matchedePerioder = forrigeGrunnlag.map(bg ->
            bg.getBeregningsgrunnlagPerioder().stream()
            .filter(periodeIGjeldendeGrunnlag -> periode.getPeriode()
                .overlapper(periodeIGjeldendeGrunnlag.getPeriode())).collect(Collectors.toList())).orElse(Collections.emptyList());
        if (matchedePerioder.size() == 1) {
            return Optional.of(matchedePerioder.get(0));
        }
        return Optional.empty();
    }


    public static BeregningsgrunnlagPeriodeDto finnPeriodeIBeregningsgrunnlag(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag) {

        if (periode.getBeregningsgrunnlagPeriodeFom().isBefore(gjeldendeBeregningsgrunnlag.getSkjæringstidspunkt())) {
            return gjeldendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .min(Comparator.comparing(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom))
                .orElseThrow(() -> new IllegalStateException("Fant ingen perioder i beregningsgrunnlag."));
        }

        return gjeldendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .filter(bgPeriode -> inkludererBeregningsgrunnlagPeriodeDato(bgPeriode, periode.getBeregningsgrunnlagPeriodeFom()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Finner ingen korresponderende periode i det fastsatte grunnlaget"));
    }

    private static boolean inkludererBeregningsgrunnlagPeriodeDato(BeregningsgrunnlagPeriodeDto periode, LocalDate dato) {
        return !periode.getBeregningsgrunnlagPeriodeFom().isAfter(dato) && !periode.getBeregningsgrunnlagPeriodeTom().isBefore(dato);
    }


    /**
     * Matcher andel fra periode først basert på andelsnr. Om dette gir eit funn returneres andelen. Om dette ikkje
     * gir eit funn matches det på arbeidsforholdId. Om dette ikkje gir eit funn kastes exception.
     *
     * @param periode          beregningsgrunnlagperiode der man leter etter en andel basert på andelsnr og arbeidsforholdId
     * @param andelsnr         andelsnr til andelen det letes etter
     * @param arbeidsforholdId arbeidsforholdId til arbeidsforholdet som andelen er knyttet til
     * @return andel som matcher oppgitt informasjon, ellers kastes exception
     */
    public static BeregningsgrunnlagPrStatusOgAndelDto matchMedAndelFraPeriode(BeregningsgrunnlagPeriodeDto periode, Long andelsnr, InternArbeidsforholdRefDto arbeidsforholdId) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchetAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAndelsnr().equals(andelsnr))
            .findFirst();
        return matchetAndel.orElseGet(() -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .filter(arbeidsforholdRef -> arbeidsforholdRef.gjelderForSpesifiktArbeidsforhold()
                    && arbeidsforholdRef.gjelderFor(arbeidsforholdId))
                .isPresent()
            )
            .findFirst()
            .orElseThrow(() -> MatchBeregningsgrunnlagTjenesteFeil.FACTORY.finnerIkkeAndelFeil().toException()));
    }


    /**
     * Matcher andel fra periode først basert på andelsnr. Om dette gir eit funn returneres andelen. Om dette ikkje gir eit funn kastes exception.
     *
     * @param periode    beregningsgrunnlagperiode der man leter etter en andel basert på andelsnr og arbeidsforholdId
     * @param andelsnr   andelsnr til andelen det letes etter
     * @return andel som matcher oppgitt informasjon, ellers kastes exception
     */
    public static BeregningsgrunnlagPrStatusOgAndelDto matchMedAndelFraPeriodePåAndelsnr(BeregningsgrunnlagPeriodeDto periode, Long andelsnr) {
        return matchMedAndelFraPeriodePåAndelsnrOmFinnes(periode, andelsnr)
            .orElseThrow(() -> MatchBeregningsgrunnlagTjenesteFeil.FACTORY.finnerIkkeAndelFeil().toException());
    }


    /**
     * Matcher andel fra periode først basert på andelsnr. Om dette gir eit funn returneres andelen.
     *
     * @param periode    beregningsgrunnlagperiode der man leter etter en andel basert på andelsnr og arbeidsforholdId
     * @param andelsnr   andelsnr til andelen det letes etter
     * @return andel som matcher oppgitt informasjon
     */
    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchMedAndelFraPeriodePåAndelsnrOmFinnes(BeregningsgrunnlagPeriodeDto periode, Long andelsnr) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAndelsnr().equals(andelsnr))
            .findFirst();
    }



    /**
     * Matcher arbeidsforhold i siste beregningsgrunnlag med som ble lagret i steg,
     *
     * @param input      input for beregning som har beregningsgrunnlag med tilhørende beregningsgrunnlagperiode
     * @param arbeidsforholdId arbeidsforholdId til arbeidsforholdet som andelen er knyttet til
     * @return andel som matcher oppgitt informasjon, ellers kastes exception
     */
    public static Optional<BGAndelArbeidsforholdDto> matchArbeidsforholdIAktivtGrunnlag(BeregningsgrunnlagInput input,
                                                                                        String arbeidsgiverId,
                                                                                        InternArbeidsforholdRefDto arbeidsforholdId) {

        return input.getBeregningsgrunnlag()
            .getBeregningsgrunnlagPerioder()
            .stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .flatMap(arbeidsforhold -> arbeidsforhold.getBgAndelArbeidsforhold().stream())
            .filter(a -> a.getArbeidsgiver().getIdentifikator().equals(arbeidsgiverId) && a.getArbeidsforholdRef().gjelderFor(arbeidsforholdId))
            .findFirst();
    }

    public interface MatchBeregningsgrunnlagTjenesteFeil extends DeklarerteFeil {

        MatchBeregningsgrunnlagTjeneste.MatchBeregningsgrunnlagTjenesteFeil FACTORY = FeilFactory.create(MatchBeregningsgrunnlagTjeneste.MatchBeregningsgrunnlagTjenesteFeil.class);

        @TekniskFeil(feilkode = "FT-401644", feilmelding = "Finner ikke andelen for eksisterende grunnlag.", logLevel = LogLevel.WARN)
        Feil finnerIkkeAndelFeil();

        @TekniskFeil(feilkode = "FT-401692", feilmelding = "Fant flere enn 1 matchende periode i gjeldende grunnlag.", logLevel = LogLevel.WARN)
        Feil fantFlereEnn1Periode();
    }

}
