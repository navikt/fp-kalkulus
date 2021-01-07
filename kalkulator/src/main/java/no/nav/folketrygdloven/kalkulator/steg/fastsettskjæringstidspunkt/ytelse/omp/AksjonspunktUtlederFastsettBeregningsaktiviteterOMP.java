package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.omp;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AksjonspunktUtlederFastsettBeregningsaktiviteter;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AvklarAktiviteterTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class AksjonspunktUtlederFastsettBeregningsaktiviteterOMP implements AksjonspunktUtlederFastsettBeregningsaktiviteter {

    private static List<BeregningAksjonspunktResultat> utledAksjonspunkterForOMP(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                 BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                 BeregningsgrunnlagInput input,
                                                                                 boolean erOverstyrt) {
        Optional<AktørYtelseDto> aktørYtelse = input.getIayGrunnlag().getAktørYtelseFraRegister();
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        List<Arbeidsgiver> arbeidsgivere = inntektsmeldinger.stream().map(InntektsmeldingDto::getArbeidsgiver).collect(Collectors.toList());
        Optional<LocalDate> ventPåRapporteringAvInntektFrist = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, arbeidsgivere, LocalDate.now());
        if (ventPåRapporteringAvInntektFrist.isPresent()) {
            return List.of(autopunkt(BeregningAksjonspunkt.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST, BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST, ventPåRapporteringAvInntektFrist.get()));
        }
        if (erOverstyrt) {
            return emptyList();
        }
        if (AvklarAktiviteterTjeneste.skalAvklareAktiviteter(beregningsgrunnlag, beregningAktivitetAggregat, aktørYtelse, input.getFagsakYtelseType())) {
            return List.of(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunkt.AVKLAR_AKTIVITETER));
        }
        return emptyList();
    }

    protected static BeregningAksjonspunktResultat autopunkt(BeregningAksjonspunkt apDef, BeregningVenteårsak venteårsak, LocalDate settPåVentTom) {
        return BeregningAksjonspunktResultat.opprettMedFristFor(apDef, venteårsak, LocalDateTime.of(settPåVentTom, LocalTime.MIDNIGHT));
    }

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat, BeregningsgrunnlagInput input, boolean erOverstyrt) {
        var beregningsgrunnlag = beregningsgrunnlagRegelResultat.getBeregningsgrunnlagHvisFinnes();
        if (beregningsgrunnlag.isPresent()) {
            BeregningAktivitetAggregatDto registerAktiviteter = beregningsgrunnlagRegelResultat.getRegisterAktiviteter();
            return utledAksjonspunkterForOMP(beregningsgrunnlag.get(), registerAktiviteter, input, erOverstyrt);
        }
        return emptyList();
    }
}
