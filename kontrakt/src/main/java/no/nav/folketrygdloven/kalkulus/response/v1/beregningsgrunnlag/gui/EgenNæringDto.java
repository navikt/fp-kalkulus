package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class EgenNæringDto {

    @Valid
    @JsonProperty("utenlandskvirksomhetsnavn")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String utenlandskvirksomhetsnavn;

    @Valid
    @JsonProperty("orgnr")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String orgnr;

    @Valid
    @JsonProperty("erVarigEndret")
    private boolean erVarigEndret;

    @Valid
    @JsonProperty("erNyoppstartet")
    private boolean erNyoppstartet;

    @Valid
    @JsonProperty("virksomhetType")
    private VirksomhetType virksomhetType;

    @Valid
    @JsonProperty("begrunnelse")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String begrunnelse;

    @Valid
    @JsonProperty("endringsdato")
    private LocalDate endringsdato;

    @Valid
    @JsonProperty("oppstartsdato")
    private LocalDate oppstartsdato;

    @Valid
    @JsonProperty("regnskapsførerNavn")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String regnskapsførerNavn;

    @Valid
    @JsonProperty("regnskapsførerTlf")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String regnskapsførerTlf;

    @Valid
    @JsonProperty("kanRegnskapsførerKontaktes")
    private boolean kanRegnskapsførerKontaktes;

    @Valid
    @JsonProperty("erNyIArbeidslivet")
    private boolean erNyIArbeidslivet;

    @Valid
    @JsonProperty("oppgittInntekt")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal oppgittInntekt;

    public EgenNæringDto() {
        // Jackson
    }

    public String getOrgnr() {
        return orgnr;
    }

    public void setOrgnr(String orgnr) {
        this.orgnr = orgnr;
    }

    public boolean isErVarigEndret() {
        return erVarigEndret;
    }

    public void setErVarigEndret(boolean erVarigEndret) {
        this.erVarigEndret = erVarigEndret;
    }

    public boolean isErNyoppstartet() {
        return erNyoppstartet;
    }

    public void setErNyoppstartet(boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

    public VirksomhetType getVirksomhetType() {
        return virksomhetType;
    }

    public void setVirksomhetType(VirksomhetType virksomhetType) {
        this.virksomhetType = virksomhetType;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public LocalDate getEndringsdato() {
        return endringsdato;
    }

    public void setEndringsdato(LocalDate endringsdato) {
        this.endringsdato = endringsdato;
    }

    public LocalDate getOppstartsdato() {
        return oppstartsdato;
    }

    public void setOppstartsdato(LocalDate oppstartsdato) {
        this.oppstartsdato = oppstartsdato;
    }

    public String getRegnskapsførerNavn() {
        return regnskapsførerNavn;
    }

    public void setRegnskapsførerNavn(String regnskapsførerNavn) {
        this.regnskapsførerNavn = regnskapsførerNavn;
    }

    public String getRegnskapsførerTlf() {
        return regnskapsførerTlf;
    }

    public void setRegnskapsførerTlf(String regnskapsførerTlf) {
        this.regnskapsførerTlf = regnskapsførerTlf;
    }

    public boolean isKanRegnskapsførerKontaktes() {
        return kanRegnskapsførerKontaktes;
    }

    public void setKanRegnskapsførerKontaktes(boolean kanRegnskapsførerKontaktes) {
        this.kanRegnskapsførerKontaktes = kanRegnskapsførerKontaktes;
    }

    public boolean isErNyIArbeidslivet() {
        return erNyIArbeidslivet;
    }

    public void setErNyIArbeidslivet(boolean erNyIArbeidslivet) {
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public BigDecimal getOppgittInntekt() {
        return oppgittInntekt;
    }

    public void setOppgittInntekt(BigDecimal oppgittInntekt) {
        this.oppgittInntekt = oppgittInntekt;
    }
}
