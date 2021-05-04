package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class Ytelsegrunnlag {
    private FagsakYtelseType ytelse;
    private List<Ytelseperiode> perioder = new ArrayList<>();

    public Ytelsegrunnlag(FagsakYtelseType ytelse, List<Ytelseperiode> perioder) {
        Objects.requireNonNull(ytelse, "ytelse");
        Objects.requireNonNull(perioder, "ytelseperioder");
        this.ytelse = ytelse;
        this.perioder = perioder;
    }

    public FagsakYtelseType getYtelse() {
        return ytelse;
    }

    public List<Ytelseperiode> getPerioder() {
        return perioder;
    }
}
