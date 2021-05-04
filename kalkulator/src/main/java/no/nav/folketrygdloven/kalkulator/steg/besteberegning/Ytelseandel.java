package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;

public class Ytelseandel {
    private AktivitetStatus aktivitetStatus;
    private Arbeidskategori arbeidskategori;
    private Long dagsats;

    public Ytelseandel(AktivitetStatus aktivitetStatus,
                       Long dagsats) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetstatus");
        this.aktivitetStatus = aktivitetStatus;
        this.dagsats = dagsats;
    }

    public Ytelseandel(Arbeidskategori arbeidskategori,
                       Long dagsats) {
        Objects.requireNonNull(arbeidskategori, "arbeidskategori");
        this.arbeidskategori = arbeidskategori;
        this.dagsats = dagsats;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }

    public Long getDagsats() {
        return dagsats;
    }
}
