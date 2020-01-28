package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.InntektPeriodeType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;

public class YtelseStørrelseDto {

    private InntektPeriodeType hyppighet = InntektPeriodeType.UDEFINERT;
    private OrgNummer virksomhetOrgnr;
    private Beløp beløp;

    public YtelseStørrelseDto() {
        // hibernate
    }

    public YtelseStørrelseDto(YtelseStørrelseDto ytelseStørrelse) {
        ytelseStørrelse.getVirksomhet().ifPresent(tidligereVirksomhet -> this.virksomhetOrgnr = tidligereVirksomhet);
        this.beløp = ytelseStørrelse.getBeløp();
        this.hyppighet = ytelseStørrelse.getHyppighet();
    }

    public String getIndexKey() {
        return IndexKey.createKey(virksomhetOrgnr);
    }

    public Optional<String> getOrgnr() {
        return Optional.ofNullable(virksomhetOrgnr == null ? null : virksomhetOrgnr.getId());
    }

    /**
     * Returner orgnr dersom virksomhet. Null ellers.
     *
     * @see #getVirksomhet()
     */
    public OrgNummer getVirksomhetOrgnr() {
        return virksomhetOrgnr;
    }

    public Optional<OrgNummer> getVirksomhet() {
        return Optional.ofNullable(virksomhetOrgnr);
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public InntektPeriodeType getHyppighet() {
        return hyppighet;
    }

    void setVirksomhet(OrgNummer virksomhetOrgnr) {
        this.virksomhetOrgnr = virksomhetOrgnr;
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    void setHyppighet(InntektPeriodeType hyppighet) {
        this.hyppighet = hyppighet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof YtelseStørrelseDto))
            return false;
        YtelseStørrelseDto that = (YtelseStørrelseDto) o;
        return Objects.equals(virksomhetOrgnr, that.virksomhetOrgnr) &&
            Objects.equals(beløp, that.beløp) &&
            Objects.equals(hyppighet, that.hyppighet);
    }

    @Override
    public int hashCode() {

        return Objects.hash(virksomhetOrgnr, beløp, hyppighet);
    }

    @Override
    public String toString() {
        return "YtelseStørrelseEntitet{" +
            "virksomhet=" + virksomhetOrgnr +
            ", beløp=" + beløp +
            ", hyppighet=" + hyppighet +
            '}';
    }

    boolean hasValues() {
        return beløp != null || hyppighet != null || virksomhetOrgnr != null;
    }
}
