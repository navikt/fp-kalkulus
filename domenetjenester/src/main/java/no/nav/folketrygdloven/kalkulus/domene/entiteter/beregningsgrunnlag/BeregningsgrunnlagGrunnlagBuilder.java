package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class BeregningsgrunnlagGrunnlagBuilder {
    private BeregningsgrunnlagGrunnlagEntitet kladd;
    private boolean built;

    private BeregningsgrunnlagGrunnlagBuilder(BeregningsgrunnlagGrunnlagEntitet kladd) {
        this.kladd = kladd;
    }

    public static BeregningsgrunnlagGrunnlagBuilder nytt() {
        return new BeregningsgrunnlagGrunnlagBuilder(new BeregningsgrunnlagGrunnlagEntitet());
    }

    public static BeregningsgrunnlagGrunnlagBuilder kopiere(BeregningsgrunnlagGrunnlagEntitet kladd) {
        return new BeregningsgrunnlagGrunnlagBuilder(new BeregningsgrunnlagGrunnlagEntitet(kladd));
    }

    public static BeregningsgrunnlagGrunnlagBuilder kopiere(Optional<BeregningsgrunnlagGrunnlagEntitet> kladd) {
        return kladd.map(BeregningsgrunnlagGrunnlagBuilder::kopiere).orElseGet(BeregningsgrunnlagGrunnlagBuilder::nytt);
    }

    public BeregningsgrunnlagGrunnlagBuilder medBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        verifiserKanModifisere();
        kladd.setBeregningsgrunnlag(beregningsgrunnlag);
        return this;
    }

    public BeregningsgrunnlagGrunnlagBuilder medRegisterAktiviteter(AktivitetAggregatEntitet registerAktiviteter) {
        verifiserKanModifisere();
        kladd.setRegisterAktiviteter(registerAktiviteter);
        return this;
    }

    public BeregningsgrunnlagGrunnlagBuilder medRefusjonOverstyring(RefusjonOverstyringerEntitet refusjonOverstyringer){
        verifiserKanModifisere();
        kladd.setRefusjonOverstyringer(refusjonOverstyringer);
        return this;
    }

    public BeregningsgrunnlagGrunnlagBuilder medSaksbehandletAktiviteter(AktivitetAggregatEntitet saksbehandletAktiviteter) {
        verifiserKanModifisere();
        kladd.setSaksbehandletAktiviteter(saksbehandletAktiviteter);
        return this;
    }

    public BeregningsgrunnlagGrunnlagBuilder medFaktaAggregat(FaktaAggregatEntitet faktaAggregatEntitet) {
        verifiserKanModifisere();
        kladd.setFaktaAggregat(faktaAggregatEntitet);
        return this;
    }

    public BeregningsgrunnlagGrunnlagEntitet build(Long koblingId, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        if(built) {
            return kladd;
        }
        Objects.requireNonNull(koblingId, "koblingId");
        Objects.requireNonNull(beregningsgrunnlagTilstand, "beregningsgrunnlagTilstand");
        Objects.requireNonNull(kladd.getRegisterAktiviteter(), "Registeraktiviteter kan ikke vere null: " + kladd);
        kladd.setKoblingId(koblingId);
        kladd.setBeregningsgrunnlagTilstand(beregningsgrunnlagTilstand);
        built = true;
        return kladd;
    }

    public BeregningsgrunnlagGrunnlagBuilder medOverstyring(AktivitetAggregatEntitet aktivitetOverstyringer) {
        verifiserKanModifisere();
        kladd.setOverstyringer(aktivitetOverstyringer);
        return this;
    }

    private void verifiserKanModifisere() {
        if(built) {
            throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
        }
    }
}
