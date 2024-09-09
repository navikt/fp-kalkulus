package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;


@Entity(name = "BesteberegningEntitet")
@Table(name = "BG_BESTEBEREGNING_GRUNNLAG")
public class BesteberegninggrunnlagEntitet extends BaseEntitet {

    public static final int ANTALL_BESTEBEREGNING_MÅNEDER = 6;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_BESTEBEREGNING_GRUNNLAG")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @OneToOne(optional = false)
    @JoinColumn(name = "beregningsgrunnlag_id", nullable = false, updatable = false, unique = true)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "besteberegninggrunnlag", cascade = CascadeType.PERSIST)
    private Set<BesteberegningMånedsgrunnlagEntitet> seksBesteMåneder = new HashSet<>();

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "avvik_belop")))
    private Beløp avvik;

    public BesteberegninggrunnlagEntitet(BesteberegninggrunnlagEntitet besteberegninggrunnlagEntitet) {
        besteberegninggrunnlagEntitet.getSeksBesteMåneder().stream()
            .map(BesteberegningMånedsgrunnlagEntitet::new)
            .forEach(this::leggTilMånedsgrunnlag);
        this.avvik = besteberegninggrunnlagEntitet.getAvvik().orElse(null);
    }

    public BesteberegninggrunnlagEntitet() {
    }

    public Long getId() {
        return id;
    }

    public long getVersjon() {
        return versjon;
    }

    public Set<BesteberegningMånedsgrunnlagEntitet> getSeksBesteMåneder() {
        return seksBesteMåneder;
    }

    public BeregningsgrunnlagEntitet getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    // Det regnes kun ut et avvik for å kontrollere hvis tredje ledd har gitt beste beregning
    public Optional<Beløp> getAvvik() {
        return Optional.ofNullable(avvik);
    }

    private void leggTilMånedsgrunnlag(BesteberegningMånedsgrunnlagEntitet månedsgrunnlagEntitet) {
        if (seksBesteMåneder.size() >= ANTALL_BESTEBEREGNING_MÅNEDER) {
            throw new IllegalStateException("Kan ikke legge til mer en + " +
                ANTALL_BESTEBEREGNING_MÅNEDER + " måneder for bestebergning");
        }
        if (seksBesteMåneder.stream().anyMatch(m -> m.getPeriode().overlapper(månedsgrunnlagEntitet.getPeriode()))) {
            throw new IllegalStateException("Det finnes allerede et månedsgrunnlag for " + månedsgrunnlagEntitet.getPeriode());
        }
        månedsgrunnlagEntitet.setBesteberegninggrunnlag(this);
        this.seksBesteMåneder.add(månedsgrunnlagEntitet);
    }

    // Lager en dyp kopi, uten ID og beregningsgrunnlag
    public static Builder kopier(BesteberegninggrunnlagEntitet besteberegninggrunnlagEntitet) {
        return new Builder(besteberegninggrunnlagEntitet);
    }

    public static Builder ny() {
        return new Builder();
    }

    public static class Builder {
        private final BesteberegninggrunnlagEntitet kladd;

        public Builder() {
            kladd = new BesteberegninggrunnlagEntitet();
        }

        public Builder(BesteberegninggrunnlagEntitet besteberegninggrunnlagEntitet) {
            Objects.requireNonNull(besteberegninggrunnlagEntitet, "Kan ikke kopiere null");
            kladd = new BesteberegninggrunnlagEntitet(besteberegninggrunnlagEntitet);
        }

        public Builder leggTilMånedsgrunnlag(BesteberegningMånedsgrunnlagEntitet månedsgrunnlagEntitet) {
            kladd.leggTilMånedsgrunnlag(månedsgrunnlagEntitet);
            return this;
        }

        public Builder medAvvik(Beløp avvik) {
            kladd.avvik = avvik;
            return this;
        }

        public BesteberegninggrunnlagEntitet build() {
            return kladd;
        }
    }
}
