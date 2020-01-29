package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektsKilde;

public class InntektDtoBuilder {
    private final boolean oppdaterer;
    private InntektDto inntekt;

    private InntektDtoBuilder(InntektDto inntekt, boolean oppdaterer) {
        this.inntekt = inntekt;
        this.oppdaterer = oppdaterer;
    }

    static InntektDtoBuilder ny() {
        return new InntektDtoBuilder(new InntektDto(), false);
    }

    static InntektDtoBuilder oppdatere(InntektDto oppdatere) {
        return new InntektDtoBuilder(oppdatere, true);
    }

    public static InntektDtoBuilder oppdatere(Optional<InntektDto> oppdatere) {
        return oppdatere.map(InntektDtoBuilder::oppdatere).orElseGet(InntektDtoBuilder::ny);
    }

    public InntektDtoBuilder medInntektsKilde(InntektsKilde inntektsKilde) {
        this.inntekt.setInntektsKilde(inntektsKilde);
        return this;
    }

    public InntektDtoBuilder leggTilInntektspost(InntektspostDtoBuilder builder) {
        InntektspostDto inntektspost = builder.build();
        inntekt.leggTilInntektspost(inntektspost);
        return this;
    }

    public InntektDtoBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.inntekt.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public InntektspostDtoBuilder getInntektspostBuilder() {
        return inntekt.getInntektspostBuilder();
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public InntektDto build() {
        if (inntekt.hasValues()) {
            return inntekt;
        }
        throw new IllegalStateException();
    }
}
