package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;

public class YtelseGrunnlagDtoBuilder {
    private final YtelseGrunnlagDto ytelseGrunnlag;

    YtelseGrunnlagDtoBuilder(YtelseGrunnlagDto ytelseGrunnlag) {
        this.ytelseGrunnlag = ytelseGrunnlag;
    }

    public static YtelseGrunnlagDtoBuilder ny() {
        return new YtelseGrunnlagDtoBuilder(new YtelseGrunnlagDto());
    }

    public YtelseGrunnlagDtoBuilder medArbeidskategori(Arbeidskategori arbeidskategori) {
        this.ytelseGrunnlag.setArbeidskategori(arbeidskategori);
        return this;
    }

    public YtelseGrunnlagDtoBuilder medDekningsgradProsent(BigDecimal prosent) {
        this.ytelseGrunnlag.setDekningsgradProsent(prosent == null ? null: new Stillingsprosent(prosent));
        return this;
    }

    public YtelseGrunnlagDtoBuilder medGraderingProsent(BigDecimal prosent) {
        this.ytelseGrunnlag.setGraderingProsent(prosent == null ? null: new Stillingsprosent(prosent));
        return this;
    }

    public YtelseGrunnlagDtoBuilder medInntektsgrunnlagProsent(BigDecimal prosent) {
        this.ytelseGrunnlag.setInntektsgrunnlagProsent(prosent == null ? null: new Stillingsprosent(prosent));
        return this;
    }

    public YtelseGrunnlagDtoBuilder medOpprinneligIdentdato(LocalDate dato) {
        this.ytelseGrunnlag.setOpprinneligIdentdato(dato);
        return this;
    }

    public YtelseGrunnlagDtoBuilder leggTilYtelseStørrelse(YtelseStørrelseDto ytelseStørrelse) {
        this.ytelseGrunnlag.leggTilYtelseStørrelse(ytelseStørrelse);
        return this;
    }

    public YtelseGrunnlagDtoBuilder medVedtaksDagsats(BigDecimal vedtaksDagsats) {
        this.ytelseGrunnlag.setVedtaksDagsats(new Beløp(vedtaksDagsats));
        return this;
    }

    public YtelseGrunnlagDtoBuilder medVedtaksDagsats(Beløp vedtaksDagsats) {
        this.ytelseGrunnlag.setVedtaksDagsats(vedtaksDagsats);
        return this;
    }

    public void tilbakestillStørrelse() {
        this.ytelseGrunnlag.tilbakestillStørrelse();
    }

    public YtelseGrunnlagDto build() {
        return ytelseGrunnlag;
    }

    public YtelseStørrelseDtoBuilder getStørrelseBuilder() {
        return YtelseStørrelseDtoBuilder.ny();
    }
}
