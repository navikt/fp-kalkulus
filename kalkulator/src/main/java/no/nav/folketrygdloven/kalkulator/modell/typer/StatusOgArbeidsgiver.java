package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public record StatusOgArbeidsgiver(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusOgArbeidsgiver that = (StatusOgArbeidsgiver) o;
        return aktivitetStatus == that.aktivitetStatus && Objects.equals(arbeidsgiver, that.arbeidsgiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver);
    }
}
