package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidType;

public class AktørArbeidDto {

    private AktørId aktørId;

    private Set<YrkesaktivitetDto> yrkesaktiviter = new LinkedHashSet<>();

    AktørArbeidDto() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørArbeidDto(AktørArbeidDto aktørArbeid) {
        this.aktørId = aktørArbeid.getAktørId();

        this.yrkesaktiviter = aktørArbeid.yrkesaktiviter.stream().map(yrkesaktivitet -> {
            var yr = new YrkesaktivitetDto(yrkesaktivitet);
            return yr;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public String getIndexKey() {
        return IndexKey.createKey(getAktørId());
    }

    /**
     * Aktøren som avtalene gjelder for
     * @return aktørId
     */
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    /** Ufiltrert liste av yrkesaktiviteter. */
    public Collection<YrkesaktivitetDto> hentAlleYrkesaktiviteter() {
        return Set.copyOf(yrkesaktiviter);
    }

    boolean hasValues() {
        return aktørId != null || yrkesaktiviter != null;
    }

    YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForNøkkel(OpptjeningsnøkkelDto identifikator, ArbeidType arbeidType) {
        Optional<YrkesaktivitetDto> yrkesaktivitet = yrkesaktiviter.stream()
            .filter(ya -> ya.getArbeidType().equals(arbeidType) && new OpptjeningsnøkkelDto(ya).equals(identifikator))
            .findFirst();
        final YrkesaktivitetDtoBuilder oppdatere = YrkesaktivitetDtoBuilder.oppdatere(yrkesaktivitet);
        oppdatere.medArbeidType(arbeidType);
        return oppdatere;
    }

    YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForNøkkel(OpptjeningsnøkkelDto identifikator, Set<ArbeidType> arbeidTyper) {
        Optional<YrkesaktivitetDto> yrkesaktivitet = yrkesaktiviter.stream()
            .filter(ya -> arbeidTyper.contains(ya.getArbeidType()) && new OpptjeningsnøkkelDto(ya).equals(identifikator))
            .findFirst();
        final YrkesaktivitetDtoBuilder oppdatere = YrkesaktivitetDtoBuilder.oppdatere(yrkesaktivitet);
        if (!oppdatere.getErOppdatering()) {
            // Defaulter til ordinert arbeidsforhold hvis saksbehandler har lagt til fra GUI
            oppdatere.medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        }
        return oppdatere;
    }

    YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForType(ArbeidType type) {
        Optional<YrkesaktivitetDto> yrkesaktivitet = yrkesaktiviter.stream()
            .filter(ya -> ya.getArbeidType().equals(type))
            .findFirst();
        final YrkesaktivitetDtoBuilder oppdatere = YrkesaktivitetDtoBuilder.oppdatere(yrkesaktivitet);
        oppdatere.medArbeidType(type);
        return oppdatere;
    }

    void leggTilYrkesaktivitet(YrkesaktivitetDto yrkesaktivitet) {
        this.yrkesaktiviter.add(yrkesaktivitet);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørArbeidDto)) {
            return false;
        }
        AktørArbeidDto other = (AktørArbeidDto) obj;
        return Objects.equals(this.getAktørId(), other.getAktørId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "aktørId=" + aktørId +
            ", yrkesaktiviteter=" + yrkesaktiviter +
            '>';
    }

}
