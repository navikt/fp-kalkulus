package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import java.util.Objects;

public class BeregningsgrunnlagFrilansAndelDto {

    private BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel;
    private Boolean mottarYtelse;
    private Boolean nyoppstartet;

    private BeregningsgrunnlagFrilansAndelDto() {
    }

    public BeregningsgrunnlagFrilansAndelDto(BeregningsgrunnlagFrilansAndelDto kopiereFra) {
        this.mottarYtelse = kopiereFra.mottarYtelse;
        this.nyoppstartet = kopiereFra.nyoppstartet;
    }

    public BeregningsgrunnlagPrStatusOgAndelDto getBeregningsgrunnlagPrStatusOgAndel() {
        return beregningsgrunnlagPrStatusOgAndel;
    }

    public Boolean getMottarYtelse() {
        return mottarYtelse;
    }

    public Boolean getNyoppstartet() {
        return nyoppstartet;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagFrilansAndelDto)) {
            return false;
        }
        BeregningsgrunnlagFrilansAndelDto other = (BeregningsgrunnlagFrilansAndelDto) obj;
        return Objects.equals(this.getMottarYtelse(), other.getMottarYtelse())
                && Objects.equals(this.getNyoppstartet(), other.getNyoppstartet());
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


    public static BeregningsgrunnlagFrilansAndelDto.Builder builder() {
        return new BeregningsgrunnlagFrilansAndelDto.Builder();
    }

    public static BeregningsgrunnlagFrilansAndelDto.Builder builder(BeregningsgrunnlagFrilansAndelDto eksisterendeBGFrilansAndel) {
        return new BeregningsgrunnlagFrilansAndelDto.Builder(eksisterendeBGFrilansAndel);
    }

    public static class Builder {
        private BeregningsgrunnlagFrilansAndelDto beregningsgrunnlagFrilansAndelMal;
        private boolean erOppdatering;

        public Builder() {
            beregningsgrunnlagFrilansAndelMal = new BeregningsgrunnlagFrilansAndelDto();
        }

        public Builder(BeregningsgrunnlagFrilansAndelDto eksisterendeBGFrilansAndelMal) {
            beregningsgrunnlagFrilansAndelMal = new BeregningsgrunnlagFrilansAndelDto(eksisterendeBGFrilansAndelMal);
        }

        public Builder(BeregningsgrunnlagFrilansAndelDto eksisterendeBGFrilansAndelMal, boolean erOppdatering) {
            beregningsgrunnlagFrilansAndelMal = eksisterendeBGFrilansAndelMal;
            this.erOppdatering = erOppdatering;
        }

        public static Builder kopier(BeregningsgrunnlagFrilansAndelDto beregningsgrunnlagFrilansAndel) {
            return new Builder(beregningsgrunnlagFrilansAndel);
        }

        public static Builder oppdatere(BeregningsgrunnlagFrilansAndelDto beregningsgrunnlagFrilansAndel) {
            return new Builder(beregningsgrunnlagFrilansAndel, true);
        }

        BeregningsgrunnlagFrilansAndelDto.Builder medMottarYtelse(Boolean mottarYtelse) {
            beregningsgrunnlagFrilansAndelMal.mottarYtelse = mottarYtelse;
            return this;
        }

        public BeregningsgrunnlagFrilansAndelDto.Builder medNyoppstartet(Boolean nyoppstartet) {
            beregningsgrunnlagFrilansAndelMal.nyoppstartet = nyoppstartet;
            return this;
        }

        public BeregningsgrunnlagFrilansAndelDto build(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
            beregningsgrunnlagFrilansAndelMal.beregningsgrunnlagPrStatusOgAndel = beregningsgrunnlagPrStatusOgAndel;
            verifyStateForBuild(beregningsgrunnlagPrStatusOgAndel);
            return beregningsgrunnlagFrilansAndelMal;
        }

        public void verifyStateForBuild(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
            if (!beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().erFrilanser()) {
                throw new IllegalArgumentException("Andel med frilansfelt må ha aktivitetstatus frilans");
            }
        }
    }
}
