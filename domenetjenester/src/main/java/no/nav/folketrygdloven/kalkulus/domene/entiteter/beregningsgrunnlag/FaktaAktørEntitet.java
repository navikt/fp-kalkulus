package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

@Entity(name = "FaktaAktørEntitet")
@Table(name = "FAKTA_AKTOER")
public class FaktaAktørEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAKTA_AKTOER")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "FAKTA_AGGREGAT_ID", nullable = false, updatable = false)
    private FaktaAggregatEntitet faktaAggregat;

    @Column(name = "ER_NY_I_ARBEIDSLIVET_SN")
    private Boolean erNyIArbeidslivetSN;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_ER_NY_I_ARBEIDSLIVET_SN", updatable = false)
    private FaktaVurderingEntitet erNyIArbeidslivetSNVurdering;

    @Column(name = "ER_NYOPPSTARTET_FL")
    private Boolean erNyoppstartetFL;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_ER_NYOPPSTARTET_FL", updatable = false)
    private FaktaVurderingEntitet erNyoppstartetFLVurdering;

    @Column(name = "HAR_FL_MOTTATT_YTELSE")
    private Boolean harFLMottattYtelse;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_HAR_FL_MOTTATT_YTELSE", updatable = false)
    private FaktaVurderingEntitet harFLMottattYtelseVurdering;

    @Column(name = "SKAL_BESTEBEREGNES")
    @Deprecated(forRemoval = true)
    private Boolean skalBesteberegnes;

    @Column(name = "MOTTAR_ETTERLONN_SLUTTPAKKE")
    private Boolean mottarEtterlønnSluttpakke;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_MOTTAR_ETTERLONN_SLUTTPAKKE", updatable = false)
    private FaktaVurderingEntitet mottarEtterlønnSluttpakkeVurdering;

    @Column(name = "SKAL_BEREGNES_SOM_MILITAER")
    private Boolean skalBeregnesSomMilitær;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_SKAL_BEREGNES_SOM_MILITAER", updatable = false)
    private FaktaVurderingEntitet skalBeregnesSomMilitærVurdering;


    public FaktaAktørEntitet() {
        // hibernate
    }

    public FaktaAktørEntitet(FaktaAktørEntitet original) {
        this.erNyIArbeidslivetSN = original.getErNyIArbeidslivetSN();
        this.erNyoppstartetFL = original.getErNyoppstartetFL();
        this.harFLMottattYtelse = original.getHarFLMottattYtelse();
        this.skalBesteberegnes = original.getSkalBesteberegnes();
        this.mottarEtterlønnSluttpakke = original.getMottarEtterlønnSluttpakke();
        this.skalBeregnesSomMilitær = original.getSkalBeregnesSomMilitær();
    }


    void setFaktaAggregat(FaktaAggregatEntitet faktaAggregat) {
        this.faktaAggregat = faktaAggregat;
    }

    public Boolean getErNyIArbeidslivetSN() {
        return erNyIArbeidslivetSN;
    }

    public Boolean getErNyoppstartetFL() {
        return erNyoppstartetFL;
    }

    public Boolean getHarFLMottattYtelse() {
        return harFLMottattYtelse;
    }

    @Deprecated
    public Boolean getSkalBesteberegnes() {
        return skalBesteberegnes;
    }

    public Boolean getMottarEtterlønnSluttpakke() {
        return mottarEtterlønnSluttpakke;
    }

    public Boolean getSkalBeregnesSomMilitær() {
        return skalBeregnesSomMilitær;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(FaktaAktørEntitet kopi) {
        return new Builder(kopi);
    }

    public static class Builder {
        private FaktaAktørEntitet mal;

        private Builder() {
            mal = new FaktaAktørEntitet();
        }

        public Builder(FaktaAktørEntitet kopi) {
            mal = new FaktaAktørEntitet(kopi);
        }

        public Builder medErNyIArbeidslivetSN(Boolean erNyIArbeidslivetSN) {
            mal.erNyIArbeidslivetSN = erNyIArbeidslivetSN;
            return this;
        }

        public Builder medErNyoppstartetFL(Boolean erNyoppstartetFL) {
            mal.erNyoppstartetFL = erNyoppstartetFL;
            return this;
        }

        public Builder medHarFLMottattYtelse(Boolean harFLMottattYtelse) {
            mal.harFLMottattYtelse = harFLMottattYtelse;
            return this;
        }

        public Builder medSkalBesteberegnes(Boolean skalBesteberegnes) {
            mal.skalBesteberegnes = skalBesteberegnes;
            return this;
        }

        public Builder medMottarEtterlønnSluttpakke(Boolean mottarEtterlønnSluttpakke) {
            mal.mottarEtterlønnSluttpakke = mottarEtterlønnSluttpakke;
            return this;
        }

        public Builder medSkalBeregnesSomMilitær(Boolean skalBeregnesSomMilitær) {
            mal.skalBeregnesSomMilitær = skalBeregnesSomMilitær;
            return this;
        }

        public FaktaAktørEntitet build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            if (erUgyldig()) {
                throw new IllegalStateException("Må ha satt minst et faktafelt.");
            }
        }

        public boolean erUgyldig() {
            return mal.erNyIArbeidslivetSN == null
                    && mal.erNyoppstartetFL == null
                    && mal.skalBeregnesSomMilitær == null
                    && mal.harFLMottattYtelse == null
                    && mal.skalBesteberegnes == null
                    && mal.mottarEtterlønnSluttpakke == null;
        }
    }
}
