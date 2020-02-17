package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelseGrunnlagDto {

    @JsonProperty(value = "ytelseStørrelse")
    @Valid
    private List<YtelseStørrelseDto> ytelseStørrelse;

    @JsonProperty(value = "arbeidskategori")
    @Valid
    private Arbeidskategori arbeidskategori;

    @JsonProperty(value = "dekningsgradProsent")
    @Valid
    private BigDecimal dekningsgradProsent;

    @JsonProperty(value = "graderingProsent")
    @Valid
    private BigDecimal graderingProsent;

    @JsonProperty(value = "inntektProsent")
    @Valid
    private BigDecimal inntektProsent;

    @JsonProperty(value = "opprinneligIdentdato")
    @Valid
    private LocalDate opprinneligIdentdato;

    @JsonProperty(value = "vedtaksDagsats")
    @Valid
    private BeløpDto vedtaksDagsats;

    public YtelseGrunnlagDto(@Valid List<YtelseStørrelseDto> ytelseStørrelse, @Valid Arbeidskategori arbeidskategori, @Valid BigDecimal dekningsgradProsent, @Valid BigDecimal graderingProsent, @Valid BigDecimal inntektProsent, @Valid LocalDate opprinneligIdentdato, @Valid BeløpDto vedtaksDagsats) {
        this.ytelseStørrelse = ytelseStørrelse;
        this.arbeidskategori = arbeidskategori;
        this.dekningsgradProsent = dekningsgradProsent;
        this.graderingProsent = graderingProsent;
        this.inntektProsent = inntektProsent;
        this.opprinneligIdentdato = opprinneligIdentdato;
        this.vedtaksDagsats = vedtaksDagsats;
    }

    public YtelseGrunnlagDto() {
        // default ctor
    }

    public List<YtelseStørrelseDto> getYtelseStørrelse() {
        return ytelseStørrelse;
    }

    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }

    public BigDecimal getDekningsgradProsent() {
        return dekningsgradProsent;
    }

    public BigDecimal getGraderingProsent() {
        return graderingProsent;
    }

    public BigDecimal getInntektProsent() {
        return inntektProsent;
    }

    public LocalDate getOpprinneligIdentdato() {
        return opprinneligIdentdato;
    }

    public BeløpDto getVedtaksDagsats() {
        return vedtaksDagsats;
    }
}
