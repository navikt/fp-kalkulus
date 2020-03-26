package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdReferanseDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;


/**
 * Spesifikasjon for å hente beregningsgrunnlagDto for GUI.
 * Henter DTO-struktur som brukes av beregning i frontend
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class HentBeregningsgrunnlagDtoForGUIRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;


    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes;

    @JsonProperty(value = "arbeidsgiverOpplysninger", required = true)
    @NotNull
    @Valid
    private List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger;

    @JsonProperty(value = "referanser")
    @Valid
    private Set<ArbeidsforholdReferanseDto> referanser;

    protected HentBeregningsgrunnlagDtoForGUIRequest() {
        // default ctor
    }

    public HentBeregningsgrunnlagDtoForGUIRequest(@Valid @NotNull UUID eksternReferanse,
                                                  @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                  @NotNull @Valid List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger,
                                                  @Valid Set<ArbeidsforholdReferanseDto> referanser) {
        this.eksternReferanse = eksternReferanse;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.arbeidsgiverOpplysninger = arbeidsgiverOpplysninger;
        this.referanser = referanser;
    }

    @Deprecated
    public HentBeregningsgrunnlagDtoForGUIRequest(@Valid @NotNull UUID eksternReferanse,
                                                  @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                  @NotNull @Valid List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {

        this.eksternReferanse = eksternReferanse;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.arbeidsgiverOpplysninger = arbeidsgiverOpplysninger;
    }

    public UUID getKoblingReferanse() {
        return eksternReferanse;
    }

    public YtelseTyperKalkulusStøtterKontrakt getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public List<ArbeidsgiverOpplysningerDto> getArbeidsgiverOpplysninger() {
        return arbeidsgiverOpplysninger;
    }

    public Set<ArbeidsforholdReferanseDto> getReferanser() {
        return referanser;
    }
}
