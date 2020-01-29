package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.UtsettelseÅrsak;
import no.nav.folketrygdloven.kalkulus.felles.tid.DatoIntervallEntitet;

public class UtsettelsePeriodeDto {

    private DatoIntervallEntitet periode;
    private UtsettelseÅrsak årsak = UtsettelseÅrsak.UDEFINERT;

    private UtsettelsePeriodeDto(LocalDate fom, LocalDate tom) {
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
        this.årsak = UtsettelseÅrsak.FERIE;
    }

    private UtsettelsePeriodeDto(LocalDate fom, LocalDate tom, UtsettelseÅrsak årsak) {
        this.årsak = årsak;
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    UtsettelsePeriodeDto() {
    }

    UtsettelsePeriodeDto(UtsettelsePeriodeDto utsettelsePeriode) {
        this.periode = utsettelsePeriode.getPeriode();
        this.årsak = utsettelsePeriode.getÅrsak();
    }

    private UtsettelsePeriodeDto(DatoIntervallEntitet datoIntervall, UtsettelseÅrsak årsak) {
        this.årsak = årsak;
        this.periode = datoIntervall;
    }

    public static UtsettelsePeriodeDto ferie(LocalDate fom, LocalDate tom) {
        return new UtsettelsePeriodeDto(fom, tom);
    }

    public static UtsettelsePeriodeDto utsettelse(LocalDate fom, LocalDate tom, UtsettelseÅrsak årsak) {
        return new UtsettelsePeriodeDto(fom, tom, årsak);
    }

    public static UtsettelsePeriodeDto utsettelse(DatoIntervallEntitet datoIntervall, UtsettelseÅrsak årsak) {
        return new UtsettelsePeriodeDto(datoIntervall, årsak);
    }

    public String getIndexKey() {
        return IndexKey.createKey(årsak, periode);
    }

    /**
     * Perioden som utsettes
     * @return perioden
     */
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    /**
     * Årsaken til utsettelsen
     * @return utsettelseårsaken
     */
    public UtsettelseÅrsak getÅrsak() {
        return årsak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof UtsettelsePeriodeDto)) return false;
        UtsettelsePeriodeDto that = (UtsettelsePeriodeDto) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(årsak, that.årsak);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, årsak);
    }

    @Override
    public String toString() {
        return "UtsettelsePeriodeEntitet{" +
            "periode=" + periode +
            ", årsak=" + årsak +
            '}';
    }
}
