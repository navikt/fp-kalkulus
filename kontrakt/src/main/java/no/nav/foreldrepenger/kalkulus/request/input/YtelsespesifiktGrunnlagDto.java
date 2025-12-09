package no.nav.foreldrepenger.kalkulus.request.input;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.folketrygdloven.kalkulus.kodeverk.KodeKonstanter;
import no.nav.foreldrepenger.kalkulus.request.input.foreldrepenger.ForeldrepengerGrunnlag;
import no.nav.foreldrepenger.kalkulus.request.input.svangerskapspenger.SvangerskapspengerGrunnlag;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "ytelseType", defaultImpl = Void.class)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ForeldrepengerGrunnlag.class, name = KodeKonstanter.YT_FORELDREPENGER),
        @JsonSubTypes.Type(value = SvangerskapspengerGrunnlag.class, name = KodeKonstanter.YT_SVANGERSKAPSPENGER),
})
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public abstract class YtelsespesifiktGrunnlagDto {}
