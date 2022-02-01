package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste.finnAktiviteterMedLønnsendringUtenInntektsmeldingIHeleBeregningsperioden;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_LØNNSENDRING")
public class VurderLønnsendringOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        if (KonfigurasjonVerdi.get("AUTOMATISK_BEREGNE_LONNSENDRING", false)) {
            // Dersom toggle er på har vi allerede lagret faktavurderingen
            return;
        }
        var lønnsendringDto = dto.getVurdertLonnsendring();
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var arbeidstakerAndeler = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
                .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
                .collect(Collectors.toList());
        var aktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringUtenInntektsmeldingIHeleBeregningsperioden(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
        var faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        arbeidstakerAndeler.stream().map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(arb -> aktiviteterMedLønnsendring.stream().anyMatch(ya -> ya.getArbeidsgiver().equals(arb.getArbeidsgiver()) &&
                        ya.getArbeidsforholdRef().gjelderFor(arb.getArbeidsforholdRef())))
                .forEach(arb -> {
                    FaktaArbeidsforholdDto.Builder faktaArbBuilder = faktaAggregatBuilder.getFaktaArbeidsforholdBuilderFor(arb.getArbeidsgiver(), arb.getArbeidsforholdRef())
                            .medHarLønnsendringIBeregningsperiodenFastsattAvSaksbehandler(lønnsendringDto.erLønnsendringIBeregningsperioden());
                    faktaAggregatBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
                });
        grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
    }
}
