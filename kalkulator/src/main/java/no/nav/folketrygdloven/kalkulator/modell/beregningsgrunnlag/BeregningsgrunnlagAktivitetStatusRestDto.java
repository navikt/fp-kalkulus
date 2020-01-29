package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;

public class BeregningsgrunnlagAktivitetStatusRestDto {

    private BeregningsgrunnlagRestDto beregningsgrunnlag;
    private AktivitetStatus aktivitetStatus;
    private Hjemmel hjemmel;

    public BeregningsgrunnlagAktivitetStatusRestDto() {
    }

    public BeregningsgrunnlagAktivitetStatusRestDto(BeregningsgrunnlagAktivitetStatusRestDto o) {
        this.aktivitetStatus = o.aktivitetStatus;
        this.hjemmel = o.hjemmel;
    }


    public BeregningsgrunnlagRestDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Hjemmel getHjemmel() {
        return hjemmel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagAktivitetStatusRestDto)) {
            return false;
        }
        BeregningsgrunnlagAktivitetStatusRestDto other = (BeregningsgrunnlagAktivitetStatusRestDto) obj;
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
                && Objects.equals(this.getBeregningsgrunnlag(), other.getBeregningsgrunnlag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, aktivitetStatus);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "beregningsgrunnlag=" + beregningsgrunnlag + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "aktivitetStatus=" + aktivitetStatus + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "hjemmel=" + hjemmel + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setBeregningsgrunnlagDto(BeregningsgrunnlagRestDto beregningsgrunnlagDto) {
        this.beregningsgrunnlag = beregningsgrunnlagDto;
    }

    public static class Builder {
        private BeregningsgrunnlagAktivitetStatusRestDto beregningsgrunnlagAktivitetStatusMal;

        public Builder() {
            beregningsgrunnlagAktivitetStatusMal = new BeregningsgrunnlagAktivitetStatusRestDto();
            beregningsgrunnlagAktivitetStatusMal.hjemmel = Hjemmel.UDEFINERT;
        }

        public Builder(BeregningsgrunnlagAktivitetStatusRestDto o) {
            beregningsgrunnlagAktivitetStatusMal = new BeregningsgrunnlagAktivitetStatusRestDto(o);
        }

        public static Builder kopier(BeregningsgrunnlagAktivitetStatusRestDto o) {
            return new Builder(o);
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            beregningsgrunnlagAktivitetStatusMal.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medHjemmel(Hjemmel hjemmel) {
            beregningsgrunnlagAktivitetStatusMal.hjemmel = hjemmel;
            return this;
        }

        public BeregningsgrunnlagAktivitetStatusRestDto build(BeregningsgrunnlagRestDto beregningsgrunnlag) {
            beregningsgrunnlagAktivitetStatusMal.beregningsgrunnlag = beregningsgrunnlag;
            verifyStateForBuild();
            beregningsgrunnlag.leggTilBeregningsgrunnlagAktivitetStatus(beregningsgrunnlagAktivitetStatusMal);
            return beregningsgrunnlagAktivitetStatusMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagAktivitetStatusMal.beregningsgrunnlag, "beregningsgrunnlag");
            Objects.requireNonNull(beregningsgrunnlagAktivitetStatusMal.aktivitetStatus, "aktivitetStatus");
            Objects.requireNonNull(beregningsgrunnlagAktivitetStatusMal.getHjemmel(), "hjemmel");
        }
    }
}
