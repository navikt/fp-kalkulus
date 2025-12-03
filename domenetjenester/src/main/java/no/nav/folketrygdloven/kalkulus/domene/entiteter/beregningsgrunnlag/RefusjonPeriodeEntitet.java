package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;

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

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;

@Entity(name = "RefusjonPeriodeEntitet")
@Table(name = "REFUSJON_PERIODE")
public class RefusjonPeriodeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Column(name = "fom", nullable = false)
    private LocalDate startdatoRefusjon;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "refusjon_overstyring_id", nullable = false, updatable = false)
    private RefusjonOverstyringEntitet refusjonOverstyring;

    protected RefusjonPeriodeEntitet() {
        // Hibernate
    }

    public RefusjonPeriodeEntitet(RefusjonPeriodeEntitet refusjonPeriodeEntitet) {
        this.arbeidsforholdRef = refusjonPeriodeEntitet.getArbeidsforholdRef();
        this.startdatoRefusjon = refusjonPeriodeEntitet.getStartdatoRefusjon();
    }

    public RefusjonPeriodeEntitet(InternArbeidsforholdRef ref, LocalDate startdatoRefusjon) {
        Objects.requireNonNull(startdatoRefusjon, "startdatoRefusjon");
        this.arbeidsforholdRef = ref;
        this.startdatoRefusjon = startdatoRefusjon;

    }

    void setRefusjonOverstyringEntitet(RefusjonOverstyringEntitet refusjonOverstyring) {
        this.refusjonOverstyring = refusjonOverstyring;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    public LocalDate getStartdatoRefusjon() {
        return startdatoRefusjon;
    }
}
