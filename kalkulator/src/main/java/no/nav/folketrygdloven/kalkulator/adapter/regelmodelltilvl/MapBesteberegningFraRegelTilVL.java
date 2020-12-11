package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.besteberegning.modell.output.AktivitetNøkkel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetAndel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;

public class MapBesteberegningFraRegelTilVL {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapBesteberegningFraRegelTilVL.class);

    public static BeregningsgrunnlagDto mapTilBeregningsgrunnlag(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                           BesteberegningOutput output) {
        BeregningsgrunnlagDto gammeltGrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow();
        var nyttGrunnlag = new BeregningsgrunnlagDto(gammeltGrunnlag);
        if  (output.getSkalBeregnesEtterSeksBesteMåneder()) {
            fjernSaksbehandlersBesteberegning(nyttGrunnlag);
            oppdaterBeregningForAndelerIBesteberegnetGrunnlag(nyttGrunnlag, output);
            settBesteberegningTilNullForAndreAndeler(nyttGrunnlag);
            if (harAlleredeBesteberegnet(gammeltGrunnlag)) {
                loggDiff(gammeltGrunnlag, nyttGrunnlag);
                return new BeregningsgrunnlagDto(gammeltGrunnlag);
            }
        }
        return nyttGrunnlag;
    }

    private static void loggDiff(BeregningsgrunnlagDto gammeltGrunnlag, BeregningsgrunnlagDto nyttGrunnlag) {
        var gammelAndelerFørstePeriode = gammeltGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        var andelerFørstePeriode = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        if (harDiffIBesteberegning(gammelAndelerFørstePeriode, andelerFørstePeriode)) {
            LOGGER.info("Oppdaget diff i besteberegning. AUTOMATISK BESTEBEREGNING: " + andelerFørstePeriode
                    + " SAKSBEHANDLERS BESTEBEREGNING: " + gammelAndelerFørstePeriode);
        }
    }

    private static boolean harDiffIBesteberegning(List<BeregningsgrunnlagPrStatusOgAndelDto> gammelAndelerFørstePeriode, List<BeregningsgrunnlagPrStatusOgAndelDto> andelerFørstePeriode) {
        for (int i = 0; i < gammelAndelerFørstePeriode.size(); i++) {
            var gammelAndel = gammelAndelerFørstePeriode.get(i);
            var nyAndel = andelerFørstePeriode.get(i);
            if (gammelAndel.getBesteberegningPrÅr().compareTo(nyAndel.getBesteberegningPrÅr()) != 0) {
                return true;
            }
        }
        return false;
    }

    private static void fjernSaksbehandlersBesteberegning(BeregningsgrunnlagDto nyttGrunnlag) {
        nyttGrunnlag.getBeregningsgrunnlagPerioder().stream()
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .forEach(a -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(a).medBesteberegningPrÅr(null));
    }

    private static boolean harAlleredeBesteberegnet(BeregningsgrunnlagDto gammeltGrunnlag) {
        return gammeltGrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream()).anyMatch(a -> a.getBesteberegningPrÅr() != null);
    }

    private static void oppdaterBeregningForAndelerIBesteberegnetGrunnlag(BeregningsgrunnlagDto nyttGrunnlag, BesteberegningOutput output) {
        List<BesteberegnetAndel> andelListe = output.getBesteberegnetGrunnlag().getBesteberegnetAndelList();
        nyttGrunnlag.getBeregningsgrunnlagPerioder()
                .forEach(p -> andelListe
                        .forEach(a -> oppdaterAndelerMedBesteberegnetInntekt(p, a)));
    }

    private static void settBesteberegningTilNullForAndreAndeler(BeregningsgrunnlagDto nyttGrunnlag) {
        nyttGrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream()).forEach(andel -> {
            if (andel.getBesteberegningPrÅr() == null) {
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel).medBesteberegningPrÅr(BigDecimal.ZERO);
            }
        });
    }

    private static void oppdaterAndelerMedBesteberegnetInntekt(BeregningsgrunnlagPeriodeDto periode, BesteberegnetAndel a) {
        AktivitetNøkkel aktivitetNøkkel = a.getAktivitetNøkkel();
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = finnMatchendeAndelIPeriode(periode, aktivitetNøkkel);
        if (matchendeAndel.isPresent()) {
            oppdaterBesteberegningForAndel(a, matchendeAndel.get());
        } else {
            leggPåDagpenger(periode, a);
        }
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnMatchendeAndelIPeriode(BeregningsgrunnlagPeriodeDto periode, AktivitetNøkkel aktivitetNøkkel) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = Optional.empty();
        if (aktivitetNøkkel.getType().equals(Aktivitet.ARBEIDSTAKERINNTEKT)) {
            matchendeAndel = finnArbeidstakerAndel(periode, aktivitetNøkkel);
        } else if (aktivitetNøkkel.getType().equals(Aktivitet.FRILANSINNTEKT)) {
            matchendeAndel = finnFrilansAndel(periode);
        } else if (aktivitetNøkkel.getType().equals(Aktivitet.NÆRINGSINNTEKT)) {
            matchendeAndel = finnNæringAndel(periode);
        }
        return matchendeAndel;
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnArbeidstakerAndel(BeregningsgrunnlagPeriodeDto periode, AktivitetNøkkel aktivitetNøkkel) {
        String identifikator = aktivitetNøkkel.getOrgnr() != null ? aktivitetNøkkel.getOrgnr() : aktivitetNøkkel.getAktørId();
        List<BeregningsgrunnlagPrStatusOgAndelDto> matchendeArbeidsforholdAndeler = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(bgAndel -> bgAndel.getArbeidsgiver().isPresent())
                .filter(bgAndel -> bgAndel.getArbeidsgiver().get().getIdentifikator().equals(identifikator))
                .filter(bgAndel -> bgAndel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().gjelderFor(InternArbeidsforholdRefDto.ref(aktivitetNøkkel.getArbeidsforholdId())))
                .collect(Collectors.toList());
        if (matchendeArbeidsforholdAndeler.size() > 1) {
            throw new IllegalStateException("Fant flere andeler i grunnlag som matcher arbeidsforhold fra a-inntekt");
        }
        return matchendeArbeidsforholdAndeler.size() == 0 ? Optional.empty() : Optional.of(matchendeArbeidsforholdAndeler.get(0));
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnFrilansAndel(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream().filter(bgAndel -> bgAndel.getAktivitetStatus().erFrilanser())
                .findFirst();
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnNæringAndel(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream().filter(bgAndel -> bgAndel.getAktivitetStatus().erSelvstendigNæringsdrivende())
                .findFirst();
    }

    private static void oppdaterBesteberegningForAndel(BesteberegnetAndel besteberegnetAndel, BeregningsgrunnlagPrStatusOgAndelDto matchendeAndel) {
        BigDecimal besteberegnet = matchendeAndel.getBesteberegningPrÅr();
        BigDecimal beregnet = besteberegnet == null ? besteberegnetAndel.getBesteberegnetPrÅr() : besteberegnet.add(besteberegnetAndel.getBesteberegnetPrÅr());
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(matchendeAndel)
                .medBesteberegningPrÅr(beregnet);
    }

    private static void leggPåDagpenger(BeregningsgrunnlagPeriodeDto periode, BesteberegnetAndel a) {
        var dagpengeAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER))
                .findFirst();
        if (dagpengeAndel.isPresent()) {
            oppdaterBesteberegningForAndel(a, dagpengeAndel.get());
        } else {
            BeregningsgrunnlagPrStatusOgAndelDto.ny().medKilde(AndelKilde.PROSESS_BESTEBEREGNING)
                    .medBesteberegningPrÅr(a.getBesteberegnetPrÅr())
                    .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);
        }
    }


}
