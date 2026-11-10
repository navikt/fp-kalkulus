package no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;

@Entity(name = "RefusjonsandelEntitet")
@Table(name = "REFUSJON_ANDEL")
public class RefusjonsandelEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fordeling_grunnlag_id", nullable = false, updatable = false)
    private FordelingGrunnlagEntitet fordelingGrunnlag;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "kilde", nullable = false, length = 50)
    private RefusjonKilde kilde;

    @Column(name = "journalpost_id", length = 50)
    private String journalpostId;

    @OneToMany(mappedBy = "refusjonsandel", cascade = CascadeType.PERSIST)
    private List<RefusjonsperiodeEntitet> refusjonsperioder = new ArrayList<>();

    protected RefusjonsandelEntitet() {
        // Hibernate
    }

    public RefusjonsandelEntitet(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef arbeidsforholdRef, RefusjonKilde kilde,
                                String journalpostId) {
        this.arbeidsgiver = Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        this.arbeidsforholdRef = arbeidsforholdRef == null ? InternArbeidsforholdRef.nullRef() : arbeidsforholdRef;
        this.kilde = Objects.requireNonNull(kilde, "kilde");
        this.journalpostId = journalpostId;
    }

    void setFordelingGrunnlag(FordelingGrunnlagEntitet fordelingGrunnlag) {
        this.fordelingGrunnlag = fordelingGrunnlag;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRef.nullRef() : arbeidsforholdRef;
    }

    public RefusjonKilde getKilde() {
        return kilde;
    }

    public Optional<String> getJournalpostId() {
        return Optional.ofNullable(journalpostId);
    }

    public List<RefusjonsperiodeEntitet> getRefusjonsperioder() {
        return List.copyOf(refusjonsperioder);
    }

    public void leggTilRefusjonsperiode(RefusjonsperiodeEntitet refusjonsperiode) {
        refusjonsperiode.setRefusjonsandel(this);
        refusjonsperioder.add(refusjonsperiode);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RefusjonsandelEntitet kladd = new RefusjonsandelEntitet();

        private Builder() {
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medKilde(RefusjonKilde kilde) {
            kladd.kilde = kilde;
            return this;
        }

        public Builder medJournalpostId(String journalpostId) {
            kladd.journalpostId = journalpostId;
            return this;
        }

        public Builder leggTilRefusjonsperiode(RefusjonsperiodeEntitet refusjonsperiode) {
            kladd.leggTilRefusjonsperiode(refusjonsperiode);
            return this;
        }

        public RefusjonsandelEntitet build() {
            Objects.requireNonNull(kladd.arbeidsgiver, "arbeidsgiver");
            Objects.requireNonNull(kladd.kilde, "kilde");
            return kladd;
        }
    }
}
