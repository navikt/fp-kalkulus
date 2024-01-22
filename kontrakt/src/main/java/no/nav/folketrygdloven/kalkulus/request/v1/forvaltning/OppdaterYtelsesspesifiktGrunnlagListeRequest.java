package no.nav.folketrygdloven.kalkulus.request.v1.forvaltning;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.request.v1.KalkulusRequest;

/**
 * Spesifikasjon for å sende inn oppdatert informasjon for ytelsesspesifikt grunnlag.
 * <p>
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class OppdaterYtelsesspesifiktGrunnlagListeRequest implements KalkulusRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    private UUID behandlingUuid;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private FagsakYtelseType ytelseSomSkalBeregnes;

    @JsonProperty(value = "ytelsespesifiktGrunnlagListe")
    @Size(min = 1)
    @Valid
    private List<OppdaterYtelsesspesifiktGrunnlagForRequest> ytelsespesifiktGrunnlagListe;


    protected OppdaterYtelsesspesifiktGrunnlagListeRequest() {
    }

    @JsonCreator
    public OppdaterYtelsesspesifiktGrunnlagListeRequest(@JsonProperty(value = "saksnummer", required = true) String saksnummer,
                                                        @JsonProperty(value = "behandlingUuid") UUID behandlingUuid,
                                                        @JsonProperty(value = "aktør", required = true) PersonIdent aktør,
                                                        @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) FagsakYtelseType ytelseSomSkalBeregnes,
                                                        @JsonProperty(value = "ytelsespesifiktGrunnlagListe", required = true) List<OppdaterYtelsesspesifiktGrunnlagForRequest> ytelsespesifiktGrunnlagListe) {
        this.saksnummer = saksnummer;
        this.behandlingUuid = behandlingUuid;
        this.aktør = aktør;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.ytelsespesifiktGrunnlagListe = ytelsespesifiktGrunnlagListe;
    }

    @Override
    public String getSaksnummer() {
        return saksnummer;
    }

    @Override
    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public PersonIdent getAktør() {
        return aktør;
    }

    public FagsakYtelseType getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public List<OppdaterYtelsesspesifiktGrunnlagForRequest> getYtelsespesifiktGrunnlagListe() {
        return ytelsespesifiktGrunnlagListe;
    }
}
