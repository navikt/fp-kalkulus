package no.nav.folketrygdloven.kalkulus.kodeverk;

import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT_2;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_VILKÅR;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BeregningsgrunnlagPeriodeRegelType implements Kodeverdi, DatabaseKode {
    FORESLÅ("FORESLÅ", "Foreslå beregningsgrunnlag", FORESLÅTT),
    FORESLÅ_2("FORESLÅ_2", "Foreslå beregningsgrunnlag del 2", FORESLÅTT_2),
    VILKÅR_VURDERING("VILKÅR_VURDERING", "Vurder beregningsvilkår", VURDERT_VILKÅR),
    FORDEL("FORDEL", "Fordel beregningsgrunnlag", OPPDATERT_MED_REFUSJON_OG_GRADERING),
    FASTSETT("FASTSETT", "Fastsett/fullføre beregningsgrunnlag", FASTSATT),
    @Deprecated
    OPPDATER_GRUNNLAG_SVP("OPPDATER_GRUNNLAG_SVP", "Oppdater grunnlag for SVP", FASTSATT),
    @Deprecated
    FASTSETT2("FASTSETT2", "Fastsette/fullføre beregningsgrunnlag for andre gangs kjøring for SVP", FASTSATT),
    FINN_GRENSEVERDI("FINN_GRENSEVERDI", "Finne grenseverdi til kjøring av fastsett beregningsgrunnlag for SVP", FASTSATT),
    UDEFINERT(KodeKonstanter.UDEFINERT, "Ikke definert", BeregningsgrunnlagTilstand.UDEFINERT),
    ;

    private static final Map<String, BeregningsgrunnlagPeriodeRegelType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private final String navn;
    @JsonValue
    private final String kode;
    @JsonIgnore
    private final BeregningsgrunnlagTilstand lagretTilstand;

    BeregningsgrunnlagPeriodeRegelType(String kode, String navn, BeregningsgrunnlagTilstand lagretTilstand) {
        this.kode = kode;
        this.navn = navn;
        this.lagretTilstand = lagretTilstand;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static BeregningsgrunnlagPeriodeRegelType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(BeregningsgrunnlagPeriodeRegelType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BeregningsgrunnlagPeriodeRegelType: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

    public static BeregningsgrunnlagPeriodeRegelType fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

    public BeregningsgrunnlagTilstand getLagretTilstand() {
        return lagretTilstand;
    }

}
