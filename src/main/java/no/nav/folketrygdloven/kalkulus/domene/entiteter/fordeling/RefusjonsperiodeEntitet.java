package no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

@Entity(name = "RefusjonsperiodeEntitet")
@Table(name = "REFUSJON_ANDEL_PERIODE")
public class RefusjonsperiodeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "refusjon_andel_id", nullable = false, updatable = false)
    private RefusjonsandelEntitet refusjonsandel;

    @Embedded
    private IntervallEntitet periode;

    @Embedded
    @AttributeOverride(name = "verdi", column = @Column(name = "refusjon_pr_mnd", nullable = false, precision = 19, scale = 2))
    private Beløp refusjonPrMnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "utfall", nullable = false, length = 50)
    private RefusjonUtfall utfall;

    @Enumerated(EnumType.STRING)
    @Column(name = "utfall_aarsak", length = 50)
    private RefusjonUtfallÅrsak utfallÅrsak;

    protected RefusjonsperiodeEntitet() {
        // Hibernate
    }

    public RefusjonsperiodeEntitet(IntervallEntitet periode, Beløp refusjonPrMnd, RefusjonUtfall utfall,
                                  RefusjonUtfallÅrsak utfallÅrsak) {
        this.periode = periode;
        this.refusjonPrMnd = refusjonPrMnd;
        this.utfall = utfall;
        this.utfallÅrsak = utfallÅrsak;
    }

    void setRefusjonsandel(RefusjonsandelEntitet refusjonsandel) {
        this.refusjonsandel = refusjonsandel;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public Beløp getRefusjonPrMnd() {
        return refusjonPrMnd;
    }

    public RefusjonUtfall getUtfall() {
        return utfall;
    }

    public RefusjonUtfallÅrsak getUtfallÅrsak() {
        return utfallÅrsak;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RefusjonsperiodeEntitet kladd = new RefusjonsperiodeEntitet();

        private Builder() {
        }

        public Builder medPeriode(IntervallEntitet periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medRefusjonPrMnd(Beløp refusjonPrMnd) {
            kladd.refusjonPrMnd = refusjonPrMnd;
            return this;
        }

        public Builder medUtfall(RefusjonUtfall utfall) {
            kladd.utfall = utfall;
            return this;
        }

        public Builder medUtfallÅrsak(RefusjonUtfallÅrsak utfallÅrsak) {
            kladd.utfallÅrsak = utfallÅrsak;
            return this;
        }

        public RefusjonsperiodeEntitet build() {
            return kladd;
        }
    }
}
