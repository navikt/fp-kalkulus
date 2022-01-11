package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederRefusjonEtterSluttdato;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutledertjenesteVurderRefusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
public class AvklaringsbehovutledertjenesteVurderRefusjonFP implements AvklaringsbehovutledertjenesteVurderRefusjon {

    @Inject
    public AvklaringsbehovutledertjenesteVurderRefusjonFP() {
    }

    @Override
    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = new ArrayList<>();

        if (AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            avklaringsbehov.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV));
        }

        if (input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag) {
            ForeldrepengerGrunnlag fpGrunnlag = input.getYtelsespesifiktGrunnlag();
            Collection<YrkesaktivitetDto> yrkesaktiviteter = input.getIayGrunnlag().getAktørArbeidFraRegister()
                    .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                    .orElse(Collections.emptyList());
            // Skal ikke gi avklaringsbehov enda, ønsker analyse på hvor mange slike saker vi har
            AvklaringsbehovutlederRefusjonEtterSluttdato.harRefusjonEtterSisteDatoIArbeidsforhold(yrkesaktiviteter,
                    input.getKoblingReferanse().getKoblingUuid(), fpGrunnlag.getSisteSøkteUttaksdag(), fpGrunnlag.getBehandlingstidspunkt(), periodisertMedRefusjonOgGradering);

        }

        return avklaringsbehov;
    }
}
