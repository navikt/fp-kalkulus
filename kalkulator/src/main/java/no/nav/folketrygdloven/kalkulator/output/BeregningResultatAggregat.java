package no.nav.folketrygdloven.kalkulator.output;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;


public class BeregningResultatAggregat {

    private List<BeregningAksjonspunktResultat> beregningAksjonspunktResultater = new ArrayList<>();

    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag;

    private BeregningVilkårResultat beregningVilkårResultat;

    private RegelSporingAggregat regelSporingAggregat;

    public List<BeregningAksjonspunktResultat> getBeregningAksjonspunktResultater() {
        return beregningAksjonspunktResultater;
    }

    public BeregningsgrunnlagGrunnlagDto getBeregningsgrunnlagGrunnlag() {
        return beregningsgrunnlagGrunnlag;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Forventet å ha beregningsgrunnlag"));
    }

    public BeregningVilkårResultat getBeregningVilkårResultat() {
        return beregningVilkårResultat;
    }


    public Optional<RegelSporingAggregat> getRegelSporingAggregat() {
        return Optional.ofNullable(regelSporingAggregat);
    }

    public static class Builder {

        private BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder;
        private BeregningsgrunnlagTilstand tilstand;
        private final BeregningResultatAggregat kladd  = new BeregningResultatAggregat();
        private LocalDate skjæringstidspunkt;

        private Builder(BeregningsgrunnlagInput input) {
            if (input.getSkjæringstidspunktForBeregning() != null) {
                this.skjæringstidspunkt = input.getSkjæringstidspunktForBeregning();
            } else {
                this.skjæringstidspunkt = input.getSkjæringstidspunktOpptjening();
            }
            this.grunnlagBuilder = input.getBeregningsgrunnlagGrunnlag() == null ? BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()) : BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        }

        public static Builder fra(BeregningsgrunnlagInput input) {
            return new Builder(input);
        }
        public Builder medRegisterAktiviteter(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
            grunnlagBuilder.medRegisterAktiviteter(beregningAktivitetAggregat);
            this.tilstand = BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;
            return this;
        }

        public Builder medBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand tilstand) {
            grunnlagBuilder.medBeregningsgrunnlag(beregningsgrunnlag);
            this.tilstand = tilstand;
            return this;
        }

        public Builder medAksjonspunkter(List<BeregningAksjonspunktResultat> beregningAksjonspunktResultater) {
            this.kladd.beregningAksjonspunktResultater = beregningAksjonspunktResultater;
            return this;
        }

        public Builder medAvslåttVilkårIHelePerioden(Vilkårsavslagsårsak vilkårsavslagsårsak) {
            this.kladd.beregningVilkårResultat = new BeregningVilkårResultat(false, vilkårsavslagsårsak, Intervall.fraOgMed(skjæringstidspunkt));
            return this;
        }

        public Builder medVilkårResultat(BeregningVilkårResultat vilkårResultat) {
            this.kladd.beregningVilkårResultat = vilkårResultat;
            return this;
        }

        public Builder medRegelSporingAggregat(RegelSporingAggregat regelsporing) {
            this.kladd.regelSporingAggregat = regelsporing;
            return this;
        }


        public BeregningResultatAggregat build() {
            if (this.tilstand != null && this.grunnlagBuilder != null) {
                this.kladd.beregningsgrunnlagGrunnlag = grunnlagBuilder.build(tilstand);
                return kladd;
            } else if (this.kladd.beregningAksjonspunktResultater != null) {
                return kladd;
            }
            throw new IllegalStateException("Må sette enten beregningsgrunnlag eller beregningaksjonspunkter på beregningresultataggregat!");
        }

    }


}
