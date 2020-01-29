package no.nav.folketrygdloven.kalkulator.modell.iay;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektsKilde;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektspostType;

public class AktørInntektDto {

    private AktørId aktørId;

    private Set<InntektDto> inntekt = new LinkedHashSet<>();

    AktørInntektDto() {
        //hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørInntektDto(AktørInntektDto aktørInntekt) {
        this.aktørId = aktørInntekt.getAktørId();

        this.inntekt = aktørInntekt.inntekt.stream().map(i -> {
            var inntekt = new InntektDto(i);
            inntekt.setAktørInntekt(this);
            return inntekt;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public String getIndexKey() {
        return IndexKey.createKey(getAktørId());
    }

    /**
     * Aktøren inntekten er relevant for
     * @return aktørid
     */
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    /** Get alle inntekter samlet (ufiltrert). */
    public Collection<InntektDto> getInntekt() {
        return List.copyOf(inntekt);
    }

    public boolean hasValues() {
        return aktørId != null || inntekt != null;
    }

    InntektDtoBuilder getInntektBuilder(InntektsKilde inntektsKilde, OpptjeningsnøkkelDto nøkkel) {
        Optional<InntektDto> inntektOptional = getInntekt()
            .stream()
            .filter(i -> inntektsKilde.equals(i.getInntektsKilde()))
            .filter(i -> i.getArbeidsgiver() != null && new OpptjeningsnøkkelDto(i.getArbeidsgiver()).matcher(nøkkel)
                || inntektsKilde.equals(InntektsKilde.SIGRUN)).findFirst();
        InntektDtoBuilder oppdatere = InntektDtoBuilder.oppdatere(inntektOptional);
        if (!oppdatere.getErOppdatering()) {
            oppdatere.medInntektsKilde(inntektsKilde);
        }
        return oppdatere;
    }

    InntektDtoBuilder getInntektBuilderForYtelser(InntektsKilde inntektsKilde) {
        Optional<InntektDto> inntektOptional = getInntekt()
            .stream()
            .filter(i -> i.getArbeidsgiver() == null)
            .filter(i -> inntektsKilde.equals(i.getInntektsKilde()))
            .filter(i -> i.getAlleInntektsposter().stream()
                .anyMatch(post -> post.getInntektspostType().equals(InntektspostType.YTELSE)))
            .findFirst();
        InntektDtoBuilder oppdatere = InntektDtoBuilder.oppdatere(inntektOptional);
        if (!oppdatere.getErOppdatering()) {
            oppdatere.medInntektsKilde(inntektsKilde);
        }
        return oppdatere;
    }

    void leggTilInntekt(InntektDto inntekt) {
        this.inntekt.add(inntekt);
        inntekt.setAktørInntekt(this);
    }

    void fjernInntekterFraKilde(InntektsKilde inntektsKilde) {
        this.inntekt.removeIf(it -> it.getInntektsKilde().equals(inntektsKilde));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørInntektDto)) {
            return false;
        }
        AktørInntektDto other = (AktørInntektDto) obj;
        return Objects.equals(this.getAktørId(), other.getAktørId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "aktørId=" + aktørId +
            ", inntekt=" + inntekt +
            '>';
    }

}
