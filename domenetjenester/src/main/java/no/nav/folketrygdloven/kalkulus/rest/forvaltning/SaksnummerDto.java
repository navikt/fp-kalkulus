package no.nav.folketrygdloven.kalkulus.rest.forvaltning;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY, isGetterVisibility = Visibility.NONE)
public class SaksnummerDto implements AbacDto {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Size(max = 19)
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$")
    private final String saksnummer;


    @JsonCreator
    public SaksnummerDto(@JsonProperty("saksnummer") @NotNull @Size(max = 19) @Pattern(regexp = "^[a-zA-Z0-9]*$") String saksnummer) {
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        var other = (SaksnummerDto) obj;
        return Objects.equals(saksnummer, other.saksnummer);
    }

    public Saksnummer getVerdi() {
        return new Saksnummer(saksnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saksnummer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '<' + "" + saksnummer + '>';
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        final var abacDataAttributter = AbacDataAttributter.opprett();
        abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getVerdi().getVerdi());
        return abacDataAttributter;
    }

}
