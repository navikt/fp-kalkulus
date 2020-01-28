package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;

public class BeregningsgrunnlagGrunnlagRestDtoBuilder {
    private BeregningsgrunnlagGrunnlagRestDto kladd;
    private boolean built;

    private BeregningsgrunnlagGrunnlagRestDtoBuilder(BeregningsgrunnlagGrunnlagRestDto kladd) {
        this.kladd = kladd;
    }

    static BeregningsgrunnlagGrunnlagRestDtoBuilder nytt() {
        return new BeregningsgrunnlagGrunnlagRestDtoBuilder(new BeregningsgrunnlagGrunnlagRestDto());
    }

    public static BeregningsgrunnlagGrunnlagRestDtoBuilder oppdatere(BeregningsgrunnlagGrunnlagRestDto kladd) {
        return new BeregningsgrunnlagGrunnlagRestDtoBuilder(new BeregningsgrunnlagGrunnlagRestDto(kladd));
    }

    public static BeregningsgrunnlagGrunnlagRestDtoBuilder oppdatere(Optional<BeregningsgrunnlagGrunnlagRestDto> kladd) {
        return kladd.map(BeregningsgrunnlagGrunnlagRestDtoBuilder::oppdatere).orElseGet(BeregningsgrunnlagGrunnlagRestDtoBuilder::nytt);
    }

    public BeregningsgrunnlagRestDto.Builder getBeregningsgrunnlagBuilder() {
        return BeregningsgrunnlagRestDto.Builder.oppdater(kladd.getBeregningsgrunnlag());
    }

    public BeregningsgrunnlagGrunnlagRestDtoBuilder medBeregningsgrunnlag(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        verifiserKanModifisere();
        kladd.setBeregningsgrunnlag(beregningsgrunnlag);
        return this;
    }

    public BeregningsgrunnlagGrunnlagRestDtoBuilder medRegisterAktiviteter(BeregningAktivitetAggregatRestDto registerAktiviteter) {
        verifiserKanModifisere();
        kladd.setRegisterAktiviteter(registerAktiviteter);
        return this;
    }

    public BeregningsgrunnlagGrunnlagRestDtoBuilder medRefusjonOverstyring(BeregningRefusjonOverstyringerDto beregningRefusjonOverstyringer){
        verifiserKanModifisere();
        kladd.setRefusjonOverstyringer(beregningRefusjonOverstyringer);
        return this;
    }

    public BeregningsgrunnlagGrunnlagRestDtoBuilder medSaksbehandletAktiviteter(BeregningAktivitetAggregatRestDto saksbehandletAktiviteter) {
        verifiserKanModifisere();
        kladd.setSaksbehandletAktiviteter(saksbehandletAktiviteter);
        return this;
    }

    public BeregningsgrunnlagGrunnlagRestDto build(BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        if(built) {
            return kladd;
        }
        Objects.requireNonNull(beregningsgrunnlagTilstand);
        kladd.setBeregningsgrunnlagTilstand(beregningsgrunnlagTilstand);
        built = true;
        return kladd;
    }

    public BeregningsgrunnlagGrunnlagRestDtoBuilder medOverstyring(BeregningAktivitetOverstyringerDto beregningAktivitetOverstyringer) {
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
