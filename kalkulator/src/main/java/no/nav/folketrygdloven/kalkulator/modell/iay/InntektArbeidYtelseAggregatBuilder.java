package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulator.modell.behandling.Fagsystem;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.InntektsKilde;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.RelatertYtelseType;

/**
 * Builder for å håndtere en gitt versjon {@link VersjonTypeDto} av grunnlaget.
 * <p>
 * Holder styr på om det er en oppdatering av eksisterende informasjon, om det gjelder før eller etter skjæringstidspunktet
 * og om det er registerdata eller saksbehandlers beslutninger.
 * <p>
 * NB! Viktig at denne builderen hentes fra repository for å sikre at den er rett tilstand ved oppdatering. Hvis ikke kan data gå tapt.
 */
public class InntektArbeidYtelseAggregatBuilder {

    private final InntektArbeidYtelseAggregatDto kladd;
    private final VersjonTypeDto versjon;
    private final List<ArbeidsforholdReferanseDto> nyeInternArbeidsforholdReferanser = new ArrayList<>();

    private InntektArbeidYtelseAggregatBuilder(InntektArbeidYtelseAggregatDto kladd, VersjonTypeDto versjon) {
        this.kladd = kladd;
        this.versjon = versjon;
    }

    public static InntektArbeidYtelseAggregatBuilder oppdatere(Optional<InntektArbeidYtelseAggregatDto> oppdatere, VersjonTypeDto versjon) {
        return builderFor(oppdatere, UUID.randomUUID(), LocalDateTime.now(), versjon);
    }

    private static InntektArbeidYtelseAggregatBuilder builderFor(Optional<InntektArbeidYtelseAggregatDto> kopierDataFra,
                                                                 UUID angittReferanse, LocalDateTime angittTidspunkt, VersjonTypeDto versjon) {
        return kopierDataFra
            .map(kopier -> new InntektArbeidYtelseAggregatBuilder(new InntektArbeidYtelseAggregatDto(angittReferanse, angittTidspunkt, kopier), versjon))
            .orElseGet(() -> new InntektArbeidYtelseAggregatBuilder(new InntektArbeidYtelseAggregatDto(angittReferanse, angittTidspunkt), versjon));
    }

    /**
     * Legger til inntekter for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørInntekt {@link AktørInntektBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørInntekt(AktørInntektBuilder aktørInntekt) {
        if (!aktørInntekt.getErOppdatering()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørInntekt(aktørInntekt.build());
        }
        return this;
    }

    /**
     * Legger til aktiviteter for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørArbeid {@link AktørArbeidBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørArbeid(AktørArbeidBuilder aktørArbeid) {
        if (!aktørArbeid.getErOppdatering()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørArbeid(aktørArbeid.build());
        }
        return this;
    }

    /**
     * Legger til tilstøtende ytelser for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørYtelse {@link AktørYtelseBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørYtelse(AktørYtelseBuilder aktørYtelse) {
        if (!aktørYtelse.getErOppdatering() && aktørYtelse.harVerdi()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørYtelse(aktørYtelse.build());
        }
        return this;
    }

    /**
     * Oppretter builder for aktiviteter for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørArbeidBuilder}
     */
    public AktørArbeidBuilder getAktørArbeidBuilder(AktørId aktørId) {
        Optional<AktørArbeidDto> aktørArbeid = kladd.getAktørArbeid().stream().filter(aa -> aktørId.equals(aa.getAktørId())).findFirst();
        return AktørArbeidBuilder.oppdatere(aktørArbeid).medAktørId(aktørId);
    }

    /**
     * Oppretter builder for inntekter for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørInntektBuilder}
     */
    public AktørInntektBuilder getAktørInntektBuilder(AktørId aktørId) {
        Optional<AktørInntektDto> aktørInntekt = kladd.getAktørInntekt().stream().filter(aa -> aktørId.equals(aa.getAktørId())).findFirst();
        final AktørInntektBuilder oppdatere = AktørInntektBuilder.oppdatere(aktørInntekt);
        oppdatere.medAktørId(aktørId);
        return oppdatere;
    }

