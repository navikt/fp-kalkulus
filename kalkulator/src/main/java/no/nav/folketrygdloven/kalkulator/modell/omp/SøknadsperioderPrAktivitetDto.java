package no.nav.folketrygdloven.kalkulator.modell.omp;

import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class SøknadsperioderPrAktivitetDto {

    private final AktivitetDto aktivitet;
    private final List<Intervall> periode;

    public SøknadsperioderPrAktivitetDto(AktivitetDto aktivitet,
                                         List<Intervall> periode) {
        this.aktivitet = Objects.requireNonNull(aktivitet);
        this.periode = Objects.requireNonNull(periode);
    }

    public List<Intervall> getPeriode() {
        return periode;
    }

    public AktivitetDto getAktivitet() {
        return aktivitet;
    }

}
