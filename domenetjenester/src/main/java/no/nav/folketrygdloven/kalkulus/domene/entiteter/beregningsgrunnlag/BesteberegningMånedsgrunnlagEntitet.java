package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;


@Entity(name = "BesteberegningMånedsgrunnlagEntitet")
@Table(name = "BESTEBEREGNING_MAANED")
public class BesteberegningMånedsgrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne
    @JoinColumn(name = "besteberegninggrunnlag_id", updatable = false, unique = true)
    private BesteberegninggrunnlagEntitet besteberegninggrunnlag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "besteberegningMåned", cascade = CascadeType.PERSIST)
    private List<BesteberegningInntektEntitet> inntekter = new ArrayList<>();

    @Embedded
    private IntervallEntitet periode;

    public BesteberegningMånedsgrunnlagEntitet() {
    }

    public BesteberegningMånedsgrunnlagEntitet(BesteberegningMånedsgrunnlagEntitet besteberegningMånedsgrunnlagEntitet) {
        this.periode = besteberegningMånedsgrunnlagEntitet.getPeriode();
        besteberegningMånedsgrunnlagEntitet.getInntekter().stream().map(BesteberegningInntektEntitet::new).forEach(this::leggTilInntekt);
    }

    public Long getId() {
        return id;
    }

    public long getVersjon() {
        return versjon;
    }

    public BesteberegninggrunnlagEntitet getBesteberegninggrunnlag() {
        return besteberegninggrunnlag;
    }

    public List<BesteberegningInntektEntitet> getInntekter() {
        return inntekter;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    void setBesteberegninggrunnlag(BesteberegninggrunnlagEntitet besteberegninggrunnlag) {
        this.besteberegninggrunnlag = besteberegninggrunnlag;
    }

    private void leggTilInntekt(BesteberegningInntektEntitet besteberegningInntektEntitet) {
        besteberegningInntektEntitet.setBesteberegningMåned(this);
        this.inntekter.add(besteberegningInntektEntitet);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BesteberegningMånedsgrunnlagEntitet kladd;

        public Builder() {
            kladd = new BesteberegningMånedsgrunnlagEntitet();
        }

        public Builder(BesteberegningMånedsgrunnlagEntitet eksisterendeMånedsgrunnlag, boolean erOppdatering) {
            if (Objects.nonNull(eksisterendeMånedsgrunnlag.getId())) {
                throw new IllegalArgumentException("Kan ikke bygge på et lagret grunnlag");
            }
            if (erOppdatering) {
                kladd = eksisterendeMånedsgrunnlag;
            } else {
                kladd = new BesteberegningMånedsgrunnlagEntitet(eksisterendeMånedsgrunnlag);
            }
        }

        public Builder leggTilInntekt(BesteberegningInntektEntitet besteberegningInntektEntitet) {
            kladd.leggTilInntekt(besteberegningInntektEntitet);
            return this;
        }

        public Builder medPeriode(LocalDate fom, LocalDate tom) {
            kladd.periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
            return this;
        }

        public BesteberegningMånedsgrunnlagEntitet build() {
            Objects.requireNonNull(kladd.periode);
            return kladd;
        }

    }


}