    /**
     * Oppretter builder for tilstøtende ytelser for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørYtelseBuilder}
     */
    public AktørYtelseBuilder getAktørYtelseBuilder(AktørId aktørId) {
        Optional<AktørYtelseDto> aktørYtelse = kladd.getAktørYtelse().stream().filter(ay -> aktørId.equals(ay.getAktørId())).findFirst();
        return AktørYtelseBuilder.oppdatere(aktørYtelse).medAktørId(aktørId);
    }

    public InntektArbeidYtelseAggregatDto build() {
        return this.kladd;
    }

    VersjonTypeDto getVersjon() {
        return versjon;
    }

    void oppdaterArbeidsforholdReferanseEtterErstatting(AktørId søker, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto gammelRef,
                                                        InternArbeidsforholdRefDto nyRef) {
        final AktørArbeidBuilder builder = getAktørArbeidBuilder(søker);
        if (builder.getErOppdatering()) {
            if (eksistererIkkeFraFør(arbeidsgiver, gammelRef, builder)) {
                final YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = builder.getYrkesaktivitetBuilderForNøkkelAvType(
                    OpptjeningsnøkkelDto.forArbeidsforholdIdMedArbeidgiver(gammelRef, arbeidsgiver),
                    ArbeidType.AA_REGISTER_TYPER);
                if (yrkesaktivitetBuilder.getErOppdatering()) {
                    yrkesaktivitetBuilder.medArbeidsforholdId(nyRef);
                    builder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
                    leggTilAktørArbeid(builder);
                }
            }
        }
    }

    private boolean eksistererIkkeFraFør(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto gammelRef, AktørArbeidBuilder builder) {
        return !builder.getYrkesaktivitetBuilderForNøkkelAvType(OpptjeningsnøkkelDto.forArbeidsforholdIdMedArbeidgiver(gammelRef, arbeidsgiver),
            ArbeidType.AA_REGISTER_TYPER).getErOppdatering();
    }

    public void medNyInternArbeidsforholdRef(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto nyRef, EksternArbeidsforholdRef eksternReferanse) {
        nyeInternArbeidsforholdReferanser.add(new ArbeidsforholdReferanseDto(arbeidsgiver, nyRef, eksternReferanse));
    }

    public InternArbeidsforholdRefDto medNyInternArbeidsforholdRef(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef eksternReferanse) {
        if (eksternReferanse == null || eksternReferanse.getReferanse() == null) {
            return InternArbeidsforholdRefDto.nullRef();
        }
        InternArbeidsforholdRefDto nyRef = InternArbeidsforholdRefDto.nyRef();
        nyeInternArbeidsforholdReferanser.add(new ArbeidsforholdReferanseDto(arbeidsgiver, nyRef, eksternReferanse));
        return nyRef;
    }

    public List<ArbeidsforholdReferanseDto> getNyeInternArbeidsforholdReferanser() {
        return nyeInternArbeidsforholdReferanser;
    }

    public static class AktørArbeidBuilder {
        private final AktørArbeidDto kladd;
        private final boolean oppdatering;

        private AktørArbeidBuilder(AktørArbeidDto aktørArbeid, boolean oppdatering) {
            this.kladd = aktørArbeid;
            this.oppdatering = oppdatering;
        }

        static AktørArbeidBuilder ny() {
            return new AktørArbeidBuilder(new AktørArbeidDto(), false);
        }

        static AktørArbeidBuilder oppdatere(AktørArbeidDto oppdatere) {
            return new AktørArbeidBuilder(oppdatere, true);
        }

        public static AktørArbeidBuilder oppdatere(Optional<AktørArbeidDto> oppdatere) {
            return oppdatere.map(AktørArbeidBuilder::oppdatere).orElseGet(AktørArbeidBuilder::ny);
        }

        public AktørArbeidBuilder medAktørId(AktørId aktørId) {
            this.kladd.setAktørId(aktørId);
            return this;
        }

        public YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForNøkkelAvType(OpptjeningsnøkkelDto nøkkel, ArbeidType arbeidType) {
            return kladd.getYrkesaktivitetBuilderForNøkkel(nøkkel, arbeidType);
        }

        public YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForNøkkelAvType(OpptjeningsnøkkelDto nøkkel, Set<ArbeidType> arbeidType) {
            return kladd.getYrkesaktivitetBuilderForNøkkel(nøkkel, arbeidType);
        }

