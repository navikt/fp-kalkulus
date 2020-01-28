package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.InntektPeriodeType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;

public class YtelseStørrelseDtoBuilder {
    private final YtelseStørrelseDto ytelseStørrelse;

    YtelseStørrelseDtoBuilder(YtelseStørrelseDto ytelseStørrelse) {
        this.ytelseStørrelse = ytelseStørrelse;
    }

    public static YtelseStørrelseDtoBuilder ny() {
        return new YtelseStørrelseDtoBuilder(new YtelseStørrelseDto());
    }

    public YtelseStørrelseDtoBuilder medVirksomhet(String virksomhetOrgnr) {
        return medVirksomhet(virksomhetOrgnr == null ? null : new OrgNummer(virksomhetOrgnr));
    }

    public YtelseStørrelseDtoBuilder medBeløp(BigDecimal verdi) {
        BigDecimal verdiNotNull = verdi != null ? verdi : new BigDecimal(0);
        this.ytelseStørrelse.setBeløp(new Beløp(verdiNotNull));
        return this;
    }

    public YtelseStørrelseDtoBuilder medHyppighet(InntektPeriodeType frekvens) {
        this.ytelseStørrelse.setHyppighet(frekvens);
        return this;
    }

    public YtelseStørrelseDto build() {
        if (ytelseStørrelse.hasValues()) {
            return ytelseStørrelse;
        }
        throw new IllegalStateException();
    }

    public YtelseStørrelseDtoBuilder medVirksomhet(OrgNummer orgNummer) {
        this.ytelseStørrelse.setVirksomhet(orgNummer);
        return this;
    }

}
