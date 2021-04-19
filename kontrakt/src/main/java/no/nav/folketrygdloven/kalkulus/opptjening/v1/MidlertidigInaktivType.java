package no.nav.folketrygdloven.kalkulus.opptjening.v1;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum MidlertidigInaktivType {

    A("8-47 A"), B("8-47 B");

    MidlertidigInaktivType(String s) {
    }
}
