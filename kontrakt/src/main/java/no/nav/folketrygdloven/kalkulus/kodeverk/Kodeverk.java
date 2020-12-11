package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kodeverk", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RelatertYtelseType.class, name = RelatertYtelseType.KODEVERK),
        @JsonSubTypes.Type(value = StegType.class, name = StegType.KODEVERK),
        @JsonSubTypes.Type(value = UtbetaltNæringsYtelseType.class, name = UtbetaltNæringsYtelseType.KODEVERK),
        @JsonSubTypes.Type(value = UtbetaltPensjonTrygdType.class, name = UtbetaltPensjonTrygdType.KODEVERK),
        @JsonSubTypes.Type(value = UtbetaltYtelseFraOffentligeType.class, name = UtbetaltYtelseFraOffentligeType.KODEVERK),
        @JsonSubTypes.Type(value = UttakArbeidType.class, name = UttakArbeidType.KODEVERK),
})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class Kodeverk {

    protected Kodeverk() {
        // default ctor
    }

    /**
     * Kode for angitt kodeverk. Gyldige verdier og validering er per kodeverk klasse.
     */
    public abstract String getKode();

    /**
     * Kodeverk - må matche kodeverk property generert for klassen.
     */
    public abstract String getKodeverk();

    @Override
    public String toString() {
        return getClass().getSimpleName() +"<"+ getKode() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==this)return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        var other = getClass().cast(obj);
        return Objects.equals(this.getKode(), other.getKode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKode());
    }
}
