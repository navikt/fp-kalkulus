package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class ArbeidsforholdOverstyrtePerioderDto {

    private Intervall periode;

    @JsonBackReference
    private ArbeidsforholdOverstyringDto arbeidsforholdOverstyring;

    ArbeidsforholdOverstyrtePerioderDto() {

    }

    ArbeidsforholdOverstyrtePerioderDto(ArbeidsforholdOverstyrtePerioderDto arbeidsforholdOverstyrtePerioder) {
        this.periode = arbeidsforholdOverstyrtePerioder.getOverstyrtePeriode();
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    public ArbeidsforholdOverstyringDto getArbeidsforholdOverstyring() {
        return arbeidsforholdOverstyring;
    }

    void setArbeidsforholdOverstyring(ArbeidsforholdOverstyringDto arbeidsforholdOverstyring) {
        this.arbeidsforholdOverstyring = arbeidsforholdOverstyring;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ArbeidsforholdOverstyrtePerioderDto)) return false;
        ArbeidsforholdOverstyrtePerioderDto that = (ArbeidsforholdOverstyrtePerioderDto) o;
        return Objects.equals(periode, that.periode) && Objects.equals(arbeidsforholdOverstyring, that.arbeidsforholdOverstyring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidsforholdOverstyring);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdInformasjonEntitet{" +
                "periode=" + periode +
                ", arbeidsforholdOverstyring=" + arbeidsforholdOverstyring +
                '}';
    }

    public Intervall getOverstyrtePeriode() {
        return periode;
    }
}
