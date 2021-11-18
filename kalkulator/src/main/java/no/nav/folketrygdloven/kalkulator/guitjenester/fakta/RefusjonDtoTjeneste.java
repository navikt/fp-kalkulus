package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingGraderingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagAndelDto;

/**
 * Tjeneste som henter ut informasjon relatert til refusjon fra beregnigsgrunnlag og setter det på dto-objekt
 */
public class RefusjonDtoTjeneste {

    private RefusjonDtoTjeneste() {
        // Hide constructor
    }

    /**
     * Utleder om gitt andel har informasjon i inntektsmelding som krever at man skal kunne flytte refusjon i perioden
     *
     * @param andelFraOppdatert Beregnignsgrunnlagsandel fra oppdatert grunnlag
     * @param periode
     * @param aktivitetGradering Graderinger for behandling
     * @param grunnbeløp
     * @return Returnerer true om andel har gradering (uten refusjon) og total refusjon i perioden er større enn 6G, ellers false
     */
    static boolean skalKunneEndreRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andelFraOppdatert,
                                          BeregningsgrunnlagPeriodeDto periode, AktivitetGradering aktivitetGradering,
                                          Beløp grunnbeløp) {
        if (harGraderingOgIkkeRefusjon(andelFraOppdatert, periode, aktivitetGradering)) {
            return grunnbeløp.multipliser(6).getVerdi().compareTo(finnTotalRefusjonPrÅr(periode)) <= 0;
        }
        return false;
    }

    private static BigDecimal finnTotalRefusjonPrÅr(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().flatMap(a -> a.getBgAndelArbeidsforhold().stream())
                .map(BGAndelArbeidsforholdDto::getInnvilgetRefusjonskravPrÅr)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }
    private static boolean harGraderingOgIkkeRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andelFraOppdatert, BeregningsgrunnlagPeriodeDto periode, AktivitetGradering aktivitetGradering) {
        List<Gradering> graderingForAndelIPeriode = FordelingGraderingTjeneste.hentGraderingerForAndelIPeriode(andelFraOppdatert, aktivitetGradering, periode.getPeriode());
        boolean andelHarGradering = !graderingForAndelIPeriode.isEmpty();
        BigDecimal refusjon = andelFraOppdatert.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr)
            .orElse(BigDecimal.ZERO);
        boolean andelHarRefusjon = refusjon.compareTo(BigDecimal.ZERO) > 0;
        return andelHarGradering && !andelHarRefusjon;
    }

    /**
     * Setter refusjonkrav på dto-objekt for gitt andel, både beløp fra inntektsmelding og fastsatt refusjon (redigert i fakta om beregning)
     *  @param andel Beregningsgrunnlagandel
     * @param endringAndel Dto for andel
     */
    public static void settRefusjonskrav(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                         FordelBeregningsgrunnlagAndelDto endringAndel) {
        if (andel.erLagtTilAvSaksbehandler()) {
            endringAndel.setRefusjonskravFraInntektsmeldingPrÅr(BigDecimal.ZERO);
        } else {
            andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
                    .ifPresent(endringAndel::setRefusjonskravFraInntektsmeldingPrÅr);
        }
        endringAndel.setRefusjonskravPrAar(andel.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr)
            .orElse(BigDecimal.ZERO));
    }


    /**
     * Adderer refusjon for andeler i samme arbeidsforhold og setter det på andelen som ikke er lagt til av saksbehandler
     *
     * @param endringAndeler Liste med Dto-objekt for andeler
     */
    public static void slåSammenRefusjonForAndelerISammeArbeidsforhold(List<FordelBeregningsgrunnlagAndelDto> endringAndeler) {
        Map<BeregningsgrunnlagArbeidsforholdDto, BigDecimal> totalRefusjonMap = getTotalrefusjonPrArbeidsforhold(endringAndeler);
        endringAndeler.forEach(andel -> {
            if (harArbeidsforholdOgErIkkjeLagtTilAvSaksbehandler(andel)) {
                BeregningsgrunnlagArbeidsforholdDto arbeidsforhold = andel.getArbeidsforhold();
                BigDecimal totalRefusjonForArbeidsforhold = totalRefusjonMap.get(arbeidsforhold);
                andel.setRefusjonskravPrAar(totalRefusjonForArbeidsforhold != null ? totalRefusjonForArbeidsforhold : andel.getRefusjonskravPrAar());
            } else if (harArbeidsforholdOgErLagtTilManuelt(andel)) {
                BeregningsgrunnlagArbeidsforholdDto arbeidsforhold = andel.getArbeidsforhold();
                BigDecimal totalRefusjonForArbeidsforhold = totalRefusjonMap.get(arbeidsforhold);
                andel.setRefusjonskravPrAar(totalRefusjonForArbeidsforhold != null ? null : andel.getRefusjonskravPrAar());
            }
        });
    }

    private static Map<BeregningsgrunnlagArbeidsforholdDto, BigDecimal> getTotalrefusjonPrArbeidsforhold(List<FordelBeregningsgrunnlagAndelDto> andeler) {
        Map<BeregningsgrunnlagArbeidsforholdDto, BigDecimal> arbeidsforholdRefusjonMap = new HashMap<>();
        andeler.forEach(andel -> {
            if (andel.getArbeidsforhold() != null) {
                BeregningsgrunnlagArbeidsforholdDto arbeidsforhold = andel.getArbeidsforhold();
                BigDecimal refusjonskrav = andel.getRefusjonskravPrAar() == null ?
                    BigDecimal.ZERO : andel.getRefusjonskravPrAar();
                if (arbeidsforholdRefusjonMap.containsKey(arbeidsforhold)) {
                    BigDecimal totalRefusjon = arbeidsforholdRefusjonMap.get(arbeidsforhold).add(refusjonskrav);
                    arbeidsforholdRefusjonMap.put(arbeidsforhold, totalRefusjon);
                } else {
                    arbeidsforholdRefusjonMap.put(arbeidsforhold, refusjonskrav);
                }
            }
        });
        return arbeidsforholdRefusjonMap;
    }

    private static boolean harArbeidsforholdOgErLagtTilManuelt(FordelBeregningsgrunnlagAndelDto andel) {
        return andel.getArbeidsforhold() != null && (andel.getLagtTilAvSaksbehandler() || andel.getKilde().equals(AndelKilde.PROSESS_OMFORDELING));
    }

    private static boolean harArbeidsforholdOgErIkkjeLagtTilAvSaksbehandler(FordelBeregningsgrunnlagAndelDto andel) {
        return andel.getArbeidsforhold() != null && !andel.getLagtTilAvSaksbehandler();
    }


}
