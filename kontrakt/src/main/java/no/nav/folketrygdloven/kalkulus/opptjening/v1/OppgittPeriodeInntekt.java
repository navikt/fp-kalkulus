package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

public interface OppgittPeriodeInntekt {

    public Periode getPeriode();

    public BigDecimal getInntekt();
}
