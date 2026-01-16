package no.nav.folketrygdloven.kalkulus.domene.mappers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Virkedager;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Periode;

class AnvistAndelMapper {

    private AnvistAndelMapper() {
    }

    public static List<AnvistAndel> mapAnvisteAndeler(no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.ytelse.YtelseAnvistDto ytelseAnvist) {
        if (ytelseAnvist.getAnvisteAndeler() != null) {
            return ytelseAnvist.getAnvisteAndeler().stream()
                    .map(aa -> mapAnvistAndelFraVedtak(aa, ytelseAnvist.getAnvistPeriode()))
                    .toList();
        }
        return Collections.emptyList();
    }

    private static AnvistAndel mapAnvistAndelFraVedtak(no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.ytelse.AnvistAndel aa, Periode anvistPeriode) {
        int antallVirkedager = Virkedager.beregnAntallVirkedagerEllerKunHelg(anvistPeriode.getFom(), anvistPeriode.getTom());
        return new AnvistAndel(
                MapFraKalkulator.mapArbeidsgiver(aa.getArbeidsgiver()),
                MapIAYTilKalulator.mapArbeidsforholdRef(aa.getArbeidsforholdId()),
                beløpFraDto(aa.getDagsats()).multipliser(BigDecimal.valueOf(antallVirkedager)),
                beløpFraDto(aa.getDagsats()),
                aa.getRefusjonsgrad() == null ? null : Stillingsprosent.fra(aa.getRefusjonsgrad().verdi()),
                aa.getInntektskategori() != null ? aa.getInntektskategori() : Inntektskategori.UDEFINERT
        );
    }

    private static Beløp beløpFraDto(no.nav.foreldrepenger.kalkulus.kontrakt.typer.Beløp beløp) {
        return Beløp.fra(beløp != null ? beløp.verdi() : null);
    }


}
