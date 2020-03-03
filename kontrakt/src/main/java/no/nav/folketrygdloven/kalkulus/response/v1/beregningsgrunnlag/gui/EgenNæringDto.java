package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class EgenNæringDto {

    @JsonProperty("utenlandskvirksomhetsnavn")
    private String utenlandskvirksomhetsnavn;

    @JsonProperty("orgnr")
    private String orgnr;

    @JsonProperty("erVarigEndret")
    private boolean erVarigEndret;

    @JsonProperty("erNyoppstartet")
    private boolean erNyoppstartet;

    @JsonProperty("virksomhetType")
    private VirksomhetType virksomhetType;

    @JsonProperty("begrunnelse")
    private String begrunnelse;

    @JsonProperty("endringsdato")
    private LocalDate endringsdato;

    @JsonProperty("oppstartsdato")
    private LocalDate oppstartsdato;

    @JsonProperty("regnskapsførerNavn")
    private String regnskapsførerNavn;

    @JsonProperty("regnskapsførerTlf")
    private String regnskapsførerTlf;

    @JsonProperty("kanRegnskapsførerKontaktes")
    private boolean kanRegnskapsførerKontaktes;

    @JsonProperty("erNyIArbeidslivet")
    private boolean erNyIArbeidslivet;

    @JsonProperty("oppgittInntekt")
    private BigDecimal oppgittInntekt;

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
