package no.nav.folketrygdloven.kalkulus.rest.abac;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.request.v1.HentGrunnbeløpRequest;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;

/**
 * Json bean med Abac.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class HentGrunnbeløpRequestAbacDto extends HentGrunnbeløpRequest implements no.nav.vedtak.sikkerhet.abac.AbacDto {


    @JsonCreator
    public HentGrunnbeløpRequestAbacDto(@JsonProperty(value = "dato", required = true) @Valid @NotNull LocalDate dato) {
        super(dato);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        final var abacDataAttributter = AbacDataAttributter.opprett();
        return abacDataAttributter;
    }
}
