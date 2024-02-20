package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederRefusjonEtterSluttdato;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.typer.Utbetalingsgrad;

public class AvklaringsbehovutledertjenesteVurderRefusjonSVP {

    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                       BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = new ArrayList<>();

        if (AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            avklaringsbehov.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV));
        }

        if (input.getYtelsespesifiktGrunnlag() instanceof SvangerskapspengerGrunnlag svpGrunnlag) {
            Collection<YrkesaktivitetDto> yrkesaktiviteter = input.getIayGrunnlag().getAktørArbeidFraRegister()
                    .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                    .orElse(Collections.emptyList());
            // Skal ikke gi avklaringsbehov enda, ønsker analyse på hvor mange slike saker vi har
            AvklaringsbehovutlederRefusjonEtterSluttdato.harRefusjonEtterSisteDatoIArbeidsforhold(yrkesaktiviteter,
                    input.getKoblingReferanse().getKoblingUuid(), finnSisteDagMedUtbetaling(svpGrunnlag), svpGrunnlag.getBehandlingstidspunkt(), periodisertMedRefusjonOgGradering,input.getYtelsespesifiktGrunnlag());
        }
        return avklaringsbehov;
    }

    private Optional<LocalDate> finnSisteDagMedUtbetaling(SvangerskapspengerGrunnlag svpGrunnlag) {
        return svpGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .flatMap(svp -> svp.getPeriodeMedUtbetalingsgrad().stream())
                .filter(utb -> utb.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) > 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode).map(Intervall::getTomDato)
                .max(Comparator.naturalOrder());

    }
}
