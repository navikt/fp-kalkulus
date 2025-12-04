package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.NaturalId;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.GrunnlagReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregningsgrunnlagTilstandKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.felles.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

@Entity(name = "BeregningsgrunnlagGrunnlagEntitet")
@Table(name = "GR_BEREGNINGSGRUNNLAG")
public class BeregningsgrunnlagGrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Column(name = "kobling_id", nullable = false, updatable = false, unique = true)
    private Long koblingId;

    @NaturalId
    @DiffIgnore
    @Embedded
    @AttributeOverride(name = "referanse", column = @Column(name = "grunnlag_referanse", updatable = false, unique = true))
    private GrunnlagReferanse grunnlagReferanse;

    @OneToOne
    @JoinColumn(name = "beregningsgrunnlag_id", updatable = false, unique = true)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    @ManyToOne
    @JoinColumn(name = "register_aktiviteter_id", updatable = false, unique = true, nullable = false)
    private AktivitetAggregatEntitet registerAktiviteter;

    @ManyToOne
    @JoinColumn(name = "saksbehandlet_aktiviteter_id", updatable = false, unique = true)
    private AktivitetAggregatEntitet saksbehandletAktiviteter;

    @ManyToOne
    @JoinColumn(name = "overstyrte_aktiviteter_id", updatable = false, unique = true)
    private AktivitetAggregatEntitet overstyringer;

    @ManyToOne
    @JoinColumn(name = "refusjon_overstyringer_id", updatable = false, unique = true)
    private RefusjonOverstyringerEntitet refusjonOverstyringer;

    @ManyToOne
    @JoinColumn(name = "fakta_aggregat_id", updatable = false, unique = true)
    private FaktaAggregatEntitet faktaAggregat;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Convert(converter = BeregningsgrunnlagTilstandKodeverdiConverter.class)
    @Column(name = "steg_opprettet", nullable = false)
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    public BeregningsgrunnlagGrunnlagEntitet() {
    }

    public BeregningsgrunnlagGrunnlagEntitet(BeregningsgrunnlagGrunnlagEntitet grunnlag) {
        this.beregningsgrunnlagTilstand = grunnlag.getBeregningsgrunnlagTilstand();
        this.koblingId = grunnlag.getKoblingId();
        grunnlag.getBeregningsgrunnlag().map(BeregningsgrunnlagEntitet::new).ifPresent(this::setBeregningsgrunnlag);
        this.setRegisterAktiviteter(
            grunnlag.getRegisterAktiviteter() == null ? null : new AktivitetAggregatEntitet(grunnlag.getRegisterAktiviteter()));
        grunnlag.getSaksbehandletAktiviteter().map(AktivitetAggregatEntitet::new).ifPresent(this::setSaksbehandletAktiviteter);
        grunnlag.getOverstyring().map(AktivitetAggregatEntitet::new).ifPresent(this::setOverstyringer);
        grunnlag.getRefusjonOverstyringer().map(RefusjonOverstyringerEntitet::new).ifPresent(this::setRefusjonOverstyringer);

        // Fakta blir kopiert direkte
        grunnlag.getFaktaAggregat().ifPresent(this::setFaktaAggregat);
    }

    public Long getId() {
        return id;
    }

    public Long getKoblingId() {
        return koblingId;
    }

    public Optional<BeregningsgrunnlagEntitet> getBeregningsgrunnlag() {
        return Optional.ofNullable(beregningsgrunnlag);
    }

    public AktivitetAggregatEntitet getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public Optional<AktivitetAggregatEntitet> getSaksbehandletAktiviteter() {
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<AktivitetAggregatEntitet> getOverstyring() {
        return Optional.ofNullable(overstyringer);
    }

    private Optional<AktivitetAggregatEntitet> getOverstyrteAktiviteter() {
        if (overstyringer != null) {
            List<AktivitetEntitet> overstyrteAktiviteter = registerAktiviteter.getAktiviteter()
                .stream()
                .filter(beregningAktivitet -> beregningAktivitet.skalBrukes(overstyringer))
                .toList();
            AktivitetAggregatEntitet.Builder overstyrtBuilder = AktivitetAggregatEntitet.builder()
                .medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
            overstyrteAktiviteter.forEach(aktivitet -> {
                AktivitetEntitet kopiert = AktivitetEntitet.builder(aktivitet).build();
                overstyrtBuilder.leggTilAktivitet(kopiert);
            });
            return Optional.of(overstyrtBuilder.build());
        }
        return Optional.empty();
    }

    public AktivitetAggregatEntitet getGjeldendeAktiviteter() {
        return getOverstyrteAktiviteter().or(this::getSaksbehandletAktiviteter).orElse(registerAktiviteter);
    }

    public Optional<FaktaAggregatEntitet> getFaktaAggregat() {
        return Optional.ofNullable(faktaAggregat);
    }

    public BeregningsgrunnlagTilstand getBeregningsgrunnlagTilstand() {
        return beregningsgrunnlagTilstand;
    }

    public boolean erAktivt() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    void setKoblingId(Long koblingId) {
        this.koblingId = koblingId;
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    void setRegisterAktiviteter(AktivitetAggregatEntitet registerAktiviteter) {
        var finnesAktivitetMedOverstyring = registerAktiviteter.getAktiviteter().stream().anyMatch(os -> os.getOverstyrHandlingType().isPresent());
        if (finnesAktivitetMedOverstyring) {
            throw new IllegalStateException("Overstyringer skal ikke lagres i denne listen, bruk heller overstyringer feltet. Liste med aktiviteter var: " + registerAktiviteter);
        }
        this.registerAktiviteter = registerAktiviteter;
    }

    void setSaksbehandletAktiviteter(AktivitetAggregatEntitet saksbehandletAktiviteter) {
        var finnesAktivitetMedOverstyring = saksbehandletAktiviteter.getAktiviteter().stream().anyMatch(os -> os.getOverstyrHandlingType().isPresent());
        if (finnesAktivitetMedOverstyring) {
            throw new IllegalStateException("Overstyringer skal ikke lagres i denne listen, bruk heller overstyringer feltet. Liste med aktiviteter var: " + saksbehandletAktiviteter);
        }
        this.saksbehandletAktiviteter = saksbehandletAktiviteter;
    }

    void setBeregningsgrunnlagTilstand(BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        this.beregningsgrunnlagTilstand = beregningsgrunnlagTilstand;
    }

    void setFaktaAggregat(FaktaAggregatEntitet faktaAggregat) {
        this.faktaAggregat = faktaAggregat;
    }

    void setOverstyringer(AktivitetAggregatEntitet overstyringer) {
        var alleOverstyringerHarBestemtHandling = overstyringer.getAktiviteter().stream().allMatch(os -> os.getOverstyrHandlingType().isPresent());
        if (!alleOverstyringerHarBestemtHandling) {
            throw new IllegalStateException("Alle overstyringer har ikke satt overstyrHandlingType. Liste med aktiviteter var: " + overstyringer);
        }
        this.overstyringer = overstyringer;
    }

    public Optional<RefusjonOverstyringerEntitet> getRefusjonOverstyringer() {
        return Optional.ofNullable(refusjonOverstyringer);
    }

    void setRefusjonOverstyringer(RefusjonOverstyringerEntitet refusjonOverstyringer) {
        this.refusjonOverstyringer = refusjonOverstyringer;
    }

    public GrunnlagReferanse getGrunnlagReferanse() {
        return grunnlagReferanse;
    }

    public void setGrunnlagReferanse(GrunnlagReferanse grunnlagReferanse) {
        this.grunnlagReferanse = grunnlagReferanse;
    }

    @Override
    public String toString() {
        return "BeregningsgrunnlagGrunnlagEntitet{" + "id=" + id + ", versjon=" + versjon + ", koblingId=" + koblingId + ", grunnlagReferanse="
            + grunnlagReferanse + ", beregningsgrunnlag=" + beregningsgrunnlag + ", registerAktiviteter=" + registerAktiviteter
            + ", saksbehandletAktiviteter=" + saksbehandletAktiviteter + ", overstyringer=" + overstyringer + ", refusjonOverstyringer="
            + refusjonOverstyringer + ", aktiv=" + aktiv + ", beregningsgrunnlagTilstand=" + beregningsgrunnlagTilstand + '}';
    }
}
