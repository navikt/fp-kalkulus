package no.nav.folketrygdloven.kalkulus.felles.v1;

import java.util.concurrent.atomic.AtomicLong;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktørId  {

    @JsonProperty(value = "aktørId", required = true, index = 1)
    @NotNull
    @Pattern(regexp = "^\\d{13}+$", message = "aktørId ${validatedValue} har ikke gyldig verdi (13 siffer)")
    private String aktørId;

    @JsonCreator
    public AktørId(@JsonProperty(value = "aktørId", required=true, index=1) String kode) {
        this.aktørId = kode;
    }

    public String getAktørId() {
        return aktørId;
    }

}
