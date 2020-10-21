package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.refusjon;

import java.time.LocalDate;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface MapTilRefusjonOverstyringFeil extends DeklarerteFeil {

    MapTilRefusjonOverstyringFeil FACTORY = FeilFactory.create(MapTilRefusjonOverstyringFeil.class); //NOSONAR

    @TekniskFeil(feilkode = "FT-401650", feilmelding = "Det finnes en startdato for refusjon dato som er før tidligste tillate startdato for refusjon." +
            " Startdato var %s og tidligste tillate startdato var %s", logLevel = LogLevel.ERROR)
    Feil ugyldigStartdatoFeil(LocalDate startdato, LocalDate tidligsteTillateStartdato);
}
