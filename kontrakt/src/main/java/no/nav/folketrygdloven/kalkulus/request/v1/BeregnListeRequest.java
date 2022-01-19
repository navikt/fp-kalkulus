package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

/**
 * Spesifikasjon for å fortsette en beregning.
 * <p>
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class BeregnListeRequest implements KalkulusRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes;

    @JsonProperty(value = "stegType", required = true)
    @NotNull
    @Valid
    private StegType stegType;

    @JsonProperty(value = "beregnForListe")
    @Size(min=1)
    @Valid
    private List<BeregnForRequest> beregnForListe;

    @JsonProperty(value = "fordelBeregningListe")
    @Size(min=1)
    @Valid
    private List<BeregnForRequest> fordelBeregningListe;

    protected BeregnListeRequest() {
    }

    @JsonCreator
    public BeregnListeRequest(@JsonProperty(value = "saksnummer", required = true) String saksnummer,
                              @JsonProperty(value = "aktør", required = true) PersonIdent aktør,
                              @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                              @JsonProperty(value = "stegType", required = true) StegType stegType,
                              @JsonProperty(value = "beregnForListe") List<BeregnForRequest> beregnForListe) {
        this.saksnummer = saksnummer;
        this.aktør = aktør;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.stegType = stegType;
        this.beregnForListe = beregnForListe;
    }

    @Override
    public String getSaksnummer() {
        return saksnummer;
    }

    public PersonIdent getAktør() {
        return aktør;
    }

    public YtelseTyperKalkulusStøtterKontrakt getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public StegType getStegType() {
        return stegType;
    }

    public List<BeregnForRequest> getBeregnForListe() {
        return fordelBeregningListe != null ? fordelBeregningListe : beregnForListe;
    }

    @AssertTrue
    public boolean skalHaInputForFørsteSteg() {
        return stegType != StegType.FASTSETT_STP_BER || beregnForListe.stream().noneMatch(r -> r.getKalkulatorInput() == null);
    }



}
