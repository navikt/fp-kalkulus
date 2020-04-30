package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.folketrygdloven.kalkulus.kodeverk.Kodeverk;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Vilkårsavslagsårsak extends Kodeverk {

    public static final String AVSLAGSÅRSAK = "AVSLAGSÅRSAK";

    public Vilkårsavslagsårsak(String kode) {
        this.kode = kode;
    }

    @JsonValue
    private final String kode;

    public String getKode() {
        return kode;
    }

    @Override
    public String getKodeverk() {
        return AVSLAGSÅRSAK;
    }
}
