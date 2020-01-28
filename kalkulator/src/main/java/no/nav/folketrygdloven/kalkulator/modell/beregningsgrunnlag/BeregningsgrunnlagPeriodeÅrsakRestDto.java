package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.PeriodeÅrsak;

public class BeregningsgrunnlagPeriodeÅrsakRestDto {

    private BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode;
    private PeriodeÅrsak periodeÅrsak = PeriodeÅrsak.UDEFINERT;

    private BeregningsgrunnlagPeriodeÅrsakRestDto() {
    }

    public BeregningsgrunnlagPeriodeÅrsakRestDto(BeregningsgrunnlagPeriodeÅrsakRestDto kopiereFra) {
        this.periodeÅrsak = kopiereFra.periodeÅrsak;
    }

    public BeregningsgrunnlagPeriodeRestDto getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public PeriodeÅrsak getPeriodeÅrsak() {
        return periodeÅrsak;
    }


    public void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlagPeriode, periodeÅrsak);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeÅrsakRestDto)) {
            return false;
        }
        BeregningsgrunnlagPeriodeÅrsakRestDto other = (BeregningsgrunnlagPeriodeÅrsakRestDto) obj;
        return Objects.equals(this.getBeregningsgrunnlagPeriode(), other.getBeregningsgrunnlagPeriode())
                && Objects.equals(this.getPeriodeÅrsak(), other.getPeriodeÅrsak());
    }

    public static class Builder {
        private BeregningsgrunnlagPeriodeÅrsakRestDto beregningsgrunnlagPeriodeÅrsakMal;

        public Builder() {
            beregningsgrunnlagPeriodeÅrsakMal = new BeregningsgrunnlagPeriodeÅrsakRestDto();
        }

        public Builder(BeregningsgrunnlagPeriodeÅrsakRestDto beregningsgrunnlagPeriodeÅrsakDto) {
            beregningsgrunnlagPeriodeÅrsakMal = new BeregningsgrunnlagPeriodeÅrsakRestDto(beregningsgrunnlagPeriodeÅrsakDto);
        }

        public static Builder kopier(BeregningsgrunnlagPeriodeÅrsakRestDto beregningsgrunnlagPeriodeÅrsakDto) {
            return new Builder(beregningsgrunnlagPeriodeÅrsakDto);
        }

        public Builder medPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            beregningsgrunnlagPeriodeÅrsakMal.periodeÅrsak = periodeÅrsak;
            return this;
        }

        public BeregningsgrunnlagPeriodeÅrsakRestDto build(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode) {
            beregningsgrunnlagPeriodeÅrsakMal.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
            beregningsgrunnlagPeriode.addBeregningsgrunnlagPeriodeÅrsak(beregningsgrunnlagPeriodeÅrsakMal);
            return beregningsgrunnlagPeriodeÅrsakMal;
        }
    }
}
