package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HåndteringKode extends Kodeverk {

    public static final HåndteringKode AVKLAR_AKTIVITETER = new HåndteringKode("5052");
    public static final HåndteringKode FAKTA_OM_BEREGNING = new HåndteringKode("5058");
    public static final HåndteringKode FAKTA_OM_FORDELING = new HåndteringKode("5046");
    public static final HåndteringKode FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET_KODE = new HåndteringKode("5049");
    public static final HåndteringKode FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD_KODE = new HåndteringKode("5047");
    public static final HåndteringKode FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS_KODE = new HåndteringKode("5038");
    public static final HåndteringKode VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE_KODE = new HåndteringKode("5039");

    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Size(min = 2, max = 50)
    @NotNull
    private String kode;
    private String kodeverk;

    @JsonCreator
    public HåndteringKode(@JsonProperty(value = "kode", required = true) String kode) {
        Objects.requireNonNull(kode, "kode");
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getKodeverk() {
        return kodeverk;
    }
}
