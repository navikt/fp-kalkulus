package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class AnvistAndel {

    private Arbeidsgiver arbeidsgiver;
    private BigDecimal beløp;
    private Stillingsprosent refusjonsgrad;
    private Inntektskategori inntektskategori;

    public AnvistAndel(Arbeidsgiver arbeidsgiver, BigDecimal beløp, Stillingsprosent refusjonsgrad, Inntektskategori inntektskategori) {
        this.arbeidsgiver = arbeidsgiver;
        this.beløp = beløp;
        this.refusjonsgrad = refusjonsgrad;
        this.inntektskategori = inntektskategori;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public Stillingsprosent getRefusjonsgrad() {
        return refusjonsgrad;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
