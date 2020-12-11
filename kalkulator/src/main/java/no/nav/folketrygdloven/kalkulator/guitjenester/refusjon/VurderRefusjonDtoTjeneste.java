package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AndelerMedØktRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;

public final class VurderRefusjonDtoTjeneste {

    private VurderRefusjonDtoTjeneste() {
        // Skjuler default
    }

    public static Optional<RefusjonTilVurderingDto> lagDto(BeregningsgrunnlagGUIInput input) {
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag = input.getVurderRefusjonBeregningsgrunnlagGrunnlag().orElse(input.getBeregningsgrunnlagGrunnlag()).getBeregningsgrunnlag();
        Optional<BeregningsgrunnlagDto> orginaltBG = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        if (orginaltBG.isEmpty() || beregningsgrunnlag.isEmpty()) {
            return Optional.empty();
        }

        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon = AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(beregningsgrunnlag.get(), orginaltBG.get());
        if (!andelerMedØktRefusjon.isEmpty()) {
            return LagVurderRefusjonDto.lagDto(andelerMedØktRefusjon, input);
        }

        return lagDtoBasertPåTidligereAvklaringer(input);
    }

    // Metode for å støtte visning av saker som tidligere er løst men som av ulike grunner ikke lenger gir samme resultat i aksjonspunktutledning
    private static Optional<RefusjonTilVurderingDto> lagDtoBasertPåTidligereAvklaringer(BeregningsgrunnlagGUIInput input) {
        Intervall hardkodetIntervall = Intervall.fraOgMed(input.getSkjæringstidspunktForBeregning()); // Bruker hele perioden det kan kreves refusjon for
        List<RefusjonAndel> andeler = new ArrayList<>();
        List<BeregningRefusjonOverstyringDto> refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList())
                .stream()
                .filter(pa -> !pa.getRefusjonPerioder().isEmpty())
                .collect(Collectors.toList());
        refusjonOverstyringer.forEach(avkalring -> {
            List<RefusjonAndel> tidligereAvklaringerPåAG = avkalring.getRefusjonPerioder().stream()
                    .map(refusjonPeriode -> new RefusjonAndel(AktivitetStatus.ARBEIDSTAKER, avkalring.getArbeidsgiver(), refusjonPeriode.getArbeidsforholdRef(),
                            BigDecimal.ZERO, BigDecimal.ZERO)) // De to siste parameterne brukes ikke for å lage dto så kan settes til dummy-verdier
                    .collect(Collectors.toList());
            andeler.addAll(tidligereAvklaringerPåAG);
        });
        if (andeler.isEmpty()) {
            return Optional.empty();
        }
        Map<Intervall, List<RefusjonAndel>> avklaringMap = new HashMap<>();
        avklaringMap.put(hardkodetIntervall, andeler);
        return LagVurderRefusjonDto.lagDto(avklaringMap, input);
    }

}
