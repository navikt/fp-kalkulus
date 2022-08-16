package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bg_refusjon_overstyring_id", nullable = false, updatable = false)
    private BeregningRefusjonOverstyringEntitet refusjonOverstyring;

    BeregningRefusjonPeriodeEntitet() {
        // Hibernate
    }

    public BeregningRefusjonPeriodeEntitet(BeregningRefusjonPeriodeEntitet beregningRefusjonPeriodeEntitet) {
        this.arbeidsforholdRef = beregningRefusjonPeriodeEntitet.getArbeidsforholdRef();
        this.startdatoRefusjon = beregningRefusjonPeriodeEntitet.getStartdatoRefusjon();
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
