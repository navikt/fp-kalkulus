package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

class FastsettFaktaLønnsendring {

    private FastsettFaktaLønnsendring() {
    }

    static List<FaktaArbeidsforholdDto> fastsettFaktaForLønnsendring(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                     Collection<InntektsmeldingDto> inntektsmeldinger) {
        var aktiviteterMedLønnsendring = LønnsendringTjeneste.finnAktiviteterMedLønnsendringEtterFørsteDagISisteMåned(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger);
        return aktiviteterMedLønnsendring.stream().map(v ->
                FaktaArbeidsforholdDto.builder(v.getArbeidsgiver(), v.getArbeidsforholdRef())
                        .medHarLønnsendringIBeregningsperioden(new FaktaVurdering(true, FaktaVurderingKilde.KALKULATOR))
                        .build()
        ).collect(Collectors.toList());
    }

}
