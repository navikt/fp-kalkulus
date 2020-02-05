package no.nav.folketrygdloven.kalkulator;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.vedtak.util.FPDateUtil;

public class AksjonspunktUtlederFastsettBeregningsaktiviteter {

    /**
     * Toggelen er knyttet til manglende arbeidsforhold pga kommune- og fylkessammenslåing, https://jira.adeo.no/browse/TFP-2172
     * Forventer at nytt arbeidsforhold skal komme inn med a-melding til 5. februar
     * Forventer at toggle kan fjernes rundt denne datoen
     */
    private static final LocalDate FORVENTET_DATO_NYTT_ARBEIDSFORHOLD = LocalDate.of(2020, Month.FEBRUARY, 5);
    private static final String AUTOPUNKT_TOGGLE_MANGLENDE_ARBEIDSFORHOLD = "fpsak.autopunkter.manglendeArbeidsforhold";


    public static List<BeregningAksjonspunktResultat> utledAksjonspunkterForFelles(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                               BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                               BeregningsgrunnlagInput input,
                                                                               boolean erOverstyrt) {
        Optional<AktørYtelseDto> aktørYtelse = input.getIayGrunnlag().getAktørYtelseFraRegister(input.getBehandlingReferanse().getAktørId());
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        List<Arbeidsgiver> arbeidsgivere = inntektsmeldinger.stream().map(InntektsmeldingDto::getArbeidsgiver).collect(Collectors.toList());
        if (no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, arbeidsgivere, FPDateUtil.iDag())) {
            return List.of(opprettSettPåVentAutopunktForVentPåRapportering(input, beregningsgrunnlag));
        }
        Optional<LocalDate> ventPåMeldekortFrist = no.nav.folketrygdloven.kalkulator.AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(aktørYtelse, beregningsgrunnlag, FPDateUtil.iDag());
        if (ventPåMeldekortFrist.isPresent()) {
            return List.of(opprettSettPåVentAutopunktMeldekort(ventPåMeldekortFrist.get()));
        }
        if (erOverstyrt) {
            return emptyList();
        }

        if (no.nav.folketrygdloven.kalkulator.AvklarAktiviteterTjeneste.skalAvklareAktiviteter(beregningsgrunnlag, beregningAktivitetAggregat, aktørYtelse)) {
            return List.of(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.AVKLAR_AKTIVITETER));
        }

        if(input.isEnabled(AUTOPUNKT_TOGGLE_MANGLENDE_ARBEIDSFORHOLD, false)){
            if(skalVentePåManglendeArbeidsforhold(beregningsgrunnlag, beregningAktivitetAggregat)){
                return List.of(opprettSettPåVentAutopunktManglendeArbeidsforhold(FORVENTET_DATO_NYTT_ARBEIDSFORHOLD));
            }
        }
        return emptyList();
    }

    private static BeregningAksjonspunktResultat opprettSettPåVentAutopunktForVentPåRapportering(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate fristDato = no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste.utledBehandlingPåVentFrist(input, beregningsgrunnlag);
        return autopunkt(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST, BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST, fristDato);
    }

    private static BeregningAksjonspunktResultat opprettSettPåVentAutopunktMeldekort(LocalDate fristDato) {
        return autopunkt(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT, BeregningVenteårsak.VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT, fristDato);
    }

    private static BeregningAksjonspunktResultat opprettSettPåVentAutopunktManglendeArbeidsforhold(LocalDate fristDato) {
        return autopunkt(BeregningAksjonspunktDefinisjon.AUTO_VENT_PÅ_MANGLENDE_ARBEIDSFORHOLD_KOMMUNEREFORM, BeregningVenteårsak.VENT_MANGLENDE_ARBEIDSFORHOLD, fristDato);
    }

    private static boolean skalVentePåManglendeArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag,
                                                              BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        LocalDate stpOppjening = beregningAktivitetAggregat.getSkjæringstidspunktOpptjening();
        LocalDate stpBeregning = beregningsgrunnlag.getSkjæringstidspunkt();

        if (søkerErKunArbeidstaker(beregningsgrunnlag) && stpIJanuar(stpOppjening) && !stpOppjening.isEqual(stpBeregning)){
                return true;
            }
        return false;
    }

    private static boolean søkerErKunArbeidstaker(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().size() == 1 && AktivitetStatus.ARBEIDSTAKER.equals(beregningsgrunnlag.getAktivitetStatuser().get(0).getAktivitetStatus());
    }

    private static boolean stpIJanuar(LocalDate stpOppjening){
        return stpOppjening.isAfter(LocalDate.of(2019, Month.DECEMBER, 31)) && stpOppjening.isBefore(LocalDate.of(2020, Month.FEBRUARY, 1));
    }

    protected static BeregningAksjonspunktResultat autopunkt(BeregningAksjonspunktDefinisjon apDef, BeregningVenteårsak venteårsak, LocalDate settPåVentTom) {
        return BeregningAksjonspunktResultat.opprettMedFristFor(apDef, venteårsak, LocalDateTime.of(settPåVentTom, LocalTime.MIDNIGHT));
    }
}
