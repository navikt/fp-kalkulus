package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k14.fp;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AutopunktUtlederFastsettBeregningsaktiviteterTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AvklarAktiviteterTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AvklaringsbehovUtlederFastsettBeregningsaktiviteter;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FORELDREPENGER)
public class AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP implements AvklaringsbehovUtlederFastsettBeregningsaktiviteter {

    private static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovFP(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                 BeregningsgrunnlagInput input,
                                                                                 boolean erOverstyrt,
                                                                                 LocalDate skjæringstidspunktForBeregning) {
        Optional<AktørYtelseDto> aktørYtelse = input.getIayGrunnlag().getAktørYtelseFraRegister();
        Optional<LocalDate> ventPåRapporteringAvInntektFrist = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntektFL(input, LocalDate.now(), beregningAktivitetAggregat, skjæringstidspunktForBeregning);
        if (ventPåRapporteringAvInntektFrist.isPresent()) {
            return List.of(autopunkt(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST, BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST, ventPåRapporteringAvInntektFrist.get()));
        }
        Optional<LocalDate> ventPåMeldekortFrist = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(aktørYtelse, LocalDate.now(), beregningAktivitetAggregat.getSkjæringstidspunktOpptjening(), Set.of(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER, FagsakYtelseType.DAGPENGER));
        if (ventPåMeldekortFrist.isPresent()) {
            return List.of(autopunkt(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT, BeregningVenteårsak.VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT, ventPåMeldekortFrist.get()));
        }
        if (erOverstyrt) {
            return emptyList();
        }

        if (AvklarAktiviteterTjeneste.skalAvklareAktiviteter(beregningAktivitetAggregat, aktørYtelse, input.getFagsakYtelseType())) {
            return List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER));
        }
        return emptyList();
    }

    protected static BeregningAvklaringsbehovResultat autopunkt(AvklaringsbehovDefinisjon apDef, BeregningVenteårsak venteårsak, LocalDate settPåVentTom) {
        return BeregningAvklaringsbehovResultat.opprettMedFristFor(apDef, venteårsak, LocalDateTime.of(settPåVentTom, LocalTime.MIDNIGHT));
    }

    @Override
    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagRegelResultat regelResultat, BeregningsgrunnlagInput input, boolean erOverstyrt) {
        BeregningAktivitetAggregatDto registerAktiviteter = regelResultat.getRegisterAktiviteter();
        LocalDate skjæringstidspunkt = regelResultat.getBeregningsgrunnlag().getSkjæringstidspunkt();
        return utledAvklaringsbehovFP(registerAktiviteter, input, erOverstyrt, skjæringstidspunkt);
    }
}
