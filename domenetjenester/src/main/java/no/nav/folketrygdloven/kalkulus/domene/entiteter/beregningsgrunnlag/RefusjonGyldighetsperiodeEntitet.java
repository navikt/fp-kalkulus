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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;

@Entity(name = "RefusjonGyldighetsperiode")
@Table(name = "REFUSJON_GYLDIGHETSPERIODE")
public class RefusjonGyldighetsperiodeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_REFUSJON_GYLDIGHETSPERIODE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "fom")),
            @AttributeOverride(name = "tomDato", column = @Column(name = "tom"))
    })
    private IntervallEntitet periode;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "bg_refusjon_overstyring_id", nullable = false, updatable = false)
    private BeregningRefusjonOverstyringEntitet refusjonOverstyring;

    protected RefusjonGyldighetsperiodeEntitet() {
        // Hibernate
    }

    public RefusjonGyldighetsperiodeEntitet(RefusjonGyldighetsperiodeEntitet beregningRefusjonOverstyringEntitet) {
        this.periode = beregningRefusjonOverstyringEntitet.getPeriode();
    }

    public RefusjonGyldighetsperiodeEntitet(IntervallEntitet periode) {
        this.periode = periode;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    void setRefusjonOverstyring(BeregningRefusjonOverstyringEntitet refusjonOverstyring) {
        this.refusjonOverstyring = refusjonOverstyring;
    }
}
