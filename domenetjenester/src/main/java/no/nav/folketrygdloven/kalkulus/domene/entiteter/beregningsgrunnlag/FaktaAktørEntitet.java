package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
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

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.FaktaVurdering;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "er_ny_i_arbeidslivet_sn")),
            @AttributeOverride(name = "kilde", column = @Column(name = "er_ny_i_arbeidslivet_sn_kilde"))
    })
    private FaktaVurdering erNyIArbeidslivetSN;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "er_nyoppstartet_fl")),
            @AttributeOverride(name = "kilde", column = @Column(name = "er_nyoppstartet_fl_kilde"))
    })
    private FaktaVurdering erNyoppstartetFL;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "har_fl_mottatt_ytelse")),
            @AttributeOverride(name = "kilde", column = @Column(name = "har_fl_mottatt_ytelse_kilde"))
    })
    private FaktaVurdering harFLMottattYtelse;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "skal_besteberegnes")),
            @AttributeOverride(name = "kilde", column = @Column(name = "skal_besteberegnes_kilde"))
    })
    private FaktaVurdering skalBesteberegnes;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "mottar_etterlonn_sluttpakke")),
            @AttributeOverride(name = "kilde", column = @Column(name = "mottar_etterlonn_sluttpakke_kilde"))
    })
    private FaktaVurdering mottarEtterlønnSluttpakke;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "skal_beregnes_som_militaer")),
            @AttributeOverride(name = "kilde", column = @Column(name = "skal_beregnes_som_militaer_kilde"))
    })
    private FaktaVurdering skalBeregnesSomMilitær;

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

    public Boolean getErNyIArbeidslivetSNVurdering() {
        return finnVurdering(erNyIArbeidslivetSN);
    }

    public Boolean getErNyoppstartetFLVurdering() {
        return finnVurdering(erNyoppstartetFL);
    }

    public Boolean getHarFLMottattYtelseVurdering() {
        return finnVurdering(harFLMottattYtelse);
    }

    public Boolean getSkalBesteberegnesVurdering() {
        return finnVurdering(skalBesteberegnes);
    }

    public Boolean getMottarEtterlønnSluttpakkeVurdering() {
        return finnVurdering(mottarEtterlønnSluttpakke);
    }

    public Boolean getSkalBeregnesSomMilitærVurdering() {
        return finnVurdering(skalBeregnesSomMilitær);
    }

    private Boolean finnVurdering(FaktaVurdering faktaVurdering) {
        if (faktaVurdering == null) {
            return null;
        }
        return faktaVurdering.getVurdering();
    }

    public FaktaVurdering getErNyIArbeidslivetSN() {
        return erNyIArbeidslivetSN;
    }

    public FaktaVurdering getErNyoppstartetFL() {
        return erNyoppstartetFL;
    }

    public FaktaVurdering getHarFLMottattYtelse() {
        return harFLMottattYtelse;
    }

    public FaktaVurdering getSkalBesteberegnes() {
        return skalBesteberegnes;
    }

    public FaktaVurdering getMottarEtterlønnSluttpakke() {
        return mottarEtterlønnSluttpakke;
    }

    public FaktaVurdering getSkalBeregnesSomMilitær() {
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
            mal.erNyIArbeidslivetSN = new FaktaVurdering(erNyIArbeidslivetSN, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medErNyoppstartetFL(Boolean erNyoppstartetFL) {
            mal.erNyoppstartetFL = new FaktaVurdering(erNyoppstartetFL, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medHarFLMottattYtelse(Boolean harFLMottattYtelse) {
            mal.harFLMottattYtelse = new FaktaVurdering(harFLMottattYtelse, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medSkalBesteberegnes(Boolean skalBesteberegnes) {
            mal.skalBesteberegnes = new FaktaVurdering(skalBesteberegnes, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medMottarEtterlønnSluttpakke(Boolean mottarEtterlønnSluttpakke) {
            mal.mottarEtterlønnSluttpakke = new FaktaVurdering(mottarEtterlønnSluttpakke, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medSkalBeregnesSomMilitær(Boolean skalBeregnesSomMilitær) {
            mal.skalBeregnesSomMilitær = new FaktaVurdering(skalBeregnesSomMilitær, FaktaVurderingKilde.SAKSBEHANDLER);
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
