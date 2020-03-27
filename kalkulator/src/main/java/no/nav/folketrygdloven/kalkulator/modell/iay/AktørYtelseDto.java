package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

public class AktørYtelseDto {

    private AktørId aktørId;

    private Set<YtelseDto> ytelser = new LinkedHashSet<>();

    public AktørYtelseDto() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørYtelseDto(AktørYtelseDto aktørYtelse) {
        this.aktørId = aktørYtelse.getAktørId();
        this.ytelser = aktørYtelse.getAlleYtelser().stream().map(ytelse -> {
            var yt = new YtelseDto(ytelse);
            return yt;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public String getIndexKey() {
        return IndexKey.createKey(getAktørId());
    }

    /**
     * Aktøren tilstøtende ytelser gjelder for
     * @return aktørId
     */
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    /**
     * Alle registrerte tilstøende ytelser (ufiltrert).
     */
    public Collection<YtelseDto> getAlleYtelser() {
        return List.copyOf(ytelser);
    }

    boolean hasValues() {
        return aktørId != null || ytelser != null && !ytelser.isEmpty();
    }

    YtelseDtoBuilder getYtelseBuilderForType(FagsakYtelseType type, Intervall periode) {
        Optional<YtelseDto> ytelse = getAlleYtelser().stream()
            .filter(ya -> ya.getPeriode().equals(periode) && ya.getRelatertYtelseType().equals(type))
            .findFirst();
        return YtelseDtoBuilder.oppdatere(ytelse).medPeriode(periode).medYtelseType(type);
    }

    void leggTilYtelse(YtelseDto ytelse) {
        this.ytelser.add(ytelse);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørYtelseDto)) {
            return false;
        }
        AktørYtelseDto other = (AktørYtelseDto) obj;
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
            ", ytelser=" + ytelser +
            '>';
    }
}
