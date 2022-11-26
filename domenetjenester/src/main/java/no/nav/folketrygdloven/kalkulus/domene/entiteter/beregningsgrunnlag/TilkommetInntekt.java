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
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "brutto_inntekt_pr_aar")))
    private Beløp bruttoInntektPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "tilkommet_inntekt_pr_aar")))
    private Beløp tilkommetInntektPrÅr;

    protected TilkommetInntekt() {
    }

    public TilkommetInntekt(TilkommetInntekt tilkommetInntekt) {
        this.aktivitetStatus = tilkommetInntekt.aktivitetStatus;
        this.arbeidsgiver = tilkommetInntekt.arbeidsgiver;
        this.bruttoInntektPrÅr = tilkommetInntekt.bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntekt.tilkommetInntektPrÅr;
    }

    public TilkommetInntekt(AktivitetStatus aktivitetStatus,
                              Arbeidsgiver arbeidsgiver,
                              Beløp bruttoInntektPrÅr, Beløp tilkommetInntektPrÅr) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntektPrÅr;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TilkommetInntekt that = (TilkommetInntekt) o;
        return aktivitetStatus == that.aktivitetStatus &&
                Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                bruttoInntektPrÅr.equals(that.bruttoInntektPrÅr) &&
                tilkommetInntektPrÅr.equals(that.tilkommetInntektPrÅr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver, bruttoInntektPrÅr, tilkommetInntektPrÅr);
    }
}
