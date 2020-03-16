package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelserDto {

    @JsonProperty("ytelser")
    @Size(min = 1)
    @Valid
    private List<YtelseDto> ytelser;

    protected YtelserDto() {
        // default ctor
    }

    public YtelserDto(@NotNull @Valid List<YtelseDto> ytelser) {
        this.ytelser = ytelser;
    }

    public List<YtelseDto> getYtelser() {
        return ytelser;
    }
}
