package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import java.util.Objects;

public class BeregningsgrunnlagArbeidstakerAndelRestDto {

    private BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel;
    private Boolean mottarYtelse;
    private boolean harInntektsmelding = false;

    private BeregningsgrunnlagArbeidstakerAndelRestDto() {
    }

    public BeregningsgrunnlagArbeidstakerAndelRestDto(BeregningsgrunnlagArbeidstakerAndelRestDto eksisterendeBGArbeidstakerAndelMal) {
        this.mottarYtelse = eksisterendeBGArbeidstakerAndelMal.mottarYtelse;
    }

    public static BeregningsgrunnlagArbeidstakerAndelRestDto.Builder builder() {
        return new BeregningsgrunnlagArbeidstakerAndelRestDto.Builder();
    }

    public static BeregningsgrunnlagArbeidstakerAndelRestDto.Builder builder(BeregningsgrunnlagArbeidstakerAndelRestDto eksisterendeBGArbeidstakerAndel) {
        return eksisterendeBGArbeidstakerAndel == null ? new Builder() : new BeregningsgrunnlagArbeidstakerAndelRestDto.Builder(eksisterendeBGArbeidstakerAndel);
    }

    public BeregningsgrunnlagPrStatusOgAndelRestDto getBeregningsgrunnlagPrStatusOgAndel() {
        return beregningsgrunnlagPrStatusOgAndel;
    }

    public Boolean getMottarYtelse() {
        return mottarYtelse;
    }

    public boolean getHarInntektsmelding() {
        return harInntektsmelding;
    }

    public void setBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel) {
        this.beregningsgrunnlagPrStatusOgAndel = beregningsgrunnlagPrStatusOgAndel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagArbeidstakerAndelRestDto)) {
            return false;
        }
        BeregningsgrunnlagArbeidstakerAndelRestDto other = (BeregningsgrunnlagArbeidstakerAndelRestDto) obj;
        return Objects.equals(this.getMottarYtelse(), other.getMottarYtelse());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mottarYtelse);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "mottarYtelse=" + mottarYtelse + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static class Builder {
        private BeregningsgrunnlagArbeidstakerAndelRestDto beregningsgrunnlagArbeidstakerAndelMal;
        private boolean erOppdatering;

        public Builder() {
            beregningsgrunnlagArbeidstakerAndelMal = new BeregningsgrunnlagArbeidstakerAndelRestDto();
        }

        public Builder(BeregningsgrunnlagArbeidstakerAndelRestDto eksisterendeBGArbeidstakerAndelMal) {
            beregningsgrunnlagArbeidstakerAndelMal = new BeregningsgrunnlagArbeidstakerAndelRestDto(eksisterendeBGArbeidstakerAndelMal);
        }

        public Builder(BeregningsgrunnlagArbeidstakerAndelRestDto eksisterendeBGArbeidstakerAndelMal, boolean erOppdatering) {
            beregningsgrunnlagArbeidstakerAndelMal = eksisterendeBGArbeidstakerAndelMal;
            this.erOppdatering = erOppdatering;
        }

        public static Builder kopier(BeregningsgrunnlagArbeidstakerAndelRestDto beregningsgrunnlagArbeidstakerAndel) {
            return new Builder(beregningsgrunnlagArbeidstakerAndel);
        }

        public static Builder oppdatere(BeregningsgrunnlagArbeidstakerAndelRestDto beregningsgrunnlagArbeidstakerAndel) {
            return new Builder(beregningsgrunnlagArbeidstakerAndel, true);
        }


        public BeregningsgrunnlagArbeidstakerAndelRestDto.Builder medMottarYtelse(Boolean mottarYtelse) {
            beregningsgrunnlagArbeidstakerAndelMal.mottarYtelse = mottarYtelse;
            return this;
        }

        public BeregningsgrunnlagArbeidstakerAndelRestDto.Builder medHarInntektsmelding(boolean harInntektsmelding) {
            beregningsgrunnlagArbeidstakerAndelMal.harInntektsmelding = harInntektsmelding;
            return this;
        }

        public BeregningsgrunnlagArbeidstakerAndelRestDto build(BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel) {
            beregningsgrunnlagArbeidstakerAndelMal.beregningsgrunnlagPrStatusOgAndel = beregningsgrunnlagPrStatusOgAndel;
            verifyStateForBuild(beregningsgrunnlagPrStatusOgAndel);
            return beregningsgrunnlagArbeidstakerAndelMal;
        }

        public BeregningsgrunnlagArbeidstakerAndelRestDto build() {
            return beregningsgrunnlagArbeidstakerAndelMal;
        }

        public void verifyStateForBuild(BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel) {
            if (!beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().erArbeidstaker()) {
                throw new IllegalArgumentException("Andel med arbeidstakerfelt må ha aktivitetstatus arbeidstaker");
            }
        }
    }
}