        public YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForType(ArbeidType type) {
            return kladd.getYrkesaktivitetBuilderForType(type);
        }

        public AktørArbeidBuilder leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder builder) {
            YrkesaktivitetDto yrkesaktivitet = builder.build();
            if (!builder.getErOppdatering()) {
                kladd.leggTilYrkesaktivitet(yrkesaktivitet);
            }
            return this;
        }

        public AktørArbeidBuilder leggTilYrkesaktivitet(YrkesaktivitetDto yrkesaktivitet) {
            kladd.leggTilYrkesaktivitet(yrkesaktivitet);
            return this;
        }

        public AktørArbeidDto build() {
            if (kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException();
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }

    public static class AktørInntektBuilder {
        private final AktørInntektDto kladd;
        private final boolean oppdatering;

        private AktørInntektBuilder(AktørInntektDto aktørInntekt, boolean oppdatering) {
            this.kladd = aktørInntekt;
            this.oppdatering = oppdatering;
        }

        static AktørInntektBuilder ny() {
            return new AktørInntektBuilder(new AktørInntektDto(), false);
        }

        static AktørInntektBuilder oppdatere(AktørInntektDto oppdatere) {
            return new AktørInntektBuilder(oppdatere, true);
        }

        public static AktørInntektBuilder oppdatere(Optional<AktørInntektDto> oppdatere) {
            return oppdatere.map(AktørInntektBuilder::oppdatere).orElseGet(AktørInntektBuilder::ny);
        }

        public void medAktørId(AktørId aktørId) {
            this.kladd.setAktørId(aktørId);
        }

        public InntektDtoBuilder getInntektBuilder(InntektsKilde inntektsKilde, OpptjeningsnøkkelDto opptjeningsnøkkel) {
            return kladd.getInntektBuilder(inntektsKilde, opptjeningsnøkkel);
        }

        public InntektDtoBuilder getInntektBuilderForYtelser(InntektsKilde inntektsKilde) {
            return kladd.getInntektBuilderForYtelser(inntektsKilde);
        }

        public AktørInntektBuilder leggTilInntekt(InntektDtoBuilder builder) {
            if (!builder.getErOppdatering()) {
                kladd.leggTilInntekt(builder.build());
            }
            return this;
        }

        public AktørInntektDto build() {
            if (kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException();
        }

        boolean getErOppdatering() {
            return oppdatering;
        }

    }

    public static class AktørYtelseBuilder {
        private final AktørYtelseDto kladd;
        private final boolean oppdatering;

        private AktørYtelseBuilder(AktørYtelseDto aktørYtelse, boolean oppdatering) {
            this.kladd = aktørYtelse;
            this.oppdatering = oppdatering;
        }

        static AktørYtelseBuilder ny() {
            return new AktørYtelseBuilder(new AktørYtelseDto(), false);
        }

        static AktørYtelseBuilder oppdatere(AktørYtelseDto oppdatere) {
            return new AktørYtelseBuilder(oppdatere, true);
        }

        public static AktørYtelseBuilder oppdatere(Optional<AktørYtelseDto> oppdatere) {
            return oppdatere.map(AktørYtelseBuilder::oppdatere).orElseGet(AktørYtelseBuilder::ny);
        }

        boolean getErOppdatering() {
            return oppdatering;
        }

        public AktørYtelseBuilder medAktørId(AktørId aktørId) {
            this.kladd.setAktørId(aktørId);
            return this;
        }

        public YtelseDtoBuilder getYtelselseBuilderForType(Fagsystem fagsystem, RelatertYtelseType type) {
            return kladd.getYtelseBuilderForType(fagsystem, type);
        }

        public AktørYtelseBuilder leggTilYtelse(YtelseDtoBuilder builder) {
            YtelseDto ytelse = builder.build();
            if (!builder.getErOppdatering()) {
                this.kladd.leggTilYtelse(ytelse);
            }
            return this;
        }

        boolean harVerdi() {
            return kladd.hasValues();
        }

        public AktørYtelseDto build() {
            if (this.kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException("Har ikke innhold");
        }
    }

}
