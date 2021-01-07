package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AksjonspunktUtlederFastsettBeregningsaktiviteter;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class AksjonspunktUtlederFastsettBeregningsaktiviteterFRISINN implements AksjonspunktUtlederFastsettBeregningsaktiviteter {

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagRegelResultat regelResultat,
                                                                   BeregningsgrunnlagInput input,
                                                                   boolean erOverstyrt) {
        if (regelResultat.getBeregningsgrunnlagHvisFinnes().isEmpty()) {
            if (regelResultat.getAksjonspunkter().stream().anyMatch(bar -> bar.getBeregningAksjonspunktDefinisjon().equals(BeregningAksjonspunkt.AUTO_VENT_FRISINN))) {
                return List.of(BeregningAksjonspunktResultat.opprettMedFristFor(
                        BeregningAksjonspunkt.AUTO_VENT_FRISINN,
                        BeregningVenteårsak.INGEN_PERIODE_UTEN_YTELSE,
                        LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)));
            }
            if (regelResultat.getAksjonspunkter().stream().anyMatch(bar -> bar.getBeregningAksjonspunktDefinisjon().equals(BeregningAksjonspunkt.INGEN_AKTIVITETER))) {
                return List.of(BeregningAksjonspunktResultat.opprettMedFristFor(
                        BeregningAksjonspunkt.AUTO_VENT_FRISINN,
                        BeregningVenteårsak.INGEN_AKTIVITETER,
                        LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)));
            }
        }
        return Collections.emptyList();
    }


}
