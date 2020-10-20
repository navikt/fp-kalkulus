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

    @Column(name = "ER_NYOPPSTARTET_FL")
    private Boolean erNyoppstartetFL;

    @Column(name = "HAR_FL_MOTTATT_YTELSE")
    private Boolean harFLMottattYtelse;

    @Column(name = "SKAL_BESTEBEREGNES")
    private Boolean skalBesteberegnes;

    @Column(name = "MOTTAR_ETTERLONN_SLUTTPAKKE")
    private Boolean mottarEtterlønnSluttpakke;

    public FaktaAktørEntitet() {
        // hibernate
    }

    public FaktaAktørEntitet(FaktaAktørEntitet original) {
        this.erNyIArbeidslivetSN = original.getErNyIArbeidslivetSN();
        this.erNyoppstartetFL = original.getErNyoppstartetFL();
        this.harFLMottattYtelse = original.getHarFLMottattYtelse();
        this.skalBesteberegnes = original.getSkalBesteberegnes();
        this.mottarEtterlønnSluttpakke = original.getMottarEtterlønnSluttpakke();
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

    public Boolean getSkalBesteberegnes() {
        return skalBesteberegnes;
    }

    public Boolean getMottarEtterlønnSluttpakke() {
        return mottarEtterlønnSluttpakke;
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

        public Builder medErNyIArbeidslivetSN(boolean erNyIArbeidslivetSN) {
            mal.erNyIArbeidslivetSN = erNyIArbeidslivetSN;
            return this;
        }

        public Builder medErNyoppstartetFL(boolean erNyoppstartetFL) {
            mal.erNyoppstartetFL = erNyoppstartetFL;
            return this;
        }

        public Builder medHarFLMottattYtelse(boolean harFLMottattYtelse) {
            mal.harFLMottattYtelse = harFLMottattYtelse;
            return this;
        }

        public Builder medSkalBesteberegnes(boolean skalBesteberegnes) {
            mal.skalBesteberegnes = skalBesteberegnes;
            return this;
        }

        public Builder medMottarEtterlønnSluttpakke(boolean mottarEtterlønnSluttpakke) {
            mal.mottarEtterlønnSluttpakke = mottarEtterlønnSluttpakke;
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
                    && mal.harFLMottattYtelse == null
                    && mal.skalBesteberegnes == null
                    && mal.mottarEtterlønnSluttpakke == null;
        }
    }
}
