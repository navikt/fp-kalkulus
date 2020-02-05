package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import java.util.Objects;

public class BeregningsgrunnlagArbeidstakerAndelDto {

    private BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel;
    private Boolean mottarYtelse;
    private boolean harInntektsmelding = false;

    private BeregningsgrunnlagArbeidstakerAndelDto() {
    }

    public BeregningsgrunnlagArbeidstakerAndelDto(BeregningsgrunnlagArbeidstakerAndelDto eksisterendeBGArbeidstakerAndelMal) {
        this.mottarYtelse = eksisterendeBGArbeidstakerAndelMal.mottarYtelse;
    }

    public static Builder builder() {
        return new BeregningsgrunnlagArbeidstakerAndelDto.Builder();
    }

    public static Builder builder(BeregningsgrunnlagArbeidstakerAndelDto eksisterendeBGArbeidstakerAndel) {
        return eksisterendeBGArbeidstakerAndel == null ? new Builder() : new Builder(eksisterendeBGArbeidstakerAndel);
    }

    public BeregningsgrunnlagPrStatusOgAndelDto getBeregningsgrunnlagPrStatusOgAndel() {
        return beregningsgrunnlagPrStatusOgAndel;
    }

    public Boolean getMottarYtelse() {
        return mottarYtelse;
    }

    public boolean getHarInntektsmelding() {
        return harInntektsmelding;
    }

    void setBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
        this.beregningsgrunnlagPrStatusOgAndel = beregningsgrunnlagPrStatusOgAndel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagArbeidstakerAndelDto)) {
            return false;
        }
        BeregningsgrunnlagArbeidstakerAndelDto other = (BeregningsgrunnlagArbeidstakerAndelDto) obj;
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
        private BeregningsgrunnlagArbeidstakerAndelDto beregningsgrunnlagArbeidstakerAndelMal;
        private boolean erOppdatering;

        public Builder() {
            beregningsgrunnlagArbeidstakerAndelMal = new BeregningsgrunnlagArbeidstakerAndelDto();
        }

        public Builder(BeregningsgrunnlagArbeidstakerAndelDto eksisterendeBGArbeidstakerAndelMal) {
            beregningsgrunnlagArbeidstakerAndelMal = new BeregningsgrunnlagArbeidstakerAndelDto(eksisterendeBGArbeidstakerAndelMal);
        }

        public Builder(BeregningsgrunnlagArbeidstakerAndelDto eksisterendeBGArbeidstakerAndelMal, boolean erOppdatering) {
            beregningsgrunnlagArbeidstakerAndelMal = eksisterendeBGArbeidstakerAndelMal;
            this.erOppdatering = erOppdatering;
        }

        public static Builder kopier(BeregningsgrunnlagArbeidstakerAndelDto beregningsgrunnlagArbeidstakerAndel) {
            return new Builder(beregningsgrunnlagArbeidstakerAndel);
        }

        public static Builder oppdatere(BeregningsgrunnlagArbeidstakerAndelDto beregningsgrunnlagArbeidstakerAndel) {
            return new Builder(beregningsgrunnlagArbeidstakerAndel, true);
        }

        public BeregningsgrunnlagArbeidstakerAndelDto.Builder medHarInntektsmelding(boolean harInntektsmelding) {
            beregningsgrunnlagArbeidstakerAndelMal.harInntektsmelding = harInntektsmelding;
            return this;
        }
        public BeregningsgrunnlagArbeidstakerAndelDto.Builder medMottarYtelse(Boolean mottarYtelse) {
            beregningsgrunnlagArbeidstakerAndelMal.mottarYtelse = mottarYtelse;
            return this;
        }

        public BeregningsgrunnlagArbeidstakerAndelDto build(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
            beregningsgrunnlagArbeidstakerAndelMal.beregningsgrunnlagPrStatusOgAndel = beregningsgrunnlagPrStatusOgAndel;
            verifyStateForBuild(beregningsgrunnlagPrStatusOgAndel);
            return beregningsgrunnlagArbeidstakerAndelMal;
        }

        public BeregningsgrunnlagArbeidstakerAndelDto build() {
            return beregningsgrunnlagArbeidstakerAndelMal;
        }

        public void verifyStateForBuild(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
            if (!beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().erArbeidstaker()) {
                throw new IllegalArgumentException("Andel med arbeidstakerfelt må ha aktivitetstatus arbeidstaker");
            }
        }
    }
}

