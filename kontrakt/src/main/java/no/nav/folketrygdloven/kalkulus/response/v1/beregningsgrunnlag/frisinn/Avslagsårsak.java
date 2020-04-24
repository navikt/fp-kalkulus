package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public enum Avslagsårsak {

    INGEN_FRILANS_I_PERIODE_UTEN_YTELSE("INGEN_FRILANS_I_PERIODE_UTEN_YTELSE"),
    AVKORTET_GRUNNET_LØPENDE_INNTEKT("AVKORTET_GRUNNET_LØPENDE_INNTEKT"),
    AVKORTET_GRUNNET_ANNEN_INNTEKT("AVKORTET_GRUNNET_ANNEN_INNTEKT");

    @JsonValue
    private final String kode;

    Avslagsårsak(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
