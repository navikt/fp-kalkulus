package no.nav.folketrygdloven.kalkulator.kontrakt.v1;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class MinimalIAY {

    Arbeidsgiver arbeidsgiver;
    InternArbeidsforholdRefDto internArbeidsforholdRefDto;
    Intervall ansettelsePeriode;
    List<AktivitetsAvtaleDto> aktivitetsAvtaler;
    List<InntektDto> inntekter;


    public MinimalIAY(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internArbeidsforholdRefDto, Intervall ansettelsePeriode, List<AktivitetsAvtaleDto> aktivitetsAvtaler, List<InntektDto> inntekter) {
        this.arbeidsgiver = arbeidsgiver;
        this.internArbeidsforholdRefDto = internArbeidsforholdRefDto;
        this.ansettelsePeriode = ansettelsePeriode;
        this.aktivitetsAvtaler = aktivitetsAvtaler;
        this.inntekter = inntekter;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getInternArbeidsforholdRefDto() {
        return internArbeidsforholdRefDto;
    }

    public Intervall getAnsettelsePeriode() {
        return ansettelsePeriode;
    }

    public List<AktivitetsAvtaleDto> getAktivitetsAvtaler() {
        return aktivitetsAvtaler;
    }

    public List<InntektDto> getInntekter() {
        return inntekter;
    }
}
