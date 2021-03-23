package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AksjonspunktutlederRefusjonEtterSluttdato;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AksjonspunktutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AksjonspunkutledertjenesteVurderRefusjon;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;

@ApplicationScoped
@FagsakYtelseTypeRef("SVP")
public class AksjonspunktutledertjenesteVurderRefusjonSVP implements AksjonspunkutledertjenesteVurderRefusjon {

    @Inject
    public AksjonspunktutledertjenesteVurderRefusjonSVP() {
    }

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        List<BeregningAksjonspunktResultat> aksjonspunkter = new ArrayList<>();

        if (AksjonspunktutlederVurderRefusjon.skalHaAksjonspunktVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            aksjonspunkter.add(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunkt.VURDER_REFUSJONSKRAV));
        }

        if (input.getYtelsespesifiktGrunnlag() instanceof SvangerskapspengerGrunnlag) {
            SvangerskapspengerGrunnlag svpGrunnlag = input.getYtelsespesifiktGrunnlag();
            Collection<YrkesaktivitetDto> yrkesaktiviteter = input.getIayGrunnlag().getAktørArbeidFraRegister()
                    .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                    .orElse(Collections.emptyList());
            // Skal ikke gi aksjonspunkt enda, ønsker analyse på hvor mange slike saker vi har
            AksjonspunktutlederRefusjonEtterSluttdato.harRefusjonEtterSisteDatoIArbeidsforhold(yrkesaktiviteter,
                    input.getKoblingReferanse().getKoblingUuid(), finnSisteDagMedUtbetaling(svpGrunnlag), svpGrunnlag.getBehandlingstidspunkt(), periodisertMedRefusjonOgGradering);
        }
        return aksjonspunkter;
    }

    private Optional<LocalDate> finnSisteDagMedUtbetaling(SvangerskapspengerGrunnlag svpGrunnlag) {
        return svpGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .flatMap(svp -> svp.getPeriodeMedUtbetalingsgrad().stream())
                .filter(utb -> utb.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode).map(Intervall::getTomDato)
                .max(Comparator.naturalOrder());

    }
}
