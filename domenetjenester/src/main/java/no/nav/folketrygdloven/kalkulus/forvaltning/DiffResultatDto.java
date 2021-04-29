package no.nav.folketrygdloven.kalkulus.forvaltning;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class DiffResultatDto {
    private String saksnummer;
    private boolean harDiff;
    private List<AktivitetStatus> statuserDetErSøktOm;
    private BigDecimal gjeldendeInntektIkkeSøktOm;
    private BigDecimal reberegnetInntektIkkeSøktOm;
    private BigDecimal gjeldendeInntektSøktOm;
    private BigDecimal reberegnetInntektSøktOm;

    public DiffResultatDto() {
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public boolean isHarDiff() {
        return harDiff;
    }

    public void setHarDiff(boolean harDiff) {
        this.harDiff = harDiff;
    }

    public List<AktivitetStatus> getStatuserDetErSøktOm() {
        return statuserDetErSøktOm;
    }

    public void setStatuserDetErSøktOm(List<AktivitetStatus> statuserDetErSøktOm) {
        this.statuserDetErSøktOm = statuserDetErSøktOm;
    }

    public BigDecimal getGjeldendeInntektIkkeSøktOm() {
        return gjeldendeInntektIkkeSøktOm;
    }

    public void setGjeldendeInntektIkkeSøktOm(BigDecimal gjeldendeInntektIkkeSøktOm) {
        this.gjeldendeInntektIkkeSøktOm = gjeldendeInntektIkkeSøktOm;
    }

    public BigDecimal getReberegnetInntektIkkeSøktOm() {
        return reberegnetInntektIkkeSøktOm;
    }

    public void setReberegnetInntektIkkeSøktOm(BigDecimal reberegnetInntektIkkeSøktOm) {
        this.reberegnetInntektIkkeSøktOm = reberegnetInntektIkkeSøktOm;
    }

    public BigDecimal getGjeldendeInntektSøktOm() {
        return gjeldendeInntektSøktOm;
    }

    public void setGjeldendeInntektSøktOm(BigDecimal gjeldendeInntektSøktOm) {
        this.gjeldendeInntektSøktOm = gjeldendeInntektSøktOm;
    }

    public BigDecimal getReberegnetInntektSøktOm() {
        return reberegnetInntektSøktOm;
    }

    public void setReberegnetInntektSøktOm(BigDecimal reberegnetInntektSøktOm) {
        this.reberegnetInntektSøktOm = reberegnetInntektSøktOm;
    }
}
