package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k9.omp;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.felles.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AvklaringsbehovUtlederFastsettBeregningsaktiviteter;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AvklarAktiviteterTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class AvklaringsbehovUtlederFastsettBeregningsaktiviteterOMP implements AvklaringsbehovUtlederFastsettBeregningsaktiviteter {

    private static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovForOMP(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                 BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                 BeregningsgrunnlagInput input,
                                                                                 boolean erOverstyrt) {
        Optional<AktørYtelseDto> aktørYtelse = input.getIayGrunnlag().getAktørYtelseFraRegister();
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        List<Arbeidsgiver> arbeidsgivere = inntektsmeldinger.stream().map(InntektsmeldingDto::getArbeidsgiver).collect(Collectors.toList());
        Optional<LocalDate> ventPåRapporteringAvInntektFrist = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, arbeidsgivere, LocalDate.now());
        if (ventPåRapporteringAvInntektFrist.isPresent()) {
            return List.of(autopunkt(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST, BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST, ventPåRapporteringAvInntektFrist.get()));
        }
        if (erOverstyrt) {
            return emptyList();
        }
        if (AvklarAktiviteterTjeneste.skalAvklareAktiviteter(beregningsgrunnlag, beregningAktivitetAggregat, aktørYtelse, input.getFagsakYtelseType())) {
            return List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER));
        }
        return emptyList();
    }

    protected static BeregningAvklaringsbehovResultat autopunkt(AvklaringsbehovDefinisjon apDef, BeregningVenteårsak venteårsak, LocalDate settPåVentTom) {
        return BeregningAvklaringsbehovResultat.opprettMedFristFor(apDef, venteårsak, LocalDateTime.of(settPåVentTom, LocalTime.MIDNIGHT));
    }

    @Override
    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat, BeregningsgrunnlagInput input, boolean erOverstyrt) {
        var beregningsgrunnlag = beregningsgrunnlagRegelResultat.getBeregningsgrunnlagHvisFinnes();
        if (beregningsgrunnlag.isPresent()) {
            BeregningAktivitetAggregatDto registerAktiviteter = beregningsgrunnlagRegelResultat.getRegisterAktiviteter();
            return utledAvklaringsbehovForOMP(beregningsgrunnlag.get(), registerAktiviteter, input, erOverstyrt);
        }
        return emptyList();
    }
}
