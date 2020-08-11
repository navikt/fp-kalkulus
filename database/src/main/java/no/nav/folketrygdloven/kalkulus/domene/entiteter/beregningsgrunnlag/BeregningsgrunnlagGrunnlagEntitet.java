package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.NaturalId;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.GrunnlagReferanse;
import no.nav.folketrygdloven.kalkulus.felles.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

@Entity(name = "BeregningsgrunnlagGrunnlagEntitet")
@Table(name = "GR_BEREGNINGSGRUNNLAG")
public class BeregningsgrunnlagGrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_BEREGNINGSGRUNNLAG")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "kobling_id", nullable = false, updatable = false, unique = true)
    private Long koblingId;

    @NaturalId
    @DiffIgnore
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "referanse", column = @Column(name = "grunnlag_referanse", updatable = false, unique = true))
    })
    private GrunnlagReferanse grunnlagReferanse;

    @OneToOne
    @JoinColumn(name = "beregningsgrunnlag_id", updatable = false, unique = true)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    @ManyToOne
    @JoinColumn(name = "register_aktiviteter_id", updatable = false, unique = true, nullable = false)
    private BeregningAktivitetAggregatEntitet registerAktiviteter;

    @ManyToOne
    @JoinColumn(name = "saksbehandlet_aktiviteter_id", updatable = false, unique = true)
    private BeregningAktivitetAggregatEntitet saksbehandletAktiviteter;

    @ManyToOne
    @JoinColumn(name = "ba_overstyringer_id", updatable = false, unique = true)
    private BeregningAktivitetOverstyringerEntitet overstyringer;

    @ManyToOne
    @JoinColumn(name = "br_overstyringer_id", updatable = false, unique = true)
    private BeregningRefusjonOverstyringerEntitet refusjonOverstyringer;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Convert(converter= BeregningsgrunnlagTilstand.KodeverdiConverter.class)
    @Column(name="steg_opprettet", nullable = false)
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    public BeregningsgrunnlagGrunnlagEntitet() {
    }

    public BeregningsgrunnlagGrunnlagEntitet(BeregningsgrunnlagGrunnlagEntitet grunnlag) {
        grunnlag.getBeregningsgrunnlag().ifPresent(this::setBeregningsgrunnlag);
        this.setRegisterAktiviteter(grunnlag.getRegisterAktiviteter());
        grunnlag.getSaksbehandletAktiviteter().ifPresent(this::setSaksbehandletAktiviteter);
        grunnlag.getOverstyring().ifPresent(this::setOverstyringer);
        grunnlag.getRefusjonOverstyringer().ifPresent(this::setRefusjonOverstyringer);
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

    public BeregningAktivitetAggregatEntitet getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public Optional<BeregningAktivitetAggregatEntitet> getSaksbehandletAktiviteter() {
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<BeregningAktivitetAggregatEntitet> getOverstyrteEllerSaksbehandletAktiviteter() {
        Optional<BeregningAktivitetAggregatEntitet> overstyrteAktiviteter = getOverstyrteAktiviteter();
        if (overstyrteAktiviteter.isPresent()) {
            return overstyrteAktiviteter;
        }
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<BeregningAktivitetOverstyringerEntitet> getOverstyring() {
        return Optional.ofNullable(overstyringer);
    }

    private Optional<BeregningAktivitetAggregatEntitet> getOverstyrteAktiviteter() {
        if (overstyringer != null) {
            List<BeregningAktivitetEntitet> overstyrteAktiviteter = registerAktiviteter.getBeregningAktiviteter().stream()
                    .filter(beregningAktivitet -> beregningAktivitet.skalBrukes(overstyringer))
                    .collect(Collectors.toList());
            BeregningAktivitetAggregatEntitet.Builder overstyrtBuilder = BeregningAktivitetAggregatEntitet.builder()
                    .medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
            overstyrteAktiviteter.forEach(aktivitet -> {
                BeregningAktivitetEntitet kopiert = BeregningAktivitetEntitet.builder(aktivitet).build();
                overstyrtBuilder.leggTilAktivitet(kopiert);
            });
            return Optional.of(overstyrtBuilder.build());
        }
        return Optional.empty();
    }

    public BeregningAktivitetAggregatEntitet getGjeldendeAktiviteter() {
        return getOverstyrteAktiviteter()
                .or(this::getSaksbehandletAktiviteter)
                .orElse(registerAktiviteter);
    }

    public BeregningAktivitetAggregatEntitet getOverstyrteEllerRegisterAktiviteter() {
        Optional<BeregningAktivitetAggregatEntitet> overstyrteAktiviteter = getOverstyrteAktiviteter();
        if (overstyrteAktiviteter.isPresent()) {
            return overstyrteAktiviteter.get();
        }
        return registerAktiviteter;
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

    void setRegisterAktiviteter(BeregningAktivitetAggregatEntitet registerAktiviteter) {
        this.registerAktiviteter = registerAktiviteter;
    }

    void setSaksbehandletAktiviteter(BeregningAktivitetAggregatEntitet saksbehandletAktiviteter) {
        this.saksbehandletAktiviteter = saksbehandletAktiviteter;
    }

    void setBeregningsgrunnlagTilstand(BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        this.beregningsgrunnlagTilstand = beregningsgrunnlagTilstand;
    }

    void setOverstyringer(BeregningAktivitetOverstyringerEntitet overstyringer) {
        this.overstyringer = overstyringer;
    }

    public Optional<BeregningRefusjonOverstyringerEntitet> getRefusjonOverstyringer() {
        return Optional.ofNullable(refusjonOverstyringer);
    }

    void setRefusjonOverstyringer(BeregningRefusjonOverstyringerEntitet refusjonOverstyringer) {
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
        return "BeregningsgrunnlagGrunnlagEntitet{" +
                "id=" + id +
                ", versjon=" + versjon +
                ", koblingId=" + koblingId +
                ", grunnlagReferanse=" + grunnlagReferanse +
                ", beregningsgrunnlag=" + beregningsgrunnlag +
                ", registerAktiviteter=" + registerAktiviteter +
                ", saksbehandletAktiviteter=" + saksbehandletAktiviteter +
                ", overstyringer=" + overstyringer +
                ", refusjonOverstyringer=" + refusjonOverstyringer +
                ", aktiv=" + aktiv +
                ", beregningsgrunnlagTilstand=" + beregningsgrunnlagTilstand +
                '}';
    }
}
