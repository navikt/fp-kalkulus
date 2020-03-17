package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.folketrygdloven.kalkulus.kodeverk.Kodeverk;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kodeverk", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InntektskategoriEndring.class, name = InntektskategoriEndring.KODEVERK),
})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class KodeverdiEndring {


    public abstract Kodeverk getFraVerdi();

    public abstract Kodeverk getTilVerdi();

    /**
     * Kodeverk - må matche kodeverk property generert for klassen.
     */
    public abstract String getKodeverk();

}
