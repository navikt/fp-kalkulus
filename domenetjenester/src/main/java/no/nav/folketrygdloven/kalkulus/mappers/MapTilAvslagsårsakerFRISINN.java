package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_BEGYNNELSE;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_ARBEIDSINNTEKT;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_LØPENDE_FRILANS;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_LØPENDE_NÆRING;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_NÆRING_GRUNNET_KOMPENSERT_FRILANS;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.INGEN_FRILANS_I_PERIODE_UTEN_YTELSE;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak;

public class MapTilAvslagsårsakerFRISINN {

    public static Optional<Avslagsårsak> map(BeregningsgrunnlagPrStatusOgAndel andel,
                                             List<BeregningsgrunnlagPrStatusOgAndel> andelerISammePeriode,
                                             FrisinnGrunnlag frisinnGrunnlag,
                                             OppgittOpptjeningDto oppgittOpptjening,
                                             BigDecimal grunnbeløp) {
        if (andel.getRedusertPrÅr() == null || andel.getRedusertPrÅr().compareTo(BigDecimal.ZERO) != 0) {
            return Optional.empty();
        }
        if (andel.getAktivitetStatus().erFrilanser() && frisinnGrunnlag.getSøkerYtelseForFrilans()) {
            return finnAvslagsårsakForFrilans(andel, andelerISammePeriode, oppgittOpptjening, grunnbeløp);
        }
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende() && frisinnGrunnlag.getSøkerYtelseForNæring()) {
            return finnAvslagsårsakForNæring(andel, andelerISammePeriode, oppgittOpptjening, frisinnGrunnlag, grunnbeløp);
        }
        return Optional.empty();
    }

    private static Optional<Avslagsårsak> finnAvslagsårsakForFrilans(BeregningsgrunnlagPrStatusOgAndel andel,
                                                                     List<BeregningsgrunnlagPrStatusOgAndel> andelerISammePeriode,
                                                                     OppgittOpptjeningDto oppgittOpptjening, BigDecimal grunnbeløp) {
        IntervallEntitet periode = andel.getBeregningsgrunnlagPeriode().getPeriode();
        var førsteSøknadsdato = finnPeriodeinntekterFrilans(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        if (førsteSøknadsdato.isAfter(periode.getFomDato())) {
            return Optional.empty();
        }
        if (andel.getBeregnetPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            return Optional.of(INGEN_FRILANS_I_PERIODE_UTEN_YTELSE);
        }
        if (andel.getAvkortetPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal antallGØvreGrenseverdi = KonfigTjeneste.forYtelse(FagsakYtelseType.FRISINN).getAntallGØvreGrenseverdi();
            BigDecimal grunnlagFraArbeid = finnGrunnlagFraArbeid(andelerISammePeriode);
            BigDecimal seksG = grunnbeløp.multiply(antallGØvreGrenseverdi);
            if (grunnlagFraArbeid.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ARBEIDSINNTEKT);
            }
            return Optional.of(AVKORTET_GRUNNET_LØPENDE_FRILANS);
        }
        return Optional.empty();
    }

    private static Optional<Avslagsårsak> finnAvslagsårsakForNæring(BeregningsgrunnlagPrStatusOgAndel andel,
                                                                    List<BeregningsgrunnlagPrStatusOgAndel> andelerISammePeriode,
                                                                    OppgittOpptjeningDto oppgittOpptjening,
                                                                    FrisinnGrunnlag frisinnGrunnlag,
                                                                    BigDecimal grunnbeløp) {
        BeregningsgrunnlagPeriode bgPeriode = andel.getBeregningsgrunnlagPeriode();
        IntervallEntitet periode = bgPeriode.getPeriode();
        var førsteSøknadsdato = finnPeriodeinntekterNæring(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getFomDato)
                .filter(d -> !d.isBefore(bgPeriode.getBeregningsgrunnlag().getSkjæringstidspunkt()))
                .min(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        if (førsteSøknadsdato.isAfter(periode.getFomDato())) {
            return Optional.empty();
        }
        if (andel.getBeregnetPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Ugyldig tilstand for frisinn");
        }
        if (andel.getAvkortetPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal antallGØvreGrenseverdi = KonfigTjeneste.forYtelse(FagsakYtelseType.FRISINN).getAntallGØvreGrenseverdi();
            BigDecimal grunnlagFraArbeid = finnGrunnlagFraArbeid(andelerISammePeriode);

            BigDecimal seksG = grunnbeløp.multiply(antallGØvreGrenseverdi);
            if (grunnlagFraArbeid.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ARBEIDSINNTEKT);
            }
            BigDecimal løpendeInntektFrilans = finnLøpendeFrilansInntekt(andel, oppgittOpptjening);
            BigDecimal grunnlagMedLøpendeFrilans = grunnlagFraArbeid.add(løpendeInntektFrilans);
            if (grunnlagMedLøpendeFrilans.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_LØPENDE_FRILANS);
            }
            BigDecimal grunnlagFraFrilans = finnKompensasjonsgrunnlagFrilans(andelerISammePeriode);
            BigDecimal grunnlagMedKompensertFrilans = grunnlagFraArbeid.add(grunnlagFraFrilans);
            if (!frisinnGrunnlag.getSøkerYtelseForFrilans() && grunnlagMedKompensertFrilans.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_NÆRING_GRUNNET_KOMPENSERT_FRILANS);
            }
            return Optional.of(AVKORTET_GRUNNET_LØPENDE_NÆRING);
        }
        return Optional.empty();
    }

    private static BigDecimal finnKompensasjonsgrunnlagFrilans(List<BeregningsgrunnlagPrStatusOgAndel> andelerISammePeriode) {
        return andelerISammePeriode.stream()
                .filter(a -> a.getAktivitetStatus().erFrilanser())
                .map(BeregningsgrunnlagPrStatusOgAndel::getBeregnetPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal finnGrunnlagFraArbeid(List<BeregningsgrunnlagPrStatusOgAndel> andelerISammePeriode) {
        return andelerISammePeriode.stream()
                .filter(a -> a.getAktivitetStatus().erArbeidstaker())
                .map(BeregningsgrunnlagPrStatusOgAndel::getBeregnetPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal finnLøpendeFrilansInntekt(BeregningsgrunnlagPrStatusOgAndel andel,
                                                        OppgittOpptjeningDto oppgittOpptjening) {
        List<OppgittPeriodeInntekt> oppgittInntektFrilans = finnPeriodeinntekterFrilans(oppgittOpptjening);
        return finnInntektIPeriode(oppgittInntektFrilans, andel.getBeregningsgrunnlagPeriode().getPeriode());
    }

    private static List<OppgittPeriodeInntekt> finnPeriodeinntekterFrilans(OppgittOpptjeningDto oppgittOpptjening) {
        return oppgittOpptjening.getFrilans()
                    .stream()
                    .flatMap(oppgittFrilansDto -> oppgittFrilansDto.getOppgittFrilansInntekt().stream())
                    .map(i -> (OppgittPeriodeInntekt) i)
                    .collect(Collectors.toList());
    }

    private static List<OppgittPeriodeInntekt> finnPeriodeinntekterNæring(OppgittOpptjeningDto oppgittOpptjening) {
        return oppgittOpptjening.getEgenNæring()
                .stream()
                .map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }


    private static BigDecimal finnInntektIPeriode(List<OppgittPeriodeInntekt> periodeInntekter, IntervallEntitet periode) {
        return periodeInntekter.stream()
                .filter(i -> i.getPeriode().getFomDato().equals(periode.getFomDato()))
                .map(UtbetalingsgradMapperFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }
}
