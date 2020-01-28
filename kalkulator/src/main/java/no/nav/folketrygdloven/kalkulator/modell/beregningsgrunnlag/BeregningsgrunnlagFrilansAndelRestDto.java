package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import java.util.Objects;

public class BeregningsgrunnlagFrilansAndelRestDto {

    private BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel;
    private Boolean mottarYtelse;
    private Boolean nyoppstartet;

    private BeregningsgrunnlagFrilansAndelRestDto() {
    }

    public BeregningsgrunnlagFrilansAndelRestDto(BeregningsgrunnlagFrilansAndelRestDto kopiereFra) {
        this.mottarYtelse = kopiereFra.mottarYtelse;
        this.nyoppstartet = kopiereFra.nyoppstartet;
    }

    public BeregningsgrunnlagPrStatusOgAndelRestDto getBeregningsgrunnlagPrStatusOgAndel() {
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
        } else if (!(obj instanceof BeregningsgrunnlagFrilansAndelRestDto)) {
            return false;
        }
        BeregningsgrunnlagFrilansAndelRestDto other = (BeregningsgrunnlagFrilansAndelRestDto) obj;
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


    public static BeregningsgrunnlagFrilansAndelRestDto.Builder builder() {
        return new BeregningsgrunnlagFrilansAndelRestDto.Builder();
    }

    public static BeregningsgrunnlagFrilansAndelRestDto.Builder builder(BeregningsgrunnlagFrilansAndelRestDto eksisterendeBGFrilansAndel) {
        return new BeregningsgrunnlagFrilansAndelRestDto.Builder(eksisterendeBGFrilansAndel);
    }

    public static class Builder {
        private BeregningsgrunnlagFrilansAndelRestDto beregningsgrunnlagFrilansAndelMal;
        private boolean erOppdatering;

        public Builder() {
            beregningsgrunnlagFrilansAndelMal = new BeregningsgrunnlagFrilansAndelRestDto();
        }

        public Builder(BeregningsgrunnlagFrilansAndelRestDto eksisterendeBGFrilansAndelMal) {
            beregningsgrunnlagFrilansAndelMal = new BeregningsgrunnlagFrilansAndelRestDto(eksisterendeBGFrilansAndelMal);
        }

        public Builder(BeregningsgrunnlagFrilansAndelRestDto eksisterendeBGFrilansAndelMal, boolean erOppdatering) {
            beregningsgrunnlagFrilansAndelMal = eksisterendeBGFrilansAndelMal;
            this.erOppdatering = erOppdatering;
        }

        public static Builder kopier(BeregningsgrunnlagFrilansAndelRestDto beregningsgrunnlagFrilansAndel) {
            return new Builder(beregningsgrunnlagFrilansAndel);
        }

        public static Builder oppdatere(BeregningsgrunnlagFrilansAndelRestDto beregningsgrunnlagFrilansAndel) {
            return new Builder(beregningsgrunnlagFrilansAndel, true);
        }

        BeregningsgrunnlagFrilansAndelRestDto.Builder medMottarYtelse(Boolean mottarYtelse) {
            beregningsgrunnlagFrilansAndelMal.mottarYtelse = mottarYtelse;
            return this;
        }

        public BeregningsgrunnlagFrilansAndelRestDto.Builder medNyoppstartet(Boolean nyoppstartet) {
            beregningsgrunnlagFrilansAndelMal.nyoppstartet = nyoppstartet;
            return this;
        }

        public BeregningsgrunnlagFrilansAndelRestDto build(BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel) {
            beregningsgrunnlagFrilansAndelMal.beregningsgrunnlagPrStatusOgAndel = beregningsgrunnlagPrStatusOgAndel;
            verifyStateForBuild(beregningsgrunnlagPrStatusOgAndel);
            return beregningsgrunnlagFrilansAndelMal;
        }

        public void verifyStateForBuild(BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel) {
            if (!beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().erFrilanser()) {
                throw new IllegalArgumentException("Andel med frilansfelt må ha aktivitetstatus frilans");
            }
        }
    }
}
