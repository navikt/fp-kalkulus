package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Objects;

import javax.validation.constraints.AssertTrue;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kodeverk", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InntektskildeType.class, name = InntektskildeType.KODEVERK),
        @JsonSubTypes.Type(value = AktivitetStatus.class, name = AktivitetStatus.KODEVERK),
        @JsonSubTypes.Type(value = Arbeidskategori.class, name = Arbeidskategori.KODEVERK),
        @JsonSubTypes.Type(value = ArbeidType.class, name = ArbeidType.KODEVERK),
        @JsonSubTypes.Type(value = BeregningAksjonspunkt.class, name = BeregningAksjonspunkt.KODEVERK),
        @JsonSubTypes.Type(value = BeregningVenteårsak.class, name = BeregningVenteårsak.KODEVERK),
        @JsonSubTypes.Type(value = Fagsystem.class, name = Fagsystem.KODEVERK),
        @JsonSubTypes.Type(value = FaktaOmBeregningTilfelle.class, name = FaktaOmBeregningTilfelle.KODEVERK),
        @JsonSubTypes.Type(value = HåndteringKode.class, name = HåndteringKode.KODEVERK),
        @JsonSubTypes.Type(value = Inntektskategori.class, name = Inntektskategori.KODEVERK),
        @JsonSubTypes.Type(value = InntektskildeType.class, name = InntektskildeType.KODEVERK),
        @JsonSubTypes.Type(value = InntektspostType.class, name = InntektspostType.KODEVERK),
        @JsonSubTypes.Type(value = NaturalYtelseType.class, name = NaturalYtelseType.KODEVERK),
        @JsonSubTypes.Type(value = OpptjeningAktivitetType.class, name = OpptjeningAktivitetType.KODEVERK),
        @JsonSubTypes.Type(value = PeriodeÅrsak.class, name = PeriodeÅrsak.KODEVERK),
        @JsonSubTypes.Type(value = RelatertYtelseTilstand.class, name = RelatertYtelseTilstand.KODEVERK),
        @JsonSubTypes.Type(value = RelatertYtelseType.class, name = RelatertYtelseType.KODEVERK),
        @JsonSubTypes.Type(value = SammenligningsgrunnlagType.class, name = SammenligningsgrunnlagType.KODEVERK),
        @JsonSubTypes.Type(value = SkatteOgAvgiftsregelType.class, name = SkatteOgAvgiftsregelType.KODEVERK),
        @JsonSubTypes.Type(value = StegType.class, name = StegType.KODEVERK),
        @JsonSubTypes.Type(value = TemaUnderkategori.class, name = TemaUnderkategori.KODEVERK),
        @JsonSubTypes.Type(value = UtbetaltNæringsYtelseType.class, name = UtbetaltNæringsYtelseType.KODEVERK),
        @JsonSubTypes.Type(value = UtbetaltPensjonTrygdType.class, name = UtbetaltPensjonTrygdType.KODEVERK),
        @JsonSubTypes.Type(value = UtbetaltYtelseFraOffentligeType.class, name = UtbetaltYtelseFraOffentligeType.KODEVERK),
        @JsonSubTypes.Type(value = VirksomhetType.class, name = VirksomhetType.KODEVERK),
        @JsonSubTypes.Type(value = YtelseTyperKalkulusStøtterKontrakt.class, name = YtelseTyperKalkulusStøtterKontrakt.KODEVERK),
})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class Kodeverk {

    @JacksonInject
    private KodeValidator kodeValidator;

    protected Kodeverk() {
        // default ctor
    }

    /**
     * Kode for angitt kodeverk. Gyldige verdier og validering er per kodeverk klasse.
     */
    public abstract String getKode();

    /**
     * Kodeverk - må matche kodeverk property generert for klassen.
     */
    public abstract String getKodeverk();

    @AssertTrue
    private boolean isOk() {
        return kodeValidator == null || kodeValidator.valider(getKodeverk(), getKode()) == null;
    }
    @Override
    public String toString() {
        return getKodeverk() +"<"+ getKode() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==this)return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        var other = getClass().cast(obj);
        return Objects.equals(this.getKode(), other.getKode())
                && Objects.equals(this.getKodeverk(), other.getKodeverk());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKode(), getKodeverk());
    }
}
