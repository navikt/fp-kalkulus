package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.NæringsinntektType;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.OffentligYtelseType;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.PensjonTrygdType;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.YtelseType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.tid.DatoIntervallEntitet;

public class InntektspostDto {

    private static final Map<String, Map<String, ? extends YtelseType>> YTELSE_TYPER = new LinkedHashMap<>();

    static {
        YTELSE_TYPER.put(OffentligYtelseType.KODEVERK, OffentligYtelseType.kodeMap());
        YTELSE_TYPER.put(NæringsinntektType.KODEVERK, NæringsinntektType.kodeMap());
        YTELSE_TYPER.put(PensjonTrygdType.KODEVERK, PensjonTrygdType.kodeMap());

    }
    private InntektspostType inntektspostType;
    private SkatteOgAvgiftsregelType skatteOgAvgiftsregelType = SkatteOgAvgiftsregelType.UDEFINERT;
    private InntektDto inntekt;
    private String ytelseType = OffentligYtelseType.KODEVERK;
    private String ytelse = OffentligYtelseType.UDEFINERT.getKode();
    private DatoIntervallEntitet periode;
    private Beløp beløp;

    public InntektspostDto() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    InntektspostDto(InntektspostDto inntektspost) {
        this.inntektspostType = inntektspost.getInntektspostType();
        this.skatteOgAvgiftsregelType = inntektspost.getSkatteOgAvgiftsregelType();
        this.ytelse = inntektspost.getYtelseType().getKode();
        this.periode = inntektspost.getPeriode();
        this.beløp = inntektspost.getBeløp();
        this.ytelseType = inntektspost.getYtelseType().getKodeverk();
    }

    public String getIndexKey() {
        return IndexKey.createKey(getInntektspostType(), getYtelseType().getKodeverk(), getYtelseType().getKode(), getSkatteOgAvgiftsregelType(), periode);
    }

    /**
     * Underkategori av utbetaling
     * <p>
     * F.eks
     * <ul>
     * <li>Lønn</li>
     * <li>Ytelse</li>
     * <li>Næringsinntekt</li>
     * </ul>
     *
     * @return {@link InntektspostType}
     */
    public InntektspostType getInntektspostType() {
        return inntektspostType;
    }

    void setInntektspostType(InntektspostType inntektspostType) {
        this.inntektspostType = inntektspostType;
    }

    /**
     * En kodeverksverdi som angir særskilt beskatningsregel.
     * Den er ikke alltid satt, og kommer fra inntektskomponenten
     *
     * @return {@link SkatteOgAvgiftsregelType}
     */
    public SkatteOgAvgiftsregelType getSkatteOgAvgiftsregelType() {
        return skatteOgAvgiftsregelType;
    }

    void setSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        this.skatteOgAvgiftsregelType = skatteOgAvgiftsregelType;
    }

    void setPeriode(LocalDate fom, LocalDate tom) {
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    /**
     * Beløpet som har blitt utbetalt i perioden
     *
     * @return Beløpet
     */
    public Beløp getBeløp() {
        return beløp;
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    public YtelseType getYtelseType() {
        var yt = YTELSE_TYPER.getOrDefault(ytelseType, Collections.emptyMap()).get(ytelse);
        return yt != null ? yt : OffentligYtelseType.UDEFINERT;
    }

    public InntektDto getInntekt() {
        return inntekt;
    }

    void setInntekt(InntektDto inntekt) {
        this.inntekt = inntekt;
    }

    void setYtelse(YtelseType ytelse) {
        this.ytelseType = ytelse.getKodeverk();
        this.ytelse = ytelse.getKode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof InntektspostDto)) {
            return false;
        }
        InntektspostDto other = (InntektspostDto) obj;
        return Objects.equals(this.getInntektspostType(), other.getInntektspostType())
            && Objects.equals(this.getYtelseType(), other.getYtelseType())
            && Objects.equals(this.getSkatteOgAvgiftsregelType(), other.getSkatteOgAvgiftsregelType())
            && Objects.equals(this.getPeriode().getFomDato(), other.getPeriode().getFomDato())
            && Objects.equals(this.getPeriode().getTomDato(), other.getPeriode().getTomDato());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInntektspostType(), getYtelseType(), getSkatteOgAvgiftsregelType(), getPeriode().getFomDato(), getPeriode().getTomDato());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "ytelseType=" + ytelseType +
            "inntektspostType=" + inntektspostType +
            "skatteOgAvgiftsregelType=" + skatteOgAvgiftsregelType +
            ", fraOgMed=" + periode.getFomDato() +
            ", tilOgMed=" + periode.getTomDato() +
            ", beløp=" + beløp +
            '>';
    }

    public boolean hasValues() {
        return inntektspostType != null || periode.getFomDato() != null || periode.getTomDato() != null || beløp != null;
    }

}
