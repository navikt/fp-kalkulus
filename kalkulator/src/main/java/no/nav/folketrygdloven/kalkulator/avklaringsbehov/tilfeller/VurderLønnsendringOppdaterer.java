package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste.finnAktiviteterMedLønnsendringEtterFørsteDagISisteMåned;
import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste.finnAktiviteterMedLønnsendringIHeleBeregningsperioden;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderLønnsendringDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_LØNNSENDRING")
public class VurderLønnsendringOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderLønnsendringDto lønnsendringDto = dto.getVurdertLonnsendring();
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerAndeler = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
                .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
                .collect(Collectors.toList());
        List<YrkesaktivitetDto> aktiviteterMedLønnsendring;
        if (KonfigurasjonVerdi.get("AUTOMATISK_BEREGNE_LONNSENDRING",false)) {
            aktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringEtterFørsteDagISisteMåned(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
        } else {
            aktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringIHeleBeregningsperioden(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
        }
        FaktaAggregatDto.Builder faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        arbeidstakerAndeler.stream().filter(a -> a.getBgAndelArbeidsforhold().isPresent())
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
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
