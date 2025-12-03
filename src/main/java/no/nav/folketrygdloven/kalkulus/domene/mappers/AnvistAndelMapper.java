package no.nav.folketrygdloven.kalkulus.domene.mappers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Virkedager;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

class AnvistAndelMapper {

    private AnvistAndelMapper() {
    }

    public static List<AnvistAndel> mapAnvisteAndeler(no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseAnvistDto ytelseAnvist) {
        if (ytelseAnvist.getAnvisteAndeler() != null) {
            return ytelseAnvist.getAnvisteAndeler().stream()
                    .map(aa -> mapAnvistAndelFraVedtak(aa, ytelseAnvist.getAnvistPeriode()))
                    .toList();
        }
        return Collections.emptyList();
    }

    private static AnvistAndel mapAnvistAndelFraVedtak(no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.AnvistAndel aa, Periode anvistPeriode) {
        int antallVirkedager = Virkedager.beregnAntallVirkedagerEllerKunHelg(anvistPeriode.getFom(), anvistPeriode.getTom());
        return new AnvistAndel(
                MapFraKalkulator.mapArbeidsgiver(aa.getArbeidsgiver()),
                MapIAYTilKalulator.mapArbeidsforholdRef(aa.getArbeidsforholdId()),
                ModellTyperMapper.beløpFraDto(aa.getDagsats()).multipliser(BigDecimal.valueOf(antallVirkedager)),
                ModellTyperMapper.beløpFraDto(aa.getDagsats()),
                Stillingsprosent.fra(aa.getRefusjonsgrad()),
                aa.getInntektskategori() != null ? aa.getInntektskategori() : Inntektskategori.UDEFINERT
        );
    }


}
