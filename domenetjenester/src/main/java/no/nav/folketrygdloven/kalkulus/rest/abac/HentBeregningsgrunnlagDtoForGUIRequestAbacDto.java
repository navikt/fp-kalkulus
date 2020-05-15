package no.nav.folketrygdloven.kalkulus.rest.abac;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdReferanseDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoForGUIRequest;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Json bean med Abac.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class HentBeregningsgrunnlagDtoForGUIRequestAbacDto extends HentBeregningsgrunnlagDtoForGUIRequest implements no.nav.vedtak.sikkerhet.abac.AbacDto {


    public HentBeregningsgrunnlagDtoForGUIRequestAbacDto() {
        // For Json deserialisering
    }

    @Deprecated
    public HentBeregningsgrunnlagDtoForGUIRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                                         @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                         @JsonProperty(value = "arbeidsgiverOpplysninger", required = true) @NotNull @Valid List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        super(eksternReferanse, ytelseSomSkalBeregnes, arbeidsgiverOpplysninger);
    }

    public HentBeregningsgrunnlagDtoForGUIRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                                         @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                         @JsonProperty(value = "arbeidsgiverOpplysninger", required = true) @NotNull @Valid List<no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger,
                                                         @JsonProperty(value = "referanser") @Valid Set<ArbeidsforholdReferanseDto> referanser) {
        super(eksternReferanse, ytelseSomSkalBeregnes, arbeidsgiverOpplysninger, referanser);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        final var abacDataAttributter = AbacDataAttributter.opprett();
        abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse());
        return abacDataAttributter;
    }
}
