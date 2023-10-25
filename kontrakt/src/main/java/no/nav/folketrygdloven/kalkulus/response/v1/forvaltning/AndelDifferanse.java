package no.nav.folketrygdloven.kalkulus.response.v1.forvaltning;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class AndelDifferanse {
    @JsonProperty(value = "arbeidsgiver")
    @NotNull
    @Valid
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private String arbeidsforholdRef;

    @JsonProperty(value = "nyDagsats")
    @NotNull
    @Valid
    @Min(0)
    @Max(178956970)
    private Long nyDagsats;

    @JsonProperty(value = "gammelDagsats")
    @NotNull
    @Valid
    @Min(0)
    @Max(178956970)
    private Long gammelDagsats;


    @JsonProperty(value = "nyDagsatsBruker")
    @NotNull
    @Valid
    @Min(0)
    @Max(178956970)
    private Long nyDagsatsBruker;

    @JsonProperty(value = "gammelDagsatsBruker")
    @NotNull
    @Valid
    @Min(0)
    @Max(178956970)
    private Long gammelDagsatsBruker;

    @JsonProperty(value = "nyDagsatsArbeidsgiver")
    @NotNull
    @Valid
    @Min(0)
    @Max(178956970)
    private Long nyDagsatsArbeidsgiver;

    @JsonProperty(value = "gammelDagsatsArbeidsgiver")
    @NotNull
    @Valid
    @Min(0)
    @Max(178956970)
    private Long gammelDagsatsArbeidsgiver;


    private AndelDifferanse() {
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public String getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public Long getNyDagsats() {
        return nyDagsats;
    }

    public Long getGammelDagsats() {
        return gammelDagsats;
    }

    public Long getNyDagsatsBruker() {
        return nyDagsatsBruker;
    }

    public Long getGammelDagsatsBruker() {
        return gammelDagsatsBruker;
    }

    public Long getNyDagsatsArbeidsgiver() {
        return nyDagsatsArbeidsgiver;
    }

    public Long getGammelDagsatsArbeidsgiver() {
        return gammelDagsatsArbeidsgiver;
    }

    public static Builder ny() {
        return new Builder();
    }

    public static class Builder {


        private final AndelDifferanse kladd = new AndelDifferanse();

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            this.kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            this.kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medNyDagsats(long dagsats) {
            this.kladd.nyDagsats = dagsats;
            return this;
        }


        public Builder medGammelDagsats(long dagsats) {
            this.kladd.gammelDagsats = dagsats;
            return this;
        }

        public Builder medNyDagsatsBruker(long dagsats) {
            this.kladd.nyDagsatsBruker = dagsats;
            return this;
        }

        public Builder medGammelDagsatsBruker(long dagsats) {
            this.kladd.gammelDagsatsBruker = dagsats;
            return this;
        }


        public Builder medNyDagsatsArbeidsgiver(long dagsats) {
            this.kladd.nyDagsatsArbeidsgiver = dagsats;
            return this;
        }


        public Builder medGammelDagsatsArbeidsgiver(long dagsats) {
            this.kladd.gammelDagsatsArbeidsgiver = dagsats;
            return this;
        }

        public AndelDifferanse build() {
            return this.kladd;
        }

    }


}
