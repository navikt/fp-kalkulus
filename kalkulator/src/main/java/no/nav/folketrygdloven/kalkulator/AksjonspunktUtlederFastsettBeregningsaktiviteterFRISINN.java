package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class AksjonspunktUtlederFastsettBeregningsaktiviteterFRISINN implements AksjonspunktUtlederFastsettBeregningsaktiviteter {

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(Optional<BeregningsgrunnlagDto> beregningsgrunnlag, BeregningAktivitetAggregatDto beregningAktivitetAggregat, BeregningsgrunnlagInput input, boolean erOverstyrt, FagsakYtelseType fagsakYtelseType) {
        if (beregningsgrunnlag.isEmpty()) {
            return List.of(BeregningAksjonspunktResultat.opprettMedFristFor(
                    BeregningAksjonspunktDefinisjon.AUTO_VENT_FRISINN,
                    BeregningVenteårsak.INGEN_PERIODE_UTEN_YTELSE,
                    LocalDateTime.of(TIDENES_ENDE, LocalTime.MIDNIGHT)));
        }
        return Collections.emptyList();
    }

}
