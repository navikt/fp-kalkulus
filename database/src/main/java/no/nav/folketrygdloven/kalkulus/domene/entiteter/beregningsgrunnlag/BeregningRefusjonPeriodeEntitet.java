package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;

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

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

@Entity(name = "BeregningRefusjonPeriode")
@Table(name = "BG_REFUSJON_PERIODE")
public class BeregningRefusjonPeriodeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_REFUSJON_PERIODE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Column(name = "fom", nullable = false)
    private LocalDate startdatoRefusjon;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "bg_refusjon_overstyring_id", nullable = false, updatable = false)
    private BeregningRefusjonOverstyringEntitet refusjonOverstyring;

    BeregningRefusjonPeriodeEntitet() {
        // Hibernate
    }

    public BeregningRefusjonPeriodeEntitet(InternArbeidsforholdRef ref, LocalDate startdatoRefusjon) {
        Objects.requireNonNull(startdatoRefusjon, "startdatoRefusjon");
        this.arbeidsforholdRef = ref;
        this.startdatoRefusjon = startdatoRefusjon;

    }

    void setRefusjonOverstyringEntitet(BeregningRefusjonOverstyringEntitet refusjonOverstyring) {
        this.refusjonOverstyring = refusjonOverstyring;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    public LocalDate getStartdatoRefusjon() {
        return startdatoRefusjon;
    }
}
