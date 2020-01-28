package no.nav.folketrygdloven.kalkulator.modell.iay;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class InntektArbeidYtelseAggregatDto {

    private UUID uuid;
    private Set<AktørInntektDto> aktørInntekt = new LinkedHashSet<>();
    private Set<AktørArbeidDto> aktørArbeid = new LinkedHashSet<>();
    private Set<AktørYtelseDto> aktørYtelse = new LinkedHashSet<>();

    InntektArbeidYtelseAggregatDto() {
        // hibernate
    }

    InntektArbeidYtelseAggregatDto(UUID angittEksternReferanse, LocalDateTime angittOpprettetTidspunkt) {
        uuid = angittEksternReferanse;
    }

    /** copy constructor men med angitt referanse og tidspunkt. Hvis unikt kan denne instansen brukes til lagring. */
    InntektArbeidYtelseAggregatDto(UUID eksternReferanse, LocalDateTime opprettetTidspunkt, InntektArbeidYtelseAggregatDto kopierFra) {
        this.setAktørInntekt(kopierFra.getAktørInntekt().stream().map(ai -> {
            AktørInntektDto aktørInntekt = new AktørInntektDto(ai);
            return aktørInntekt;
        }).collect(Collectors.toList()));

        this.setAktørArbeid(kopierFra.getAktørArbeid().stream().map(aktørArbied -> {
            AktørArbeidDto aktørArbeid = new AktørArbeidDto(aktørArbied);
            return aktørArbeid;
        }).collect(Collectors.toList()));

        this.setAktørYtelse(kopierFra.getAktørYtelse().stream().map(ay -> {
            AktørYtelseDto aktørYtelse = new AktørYtelseDto(ay);
            return aktørYtelse;
        }).collect(Collectors.toList()));

        this.uuid = eksternReferanse;

    }

    public Collection<AktørInntektDto> getAktørInntekt() {
        return Collections.unmodifiableSet(aktørInntekt);
    }

    void setAktørInntekt(Collection<AktørInntektDto> aktørInntekt) {
        this.aktørInntekt = new LinkedHashSet<>(aktørInntekt);
    }

    void leggTilAktørInntekt(AktørInntektDto aktørInntekt) {
        this.aktørInntekt.add(aktørInntekt);
    }

    void leggTilAktørArbeid(AktørArbeidDto aktørArbeid) {
        this.aktørArbeid.add(aktørArbeid);
    }

    void leggTilAktørYtelse(AktørYtelseDto aktørYtelse) {
        this.aktørYtelse.add(aktørYtelse);
    }

    public Collection<AktørArbeidDto> getAktørArbeid() {
        return Collections.unmodifiableSet(aktørArbeid);
    }

    void setAktørArbeid(Collection<AktørArbeidDto> aktørArbeid) {
        this.aktørArbeid = new LinkedHashSet<>(aktørArbeid);
    }

    public Collection<AktørYtelseDto> getAktørYtelse() {
        return Collections.unmodifiableSet(aktørYtelse);
    }

    void setAktørYtelse(Collection<AktørYtelseDto> aktørYtelse) {
        this.aktørYtelse = new LinkedHashSet<>(aktørYtelse);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof InntektArbeidYtelseAggregatDto)) {
            return false;
        }
        InntektArbeidYtelseAggregatDto other = (InntektArbeidYtelseAggregatDto) obj;
        return Objects.equals(this.getAktørInntekt(), other.getAktørInntekt())
            && Objects.equals(this.getAktørArbeid(), other.getAktørArbeid())
            && Objects.equals(this.getAktørYtelse(), other.getAktørYtelse());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørInntekt, aktørArbeid, aktørYtelse);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "aktørInntekt=" + aktørInntekt +
            ", aktørArbeid=" + aktørArbeid +
            ", aktørYtelse=" + aktørYtelse +
            '>';
    }

}
