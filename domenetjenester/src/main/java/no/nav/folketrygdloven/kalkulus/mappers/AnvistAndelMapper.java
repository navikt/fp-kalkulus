package no.nav.folketrygdloven.kalkulus.mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseFordelingDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

class AnvistAndelMapper {

    private final no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    public AnvistAndelMapper(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        this.iayGrunnlag = Objects.requireNonNull(iayGrunnlag, "iayGrunnlag");
    }

    public List<AnvistAndel> mapAnvisteAndeler(no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseAnvistDto ytelseAnvist,
                                               no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseGrunnlagDto ytelseGrunnlag) {
        if (ytelseGrunnlag != null && ytelseGrunnlag.getFordeling() != null && Arbeidskategori.ARBEIDSTAKER.equals(ytelseGrunnlag.getArbeidskategori())) {
            boolean harKunGyldigeArbeidsgivere = harKunGyldigeArbeidsgivere(ytelseGrunnlag);
            if (harKunGyldigeArbeidsgivere) {
                BigDecimal utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent() == null ?
                        BigDecimal.valueOf(100) : ytelseAnvist.getUtbetalingsgradProsent();
                return ytelseGrunnlag.getFordeling().stream()
                        .map(f -> mapYtelsegrunnlagTilAnvistAndel(ytelseAnvist, utbetalingsgradProsent, f))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    /**
     * Brukes til å luke ut NØDNUMMER fra infotrygd.
     *
     * @param ytelseGrunnlag Ytelsegrunnlag fra infotrygd
     * @return har kun gyldige arbeidsgivere i ytelsegrunnlag
     */
    private boolean harKunGyldigeArbeidsgivere(YtelseGrunnlagDto ytelseGrunnlag) {
        List<String> gyldigeIdentifikatorerForYtelse = finnAlleArbeidsgiverIdentifikatorer(iayGrunnlag.getArbeidDto());
        boolean harKunGyldigeArbeidsgivere = ytelseGrunnlag.getFordeling().stream()
                .map(YtelseFordelingDto::getArbeidsgiver)
                .filter(Objects::nonNull)
                .map(Aktør::getIdent)
                .allMatch(gyldigeIdentifikatorerForYtelse::contains);
        return harKunGyldigeArbeidsgivere;
    }

    private AnvistAndel mapYtelsegrunnlagTilAnvistAndel(YtelseAnvistDto ytelseAnvist, BigDecimal utbetalingsgradProsent, YtelseFordelingDto f) {
        Periode anvistPeriode = ytelseAnvist.getAnvistPeriode();
        int antallVirkedager = Virkedager.beregnAntallVirkedagerEllerKunHelg(anvistPeriode.getFom(), anvistPeriode.getTom());
        BigDecimal utbetaling = mapGrunnlagsbeløpTilÅrsbeløp(f).divide(BigDecimal.valueOf(260), RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(antallVirkedager))
                .multiply(utbetalingsgradProsent)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        return new AnvistAndel(MapFraKalkulator.mapArbeidsgiver(f.getArbeidsgiver()),
                utbetaling,
                f.getErRefusjon() ? new Stillingsprosent(100) : Stillingsprosent.ZERO,
                Inntektskategori.ARBEIDSTAKER);
    }

    private List<String> finnAlleArbeidsgiverIdentifikatorer(ArbeidDto arbeidDto) {
        if (arbeidDto != null) {
            return arbeidDto.getYrkesaktiviteter().stream()
                    .map(YrkesaktivitetDto::getArbeidsgiver)
                    .map(Aktør::getIdent)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static BigDecimal mapGrunnlagsbeløpTilÅrsbeløp(YtelseFordelingDto f) {
        return switch (f.getHyppighet()) {
            case ÅRLIG -> f.getBeløp();
            case MÅNEDLIG -> f.getBeløp().multiply(BigDecimal.valueOf(12));
            case DAGLIG -> f.getBeløp().multiply(BigDecimal.valueOf(260));
            case UKENTLIG -> f.getBeløp().multiply(BigDecimal.valueOf(52));
            case BIUKENTLIG -> f.getBeløp().multiply(BigDecimal.valueOf(26));
            default -> throw new IllegalArgumentException("Ugyldig InntektPeriodeType" + f.getHyppighet());
        };
    }

}
