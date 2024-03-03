package no.nav.folketrygdloven.kalkulus.håndtering.faktafordeling;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.InntektEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.NyttInntektsforholdEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;

public class UtledEndringINyeInntektsforhold {

    public static List<NyttInntektsforholdEndring> utledEndringer(BeregningsgrunnlagPeriodeDto periode, Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode) {
        var forrigeTilkomneInntektsforhold = forrigePeriode.map(BeregningsgrunnlagPeriodeDto::getTilkomneInntekter)
                .orElse(Collections.emptyList());
        return periode.getTilkomneInntekter().stream()
                .map(i -> utledEndringForInntektsforhold(i, finnForrige(i, forrigeTilkomneInntektsforhold)))
                .toList();
    }

    private static Optional<TilkommetInntektDto> finnForrige(TilkommetInntektDto inntektsforhold, List<TilkommetInntektDto> forrigeTilkomneInntektsforhold) {
        return forrigeTilkomneInntektsforhold.stream()
                .filter(i -> i.getAktivitetStatus().equals(inntektsforhold.getAktivitetStatus()) &&
                        Objects.equals(inntektsforhold.getArbeidsgiver().orElse(null), i.getArbeidsgiver().orElse(null)) &&
                        Objects.equals(inntektsforhold.getArbeidsforholdRef(), i.getArbeidsforholdRef()))
                .findFirst();
    }

    private static NyttInntektsforholdEndring utledEndringForInntektsforhold(TilkommetInntektDto inntektsforhold, Optional<TilkommetInntektDto> forrigeInntektsforhold) {
        return new NyttInntektsforholdEndring(
                inntektsforhold.getAktivitetStatus(),
                mapArbeidsgiver(inntektsforhold),
                new InntektEndring(forrigeInntektsforhold.map(TilkommetInntektDto::getBruttoInntektPrÅr).map(ModellTyperMapper::beløpTilDto).orElse(null), ModellTyperMapper.beløpTilDto(inntektsforhold.getBruttoInntektPrÅr())),
                new ToggleEndring(forrigeInntektsforhold.map(TilkommetInntektDto::skalRedusereUtbetaling).orElse(null), inntektsforhold.skalRedusereUtbetaling()));
    }

    private static Aktør mapArbeidsgiver(TilkommetInntektDto inntektsforhold) {
        var arbeidsgiver = inntektsforhold.getArbeidsgiver();
        return arbeidsgiver.map(a -> a.getErVirksomhet() ? new Organisasjon(a.getIdentifikator()) : new AktørIdPersonident(a.getIdentifikator()))
                .orElse(null);
    }

}
