package no.nav.folketrygdloven.kalkulus.request.v1.migrerAksjonspunkt;

import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class MigrerAksjonspunktRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes;

    @JsonProperty(value = "eksternReferanseListe", required = true)
    @Valid
    @NotNull
    private Set<UUID> eksternReferanseListe;

    @JsonProperty(value = "avklaringsbehovStatus", required = true)
    @Valid
    @NotNull
    private String avklaringsbehovStatus;

    @JsonProperty(value = "begrunnelse")
    @Valid
    private String begrunnelse;

    protected MigrerAksjonspunktRequest() {
    }

    @JsonCreator
    public MigrerAksjonspunktRequest(@JsonProperty(value = "saksnummer", required = true) String saksnummer,
                                     @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                     @JsonProperty(value = "eksternReferanseListe", required = true) Set<UUID> eksternReferanseListe,
                                     @JsonProperty(value = "avklaringsbehovStatus", required = true) String avklaringsbehovStatus,
                                     @JsonProperty(value = "begrunnelse") String begrunnelse) {
        this.saksnummer = saksnummer;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.eksternReferanseListe = eksternReferanseListe;
        this.avklaringsbehovStatus = avklaringsbehovStatus;
        this.begrunnelse = begrunnelse;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public Set<UUID> getEksternReferanseListe() {
        return eksternReferanseListe;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public String getAvklaringsbehovStatus() {
        return avklaringsbehovStatus;
    }

    public YtelseTyperKalkulusStøtterKontrakt getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    @AssertTrue(message = "Sjekk begrunnelse er satt om utført")
    boolean sjekkHarBegrunnelseOmUtført() {
        return !AvklaringsbehovStatus.UTFØRT.equals(AvklaringsbehovStatus.fraKode(avklaringsbehovStatus)) || begrunnelse != null;
    }

}
