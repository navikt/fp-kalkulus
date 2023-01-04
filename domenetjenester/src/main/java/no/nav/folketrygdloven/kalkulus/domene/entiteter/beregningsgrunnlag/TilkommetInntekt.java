package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.AktivitetStatusKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@Entity(name = "TilkommetInntekt")
@Table(name = "TILKOMMET_INNTEKT")
public class TilkommetInntekt extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TILKOMMET_INNTEKT")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "bg_periode_id", nullable = false, updatable = false)
    private BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;

    @Convert(converter= AktivitetStatusKodeverdiConverter.class)
    @Column(name="aktivitet_status", nullable = false)
    private AktivitetStatus aktivitetStatus;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "brutto_inntekt_pr_aar")))
    private Beløp bruttoInntektPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "tilkommet_inntekt_pr_aar")))
    private Beløp tilkommetInntektPrÅr;

    @Column(name = "skal_redusere_utbetaling", nullable = false)
    private Boolean skalRedusereUtbetaling;

    protected TilkommetInntekt() {
    }

    public TilkommetInntekt(TilkommetInntekt tilkommetInntekt) {
        this.aktivitetStatus = tilkommetInntekt.aktivitetStatus;
        this.arbeidsgiver = tilkommetInntekt.arbeidsgiver;
        this.arbeidsforholdRef = tilkommetInntekt.arbeidsforholdRef;
        this.bruttoInntektPrÅr = tilkommetInntekt.bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntekt.tilkommetInntektPrÅr;
        this.skalRedusereUtbetaling = tilkommetInntekt.skalRedusereUtbetaling;
    }

    public TilkommetInntekt(AktivitetStatus aktivitetStatus,
                            Arbeidsgiver arbeidsgiver,
                            InternArbeidsforholdRef arbeidsforholdRef,
                            Beløp bruttoInntektPrÅr,
                            Beløp tilkommetInntektPrÅr,
                            boolean skalRedusereUtbetaling) {
        if (!skalRedusereUtbetaling && tilkommetInntektPrÅr != null) {
            throw new IllegalStateException("Skal ikke sette tilkommet inntekt når ikke redusert utbetaling");
        }
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntektPrÅr;
        this.skalRedusereUtbetaling = skalRedusereUtbetaling;
    }

    void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public Beløp getBruttoInntektPrÅr() {
        return bruttoInntektPrÅr;
    }

    public Beløp getTilkommetInntektPrÅr() {
        return tilkommetInntektPrÅr;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRef.nullRef() : arbeidsforholdRef;
    }

    public boolean skalRedusereUtbetaling() {
        return skalRedusereUtbetaling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TilkommetInntekt that = (TilkommetInntekt) o;
        return aktivitetStatus == that.aktivitetStatus &&
                Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef) &&
                Objects.equals(bruttoInntektPrÅr, that.bruttoInntektPrÅr) &&
                Objects.equals(tilkommetInntektPrÅr, that.tilkommetInntektPrÅr) &&
                skalRedusereUtbetaling == that.skalRedusereUtbetaling;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver, arbeidsforholdRef, bruttoInntektPrÅr, tilkommetInntektPrÅr, skalRedusereUtbetaling);
    }
}
