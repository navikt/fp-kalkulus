package no.nav.folketrygdloven.kalkulus.request.v1.forvaltning;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class OppdaterYtelsesspesifiktGrunnlagForRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "ytelsespesifiktGrunnlag")
    @Valid
    private YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag;


    @JsonCreator
    public OppdaterYtelsesspesifiktGrunnlagForRequest(@JsonProperty(value = "eksternReferanse", required = true) UUID eksternReferanse,
                                                      @JsonProperty(value = "ytelsespesifiktGrunnlag") YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag) {
        this.eksternReferanse = eksternReferanse;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public YtelsespesifiktGrunnlagDto getYtelsespesifiktGrunnlag() {
        return ytelsespesifiktGrunnlag;
    }
}
