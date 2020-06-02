package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_BEGYNNELSE;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_LØPENDE_INNTEKT;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.FOR_LAVT_BG;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.INGEN_FRILANS_I_PERIODE_UTEN_YTELSE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.EffektivÅrsinntektTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak;

public class MapTilAvslagsårsakerFRISINN {

    public static Optional<Avslagsårsak> map(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                             List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode,
                                             FrisinnGrunnlag frisinnGrunnlag,
                                             OppgittOpptjeningDto oppgittOpptjening,
                                             BigDecimal grunnbeløp,
                                             LocalDate skjæringstidspunkt) {
        LocalDate fomDato = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPeriodeFom();
        if (andel.getAktivitetStatus().equals(AktivitetStatus.FRILANSER) && frisinnGrunnlag.getSøkerYtelseForFrilans(fomDato)) {
            return finnAvslagsårsakForFrilans(andel, andelerISammePeriode, oppgittOpptjening, grunnbeløp);
        }
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende() && frisinnGrunnlag.getSøkerYtelseForNæring(fomDato)) {
            return finnAvslagsårsakForNæring(andel, andelerISammePeriode, oppgittOpptjening, frisinnGrunnlag, grunnbeløp, skjæringstidspunkt);
        }
        return Optional.empty();
    }

    private static Optional<Avslagsårsak> finnAvslagsårsakForFrilans(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                     List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode,
                                                                     OppgittOpptjeningDto oppgittOpptjening,
                                                                     BigDecimal grunnbeløp) {
        LocalDate fomDato = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPeriodeFom();
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
        if (førsteSøknadsdato.isAfter(fomDato) || sisteSøknadsdato.isBefore(fomDato)) {
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
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            return Optional.of(AVKORTET_GRUNNET_LØPENDE_INNTEKT);
        }
        return Optional.empty();
    }

    private static Optional<Avslagsårsak> finnAvslagsårsakForNæring(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                    List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode,
                                                                    OppgittOpptjeningDto oppgittOpptjening,
                                                                    FrisinnGrunnlag frisinnGrunnlag,
                                                                    BigDecimal grunnbeløp,
                                                                    LocalDate skjæringstidspunkt) {
        BeregningsgrunnlagPeriodeDto bgPeriode = andel.getBeregningsgrunnlagPeriode();
        Intervall periode = bgPeriode.getPeriode();
        var førsteSøknadsdato = finnPeriodeinntekterNæring(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getFomDato)
                .filter(d -> !d.isBefore(skjæringstidspunkt))
                .min(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        var sisteSøknadsdato = finnPeriodeinntekterNæring(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getTomDato)
                .filter(d -> !d.isBefore(skjæringstidspunkt))
                .max(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        if (førsteSøknadsdato.isAfter(periode.getFomDato()) || sisteSøknadsdato.isBefore(periode.getFomDato())) {
            return Optional.empty();
        }
        if (andel.getBeregnetPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Ugyldig tilstand for frisinn");
        }
        if (andel.getAvkortetPrÅr() != null && andel.getAvkortetPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal antallGØvreGrenseverdi = KonfigTjeneste.forYtelse(FagsakYtelseType.FRISINN).getAntallGØvreGrenseverdi();
            BigDecimal grunnlagFraArbeid = finnGrunnlagFraArbeid(andelerISammePeriode);

            BigDecimal seksG = grunnbeløp.multiply(antallGØvreGrenseverdi);
            if (grunnlagFraArbeid.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            BigDecimal løpendeInntektFrilans = finnLøpendeFrilansInntekt(andel, oppgittOpptjening);
            BigDecimal grunnlagMedLøpendeFrilans = grunnlagFraArbeid.add(løpendeInntektFrilans);
            if (!frisinnGrunnlag.getSøkerYtelseForFrilans(bgPeriode.getBeregningsgrunnlagPeriodeFom()) && grunnlagMedLøpendeFrilans.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            BigDecimal grunnlagFraFrilans = finnKompensasjonsgrunnlagFrilans(andelerISammePeriode);
            BigDecimal grunnlagMedKompensertFrilans = grunnlagFraArbeid.add(grunnlagFraFrilans);
            if (frisinnGrunnlag.getSøkerYtelseForFrilans(bgPeriode.getBeregningsgrunnlagPeriodeFom()) && grunnlagMedKompensertFrilans.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            return Optional.of(AVKORTET_GRUNNET_LØPENDE_INNTEKT);
        }
        return Optional.empty();
    }

    private static BigDecimal finnKompensasjonsgrunnlagFrilans(List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode) {
        return andelerISammePeriode.stream()
                .filter(a -> a.getAktivitetStatus().erFrilanser())
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal finnGrunnlagFraArbeid(List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode) {
        return andelerISammePeriode.stream()
                .filter(a -> a.getAktivitetStatus().erArbeidstaker())
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal finnLøpendeFrilansInntekt(BeregningsgrunnlagPrStatusOgAndelDto andel,
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


    private static BigDecimal finnInntektIPeriode(List<OppgittPeriodeInntekt> periodeInntekter, Intervall periode) {
        return periodeInntekter.stream()
                .filter(i -> i.getPeriode().getFomDato().equals(periode.getFomDato()))
                .map(EffektivÅrsinntektTjenesteFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public static Optional<Avslagsårsak> finnForPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                        FrisinnGrunnlag frisinnGrunnlag,
                                                        Optional<OppgittOpptjeningDto> oppgittOpptjening,
                                                        BigDecimal gbeløp, LocalDate skjæringstidspunkt) {
        if (beregningsgrunnlagPeriode.getDagsats() != null && beregningsgrunnlagPeriode.getDagsats() > 0) {
            return Optional.empty();
        }

        LocalDate fom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom();
        if (!frisinnGrunnlag.getSøkerYtelseForNæring(fom) && !frisinnGrunnlag.getSøkerYtelseForFrilans(fom)) {
            return Optional.empty();
        }

        if (oppgittOpptjening.isPresent()) {

            List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
            Set<Avslagsårsak> avslagsårsaker = andeler.stream()
                    .flatMap(a -> map(a, andeler, frisinnGrunnlag, oppgittOpptjening.get(), gbeløp, skjæringstidspunkt).stream())
                    .collect(Collectors.toSet());
            if (harForLavtBeregningsgrunnlag(frisinnGrunnlag, gbeløp, fom, andeler)){
                if (avslagsårsaker.contains(INGEN_FRILANS_I_PERIODE_UTEN_YTELSE)) {
                    return Optional.of(INGEN_FRILANS_I_PERIODE_UTEN_YTELSE);
                }
                return Optional.of(FOR_LAVT_BG);
            }
            if (avslagsårsaker.contains(AVKORTET_GRUNNET_ANNEN_INNTEKT)) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            if (avslagsårsaker.contains(AVKORTET_GRUNNET_LØPENDE_INNTEKT)) {
                return Optional.of(AVKORTET_GRUNNET_LØPENDE_INNTEKT);
            }
            return Optional.of(FOR_LAVT_BG);
        }

        return Optional.empty();

    }

    private static boolean harForLavtBeregningsgrunnlag(FrisinnGrunnlag frisinnGrunnlag, BigDecimal gbeløp, LocalDate fom, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        BigDecimal grunnlagFraSøkteStatuser = BigDecimal.ZERO;
        if (frisinnGrunnlag.getSøkerYtelseForFrilans(fom)) {
            BigDecimal frilansBrutto = andeler.stream().filter(a -> a.getAktivitetStatus().erFrilanser())
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr).findFirst().orElse(BigDecimal.ZERO);
            grunnlagFraSøkteStatuser = grunnlagFraSøkteStatuser.add(frilansBrutto);
        }
        if (frisinnGrunnlag.getSøkerYtelseForNæring(fom)) {
            BigDecimal næringBrutto = andeler.stream().filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr).findFirst().orElse(BigDecimal.ZERO);
            grunnlagFraSøkteStatuser = grunnlagFraSøkteStatuser.add(næringBrutto);
        }

        BigDecimal antallGForOppfyltVilkår = KonfigTjeneste.forYtelse(FagsakYtelseType.FRISINN).getAntallGForOppfyltVilkår();
        if (grunnlagFraSøkteStatuser.compareTo(gbeløp.multiply(antallGForOppfyltVilkår)) < 0) {
            return true;
        }
        return false;
    }
}
