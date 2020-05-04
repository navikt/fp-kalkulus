package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class AksjonspunktUtlederFastsettBeregningsaktiviteterFRISINN implements AksjonspunktUtlederFastsettBeregningsaktiviteter {

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagRegelResultat regelResultat,
                                                                   BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                   BeregningsgrunnlagInput input,
                                                                   boolean erOverstyrt,
                                                                   FagsakYtelseType fagsakYtelseType) {
        if (regelResultat.getBeregningsgrunnlag() == null) {
            if (regelResultat.getAksjonspunkter().stream().anyMatch(bar -> bar.getBeregningAksjonspunktDefinisjon().equals(BeregningAksjonspunktDefinisjon.AUTO_VENT_FRISINN))) {
                return List.of(BeregningAksjonspunktResultat.opprettMedFristFor(
                        BeregningAksjonspunktDefinisjon.AUTO_VENT_FRISINN,
                        BeregningVenteårsak.INGEN_PERIODE_UTEN_YTELSE,
                        LocalDateTime.of(TIDENES_ENDE, LocalTime.MIDNIGHT)));
            }
        }

        List<BeregningAktivitetDto> aktiviteter = beregningAktivitetAggregat.getBeregningAktiviteter();
        List<BeregningAktivitetDto> korteArbeidsforhold = aktiviteter.stream()
                .filter(this::varerUnder6Måneder)
                .filter(a -> OpptjeningAktivitetType.ARBEID.equals(a.getOpptjeningAktivitetType()))
                .collect(Collectors.toList());
        if (!korteArbeidsforhold.isEmpty()) {
            return List.of(BeregningAksjonspunktResultat.opprettMedFristFor(
                    BeregningAksjonspunktDefinisjon.AUTO_VENT_FRISINN,
                    BeregningVenteårsak.KORTVARIG_ARBEID,
                    LocalDateTime.of(TIDENES_ENDE, LocalTime.MIDNIGHT)));
        }

        return Collections.emptyList();
    }

    private boolean varerUnder6Måneder(BeregningAktivitetDto a) {
        return a.getPeriode().getFomDato().isAfter(LocalDate.of(2019, 9, 1)) &&
                a.getPeriode().getTomDato().isBefore(a.getPeriode().getFomDato().plusMonths(6));
    }
}
