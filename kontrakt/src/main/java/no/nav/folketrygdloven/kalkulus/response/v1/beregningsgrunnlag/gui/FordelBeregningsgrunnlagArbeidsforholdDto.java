package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FordelBeregningsgrunnlagArbeidsforholdDto extends BeregningsgrunnlagArbeidsforholdDto {

    @JsonProperty(value = "perioderMedGraderingEllerRefusjon")
    private List<GraderingEllerRefusjonDto> perioderMedGraderingEllerRefusjon = new ArrayList<>();

    @JsonProperty(value = "permisjon")
    private PermisjonDto permisjon;

    public void leggTilPeriodeMedGraderingEllerRefusjon(GraderingEllerRefusjonDto periodeMedGraderingEllerRefusjon) {
        this.perioderMedGraderingEllerRefusjon.add(periodeMedGraderingEllerRefusjon);
    }

    public List<GraderingEllerRefusjonDto> getPerioderMedGraderingEllerRefusjon() {
        return perioderMedGraderingEllerRefusjon;
    }

    public void setPerioderMedGraderingEllerRefusjon(List<GraderingEllerRefusjonDto> perioderMedGraderingEllerRefusjon) {
        this.perioderMedGraderingEllerRefusjon = perioderMedGraderingEllerRefusjon;
    }

    public PermisjonDto getPermisjon() {
        return permisjon;
    }

    public void setPermisjon(PermisjonDto permisjon) {
        this.permisjon = permisjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FordelBeregningsgrunnlagArbeidsforholdDto that = (FordelBeregningsgrunnlagArbeidsforholdDto) o;
        return Objects.equals(perioderMedGraderingEllerRefusjon, that.perioderMedGraderingEllerRefusjon)
            && Objects.equals(permisjon, that.permisjon);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), perioderMedGraderingEllerRefusjon);
    }
}
