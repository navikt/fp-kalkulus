package no.nav.folketrygdloven.kalkulator;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class AksjonspunktUtlederFastsettBeregningsaktiviteterFelles implements AksjonspunktUtlederFastsettBeregningsaktiviteter {

    private static List<BeregningAksjonspunktResultat> utledAksjonspunkterForFelles(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                    BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                    BeregningsgrunnlagInput input,
                                                                                    boolean erOverstyrt,
                                                                                    FagsakYtelseType fagsakYtelseType) {
        Optional<AktørYtelseDto> aktørYtelse = input.getIayGrunnlag().getAktørYtelseFraRegister(input.getBehandlingReferanse().getAktørId());
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        List<Arbeidsgiver> arbeidsgivere = inntektsmeldinger.stream().map(InntektsmeldingDto::getArbeidsgiver).collect(Collectors.toList());
        if (no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, arbeidsgivere, FPDateUtil.iDag())) {
            return List.of(opprettSettPåVentAutopunktForVentPåRapportering(input, beregningsgrunnlag));
        }
        Optional<LocalDate> ventPåMeldekortFrist = no.nav.folketrygdloven.kalkulator.AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(input.getFagsakYtelseType(), aktørYtelse, beregningsgrunnlag, FPDateUtil.iDag());
        if (ventPåMeldekortFrist.isPresent()) {
            return List.of(opprettSettPåVentAutopunktMeldekort(ventPåMeldekortFrist.get()));
        }
        if (erOverstyrt) {
            return emptyList();
        }

        if (no.nav.folketrygdloven.kalkulator.AvklarAktiviteterTjeneste.skalAvklareAktiviteter(beregningsgrunnlag, beregningAktivitetAggregat, aktørYtelse, fagsakYtelseType)) {
            return List.of(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.AVKLAR_AKTIVITETER));
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

    protected static BeregningAksjonspunktResultat autopunkt(BeregningAksjonspunktDefinisjon apDef, BeregningVenteårsak venteårsak, LocalDate settPåVentTom) {
        return BeregningAksjonspunktResultat.opprettMedFristFor(apDef, venteårsak, LocalDateTime.of(settPåVentTom, LocalTime.MIDNIGHT));
    }

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat, BeregningAktivitetAggregatDto beregningAktivitetAggregat, BeregningsgrunnlagInput input, boolean erOverstyrt, FagsakYtelseType fagsakYtelseType) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagRegelResultat.getBeregningsgrunnlag();
        return utledAksjonspunkterForFelles(beregningsgrunnlag, beregningAktivitetAggregat, input, erOverstyrt, fagsakYtelseType);
    }
}
