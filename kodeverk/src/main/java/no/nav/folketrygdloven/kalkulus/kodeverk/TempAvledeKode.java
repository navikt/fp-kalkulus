package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * for avledning av kode for enum som ikke er mappet direkte på navn der både ny (@JsonValue) og gammel (@JsonProperty kode + kodeverk) kan
 * bli sendt. Brukes til eksisterende kode er konvertert til @JsonValue på alle grensesnitt.
 *
 * <h3>Eksempel - {@link BehandlingType}</h3>
 * <b>Gammel</b>: {"kode":"BT-004","kodeverk":"BEHANDLING_TYPE"}
 * <p>
 * <b>Ny</b>: "BT-004"
 * <p>
 *
 * @deprecated endre grensesnitt til @JsonValue istdf @JsonProperty + @JsonCreator
 */
@Deprecated(since = "2020-12-09")
public class TempAvledeKode {

    private static final Logger LOG = LoggerFactory.getLogger(TempAvledeKode.class);

    @SuppressWarnings("rawtypes")
    public static String getVerdi(Class<? extends Enum> enumCls, Object node, String key) {
        var kode = switch (node) {
            case String s -> s;
            case JsonNode jsonNode -> jsonNode.get(key).asText();
            case Map map -> (String) map.get(key);
            default -> throw new IllegalArgumentException("Støtter ikke node av type: " + node.getClass() + " for enum:" + enumCls.getName());
        };
        if (!(node instanceof String) && LOG.isDebugEnabled()) {
            try {
                throw new IllegalArgumentException("Kodeverk");
            } catch (Exception e) {
                var melding = String.format("KODEVERK-OBJEKT-KALKULUS: mottok kodeverdi som objekt - kode %s fra kodeverk %s", kode, enumCls.getName());
                LOG.debug(melding, e);
            }
        }
        return kode;
    }

    @SuppressWarnings("rawtypes")
    public static BigDecimal getBeløp(Object node) {
        var asBigDecimal = switch (node) {
            case Integer i -> new BigDecimal(i);
            case Double d -> BigDecimal.valueOf(d);
            case BigDecimal bd -> bd;
            case Number n -> new BigDecimal(n.toString());
            case String s -> new BigDecimal(s);
            case JsonNode jsonNode -> new BigDecimal(jsonNode.get("verdi").asText());
            case Map map -> !map.isEmpty() && map.get("verdi") != null ? new BigDecimal(String.valueOf(map.get("verdi"))) : null;
            default -> throw new IllegalArgumentException("Støtter ikke node av type: " + node.getClass());
        };
        if (!(node instanceof Number) && LOG.isDebugEnabled()) {
            try {
                throw new IllegalArgumentException("Beløp/Verdi");
            } catch (Exception e) {
                var melding = String.format("BELØP-OBJEKT-KALKULUS: mottok beløp som objekt av type %s", node.getClass());
                LOG.debug(melding, e);
            }
        }
        return asBigDecimal;
    }

}
