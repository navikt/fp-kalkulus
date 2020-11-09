package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

public class FaktaAktørDto {

    private Boolean erNyIArbeidslivetSN;
    private Boolean erNyoppstartetFL;
    private Boolean harFLMottattYtelse;
    private Boolean skalBeregnesSomMilitær;
    private Boolean skalBesteberegnes;
    private Boolean mottarEtterlønnSluttpakke;

    public FaktaAktørDto() { }

    public FaktaAktørDto(FaktaAktørDto original) {
        this.erNyIArbeidslivetSN = original.getErNyIArbeidslivetSN();
        this.erNyoppstartetFL = original.getErNyoppstartetFL();
        this.harFLMottattYtelse = original.getHarFLMottattYtelse();
        this.skalBesteberegnes = original.getSkalBesteberegnes();
        this.mottarEtterlønnSluttpakke = original.getMottarEtterlønnSluttpakke();
        this.skalBeregnesSomMilitær = original.getSkalBeregnesSomMilitær();
    }

    public Boolean getErNyIArbeidslivetSN() {
        return erNyIArbeidslivetSN;
    }

    public Boolean getErNyoppstartetFL() {
        return erNyoppstartetFL;
    }

    public Boolean getHarFLMottattYtelse() {
        return harFLMottattYtelse;
    }

    public Boolean getSkalBesteberegnes() {
        return skalBesteberegnes;
    }

    public Boolean getMottarEtterlønnSluttpakke() {
        return mottarEtterlønnSluttpakke;
    }

    public Boolean getSkalBeregnesSomMilitær() {
        return skalBeregnesSomMilitær;
    }

    @Override
    public String toString() {
        return "FaktaAktørDto{" +
                "erNyIArbeidslivetSN=" + erNyIArbeidslivetSN +
                ", erNyoppstartetFL=" + erNyoppstartetFL +
                ", harFLMottattYtelse=" + harFLMottattYtelse +
                ", skalBesteberegnes=" + skalBesteberegnes +
                ", mottarEtterlønnSluttpakke=" + mottarEtterlønnSluttpakke +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(FaktaAktørDto kopi) {
        return new Builder(kopi);
    }

    public static class Builder {
        private FaktaAktørDto mal;

        private Builder() {
            mal = new FaktaAktørDto();
        }

        private Builder(FaktaAktørDto faktaAktørDto) {
            mal = new FaktaAktørDto(faktaAktørDto);
        }

        static Builder oppdater(FaktaAktørDto faktaAktørDto) {
            return faktaAktørDto == null ? new Builder() : new Builder(faktaAktørDto);
        }

        public Builder medErNyIArbeidslivetSN(Boolean erNyIArbeidslivetSN) {
            mal.erNyIArbeidslivetSN = erNyIArbeidslivetSN;
            return this;
        }

        public Builder medErNyoppstartetFL(Boolean erNyoppstartetFL) {
            mal.erNyoppstartetFL = erNyoppstartetFL;
            return this;
        }

        public Builder medHarFLMottattYtelse(Boolean harFLMottattYtelse) {
            mal.harFLMottattYtelse = harFLMottattYtelse;
            return this;
        }

        public Builder medSkalBesteberegnes(Boolean skalBesteberegnes) {
            mal.skalBesteberegnes = skalBesteberegnes;
            return this;
        }

        public Builder medErMilitærSiviltjeneste(Boolean skalBeregnesSomMilitær) {
            mal.skalBeregnesSomMilitær = skalBeregnesSomMilitær;
            return this;
        }


        public Builder medMottarEtterlønnSluttpakke(Boolean mottarEtterlønnSluttpakke) {
            mal.mottarEtterlønnSluttpakke = mottarEtterlønnSluttpakke;
            return this;
        }

        public FaktaAktørDto build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            if (erUgyldig()) {
                throw new IllegalStateException("Må ha satt minst et faktafelt.");
            }
        }

        // Brukes av fp-sak og må vere public
        public boolean erUgyldig() {
            return mal.erNyIArbeidslivetSN == null
                    && mal.erNyoppstartetFL == null
                    && mal.skalBeregnesSomMilitær == null
                    && mal.harFLMottattYtelse == null
                    && mal.skalBesteberegnes == null
                    && mal.mottarEtterlønnSluttpakke == null;
        }

    }
}
