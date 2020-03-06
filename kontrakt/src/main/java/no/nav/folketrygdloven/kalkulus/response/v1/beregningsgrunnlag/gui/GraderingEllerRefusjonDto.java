package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class GraderingEllerRefusjonDto {

    @Valid
    @JsonProperty("erRefusjon")
    private boolean erRefusjon;

    @Valid
    @JsonProperty("erGradering")
    private boolean erGradering;

    @Valid
    @JsonProperty("fom")
    private LocalDate fom;

    @Valid
    @JsonProperty("tom")
    private LocalDate tom;

    public GraderingEllerRefusjonDto(boolean erRefusjon, boolean erGradering) {
        if ((erRefusjon && erGradering) || (!erRefusjon && !erGradering)) {
            throw new IllegalArgumentException("Må gjelde enten gradering eller refusjon");
        }
        this.erGradering = erGradering;
        this.erRefusjon = erRefusjon;
    }

    public boolean isErRefusjon() {
        return erRefusjon;
    }

    public void setErRefusjon(boolean erRefusjon) {
        this.erRefusjon = erRefusjon;
    }

    public boolean isErGradering() {
        return erGradering;
    }

    public void setErGradering(boolean erGradering) {
        this.erGradering = erGradering;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }
}
