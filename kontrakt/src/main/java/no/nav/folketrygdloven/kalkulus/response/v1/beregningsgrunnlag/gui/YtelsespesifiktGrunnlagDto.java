package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnGrunnlagDto;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonTypeInfo(
        use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="ytelsetype")
@JsonSubTypes({
        @JsonSubTypes.Type(value= FrisinnGrunnlagDto.class, name= FrisinnGrunnlagDto.YTELSETYPE),
})
public abstract class YtelsespesifiktGrunnlagDto {

    public YtelsespesifiktGrunnlagDto() {
        // Jackson
    }
}

