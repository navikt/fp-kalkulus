package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BekreftetPermisjonStatus;


public class BekreftetPermisjonDto {

    private BekreftetPermisjonStatus status = BekreftetPermisjonStatus.UDEFINERT;

    private Intervall periode;

    BekreftetPermisjonDto() {
    }

    public BekreftetPermisjonDto(BekreftetPermisjonStatus status){
        this.status = status;
    }

    public BekreftetPermisjonDto(LocalDate permisjonFom, LocalDate permisjonTom, BekreftetPermisjonStatus status){
        this.periode = Intervall.fraOgMedTilOgMed(permisjonFom, permisjonTom);
        this.status = status;
    }

    public BekreftetPermisjonDto(BekreftetPermisjonDto bekreftetPermisjon) {
        this.periode = bekreftetPermisjon.getPeriode();
        this.status = bekreftetPermisjon.getStatus();
    }

    public BekreftetPermisjonStatus getStatus() {
        return status;
    }

    public Intervall getPeriode() {
        return periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof BekreftetPermisjonDto))
            return false;
        BekreftetPermisjonDto that = (BekreftetPermisjonDto) o;
        return Objects.equals(periode, that.periode)
            && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, status);
    }

    @Override
    public String toString() {
        return "BekreftetPermisjon<" +
            "periode=" + periode +
            ", status=" + status +
            '>';
    }

}
