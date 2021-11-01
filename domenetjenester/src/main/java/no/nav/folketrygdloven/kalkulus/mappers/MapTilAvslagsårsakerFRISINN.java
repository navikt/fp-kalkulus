package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_BEGYNNELSE;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_LØPENDE_INNTEKT;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.INGEN_FRILANS_I_PERIODE_UTEN_YTELSE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.EffektivÅrsinntektTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak;

public class MapTilAvslagsårsakerFRISINN {

    public static Optional<Avslagsårsak> map(BeregningsgrunnlagPrStatusOgAndel andel,
                                             List<BeregningsgrunnlagPrStatusOgAndel> andelerISammePeriode,
                                             FrisinnGrunnlag frisinnGrunnlag,
                                             OppgittOpptjeningDto oppgittOpptjening,
                                             BigDecimal grunnbeløp) {
        if (andel.getRedusertPrÅr() == null || andel.getRedusertPrÅr().compareTo(Beløp.ZERO) != 0) {
            return Optional.empty();
        }
        LocalDate fomDato = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPeriodeFom();
        if (andel.getAktivitetStatus().erFrilanser() && frisinnGrunnlag.getSøkerYtelseForFrilans(fomDato)) {
            return finnAvslagsårsakForFrilans(andel, andelerISammePeriode, oppgittOpptjening, grunnbeløp);
        }
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende() && frisinnGrunnlag.getSøkerYtelseForNæring(fomDato)) {
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
        var sisteSøknadsdato = finnPeriodeinntekterFrilans(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getTomDato)
                .max(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        if (førsteSøknadsdato.isAfter(periode.getFomDato()) || sisteSøknadsdato.isBefore(periode.getFomDato())) {
            return Optional.empty();
        }
        if (andel.getBeregnetPrÅr().compareTo(Beløp.ZERO) == 0) {
            return Optional.of(INGEN_FRILANS_I_PERIODE_UTEN_YTELSE);
        }
        if (andel.getAvkortetPrÅr().compareTo(Beløp.ZERO) == 0) {
            BigDecimal antallGØvreGrenseverdi = KonfigTjeneste.forYtelse(FagsakYtelseType.FRISINN).getAntallGØvreGrenseverdi();
            Beløp grunnlagFraArbeid = finnGrunnlagFraArbeid(andelerISammePeriode);
            BigDecimal seksG = grunnbeløp.multiply(antallGØvreGrenseverdi);
            if (grunnlagFraArbeid.compareTo(new Beløp(seksG)) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            return Optional.of(AVKORTET_GRUNNET_LØPENDE_INNTEKT);
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
        var sisteSøknadsdato = finnPeriodeinntekterNæring(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getTomDato)
                .filter(d -> !d.isBefore(bgPeriode.getBeregningsgrunnlag().getSkjæringstidspunkt()))
                .max(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        if (førsteSøknadsdato.isAfter(periode.getFomDato()) || sisteSøknadsdato.isBefore(periode.getFomDato())) {
            return Optional.empty();
        }
        if (andel.getBeregnetPrÅr().compareTo(Beløp.ZERO) == 0) {
            return Optional.empty();
        }
        if (andel.getAvkortetPrÅr().compareTo(Beløp.ZERO) == 0) {
            BigDecimal antallGØvreGrenseverdi = KonfigTjeneste.forYtelse(FagsakYtelseType.FRISINN).getAntallGØvreGrenseverdi();
            Beløp grunnlagFraArbeid = finnGrunnlagFraArbeid(andelerISammePeriode);

            Beløp seksG = new Beløp(grunnbeløp.multiply(antallGØvreGrenseverdi));
            if (grunnlagFraArbeid.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            Beløp løpendeInntektFrilans = finnLøpendeFrilansInntekt(andel, oppgittOpptjening);
            Beløp grunnlagMedLøpendeFrilans = grunnlagFraArbeid.adder(løpendeInntektFrilans);
            if (!frisinnGrunnlag.getSøkerYtelseForFrilans(bgPeriode.getBeregningsgrunnlagPeriodeFom()) && grunnlagMedLøpendeFrilans.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            Beløp grunnlagFraFrilans = finnKompensasjonsgrunnlagFrilans(andelerISammePeriode);
            Beløp grunnlagMedKompensertFrilans = grunnlagFraArbeid.adder(grunnlagFraFrilans);
            if (frisinnGrunnlag.getSøkerYtelseForFrilans(bgPeriode.getBeregningsgrunnlagPeriodeFom()) && grunnlagMedKompensertFrilans.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            return Optional.of(AVKORTET_GRUNNET_LØPENDE_INNTEKT);
        }
        return Optional.empty();
    }

    private static Beløp finnKompensasjonsgrunnlagFrilans(List<BeregningsgrunnlagPrStatusOgAndel> andelerISammePeriode) {
        return andelerISammePeriode.stream()
                .filter(a -> a.getAktivitetStatus().erFrilanser())
                .map(BeregningsgrunnlagPrStatusOgAndel::getBeregnetPrÅr)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private static Beløp finnGrunnlagFraArbeid(List<BeregningsgrunnlagPrStatusOgAndel> andelerISammePeriode) {
        return andelerISammePeriode.stream()
                .filter(a -> a.getAktivitetStatus().erArbeidstaker())
                .map(BeregningsgrunnlagPrStatusOgAndel::getBeregnetPrÅr)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private static Beløp finnLøpendeFrilansInntekt(BeregningsgrunnlagPrStatusOgAndel andel,
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


    private static Beløp finnInntektIPeriode(List<OppgittPeriodeInntekt> periodeInntekter, IntervallEntitet periode) {
        return periodeInntekter.stream()
                .filter(i -> i.getPeriode().getFomDato().equals(periode.getFomDato()))
                .map(EffektivÅrsinntektTjenesteFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .map(Beløp::new)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }
}
