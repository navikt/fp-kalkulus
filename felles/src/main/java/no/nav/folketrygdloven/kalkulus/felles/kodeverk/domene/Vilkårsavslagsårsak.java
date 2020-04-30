package no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public enum Vilkårsavslagsårsak {

    ATFL_SAMME_ORG("ATFL_SAMME_ORG"),
    SØKT_FL_INGEN_FL_INNTEKT("SØKT_FL_INGEN_FL_INNTEKT"),
    FOR_LAVT_BG("FOR_LAVT_BG");

    @JsonValue
    private final String kode;

    Vilkårsavslagsårsak(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
