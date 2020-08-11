package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

public class BeregningsgrunnlagGrunnlagBuilder {
    private BeregningsgrunnlagGrunnlagEntitet kladd;
    private boolean built;

    private BeregningsgrunnlagGrunnlagBuilder(BeregningsgrunnlagGrunnlagEntitet kladd) {
        this.kladd = kladd;
    }

    static BeregningsgrunnlagGrunnlagBuilder nytt() {
        return new BeregningsgrunnlagGrunnlagBuilder(new BeregningsgrunnlagGrunnlagEntitet());
    }

    public static BeregningsgrunnlagGrunnlagBuilder oppdatere(BeregningsgrunnlagGrunnlagEntitet kladd) {
        return new BeregningsgrunnlagGrunnlagBuilder(new BeregningsgrunnlagGrunnlagEntitet(kladd));
    }

    public static BeregningsgrunnlagGrunnlagBuilder oppdatere(Optional<BeregningsgrunnlagGrunnlagEntitet> kladd) {
        return kladd.map(BeregningsgrunnlagGrunnlagBuilder::oppdatere).orElseGet(BeregningsgrunnlagGrunnlagBuilder::nytt);
    }

    public BeregningsgrunnlagGrunnlagBuilder medBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        verifiserKanModifisere();
        kladd.setBeregningsgrunnlag(beregningsgrunnlag);
        return this;
    }

    public BeregningsgrunnlagGrunnlagBuilder medRegisterAktiviteter(BeregningAktivitetAggregatEntitet registerAktiviteter) {
        verifiserKanModifisere();
        kladd.setRegisterAktiviteter(registerAktiviteter);
        return this;
    }

    public BeregningsgrunnlagGrunnlagBuilder medRefusjonOverstyring(BeregningRefusjonOverstyringerEntitet beregningRefusjonOverstyringer){
        verifiserKanModifisere();
        kladd.setRefusjonOverstyringer(beregningRefusjonOverstyringer);
        return this;
    }

    public BeregningsgrunnlagGrunnlagBuilder medSaksbehandletAktiviteter(BeregningAktivitetAggregatEntitet saksbehandletAktiviteter) {
        verifiserKanModifisere();
        kladd.setSaksbehandletAktiviteter(saksbehandletAktiviteter);
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

    public BeregningsgrunnlagGrunnlagEntitet buildUtenIdOgTilstand() {
        return kladd;
    }

    public BeregningsgrunnlagGrunnlagBuilder medOverstyring(BeregningAktivitetOverstyringerEntitet beregningAktivitetOverstyringer) {
        verifiserKanModifisere();
        kladd.setOverstyringer(beregningAktivitetOverstyringer);
        return this;
    }

    private void verifiserKanModifisere() {
        if(built) {
            throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
        }
    }
}
