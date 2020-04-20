package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.TemaUnderkategori;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

public class YtelseDtoBuilder {

    private final YtelseDto ytelse;
    private final boolean oppdaterer;

    private YtelseDtoBuilder(YtelseDto ytelse, boolean oppdaterer) {
        this.ytelse = ytelse;
        this.oppdaterer = oppdaterer;
    }

    private static YtelseDtoBuilder ny() {
        return new YtelseDtoBuilder(new YtelseDto(), false);
    }

    private static YtelseDtoBuilder oppdatere(YtelseDto oppdatere) {
        return new YtelseDtoBuilder(oppdatere, true);
    }

    public static YtelseDtoBuilder oppdatere(Optional<YtelseDto> oppdatere) {
        return oppdatere.map(YtelseDtoBuilder::oppdatere).orElseGet(YtelseDtoBuilder::ny);
    }

    public YtelseDtoBuilder medYtelseType(FagsakYtelseType relatertYtelseType) {
        ytelse.setRelatertYtelseType(relatertYtelseType);
        return this;
    }

    public YtelseDtoBuilder medPeriode(Intervall intervallEntitet) {
        ytelse.setPeriode(intervallEntitet);
        return this;
    }


    public YtelseDtoBuilder medVedtaksDagsats(BigDecimal vedtakDagsats) {
        ytelse.setVedtaksDagsats(new Beløp(vedtakDagsats));
        return this;
    }

    public YtelseDtoBuilder leggTilYtelseAnvist(YtelseAnvistDto ytelseAnvist) {
        ytelse.leggTilYtelseAnvist(ytelseAnvist);
        return this;
    }

    public YtelseDtoBuilder medBehandlingsTema(TemaUnderkategori behandlingsTema) {
        ytelse.setBehandlingsTema(behandlingsTema);
        return this;
    }

    public Intervall getPeriode() {
        return ytelse.getPeriode();
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public YtelseDto build() {
        return ytelse;
    }

    public YtelseAnvistDtoBuilder getAnvistBuilder() {
        return YtelseAnvistDtoBuilder.ny();
    }

    public void tilbakestillAnvisteYtelser() {
        ytelse.tilbakestillAnvisteYtelser();
    }


}
