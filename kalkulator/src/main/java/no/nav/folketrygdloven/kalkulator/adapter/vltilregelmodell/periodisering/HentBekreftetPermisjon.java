package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.BekreftetPermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public class HentBekreftetPermisjon {

    private HentBekreftetPermisjon() {
        // skjul public constructor
    }

    public static Optional<BekreftetPermisjonDto> hent(InntektArbeidYtelseGrunnlagDto grunnlag, YrkesaktivitetDto yrkesaktivitet) {
        return hent(grunnlag, yrkesaktivitet.getArbeidsgiver(), yrkesaktivitet.getArbeidsforholdRef());
    }

    public static Optional<BekreftetPermisjonDto> hent(InntektArbeidYtelseGrunnlagDto grunnlag, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        List<ArbeidsforholdOverstyringDto> overstyringer = hentAlleOverstyringer(grunnlag);
        Optional<ArbeidsforholdOverstyringDto> overstyring = finnOverstyringSomMatcherArbeidsforhold(arbeidsgiver, arbeidsforholdRef, overstyringer);
        return overstyring.flatMap(ArbeidsforholdOverstyringDto::getBekreftetPermisjon);
    }

    private static Optional<ArbeidsforholdOverstyringDto> finnOverstyringSomMatcherArbeidsforhold(Arbeidsgiver arbeidsgiver,
                                                                                                  InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                                                  List<ArbeidsforholdOverstyringDto> overstyringer) {
        return overstyringer.stream()
            .filter(over -> Objects.equals(arbeidsgiver, over.getArbeidsgiver()) && arbeidsforholdRef.gjelderFor(over.getArbeidsforholdRef()))
            .findFirst();
    }

    private static List<ArbeidsforholdOverstyringDto> hentAlleOverstyringer(InntektArbeidYtelseGrunnlagDto grunnlag) {
        return grunnlag.getArbeidsforholdOverstyringer();
    }

}
