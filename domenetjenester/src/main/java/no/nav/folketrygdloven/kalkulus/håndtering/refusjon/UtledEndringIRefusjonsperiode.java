package no.nav.folketrygdloven.kalkulus.håndtering.refusjon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.DatoEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonoverstyringEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonoverstyringPeriodeEndring;

public final class UtledEndringIRefusjonsperiode {

    private UtledEndringIRefusjonsperiode() {
        // skjul
    }

    protected static RefusjonoverstyringEndring utledRefusjonoverstyringEndring(BeregningRefusjonOverstyringerDto refusjonOverstyringaggregat,
                                                                                BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                Optional<BeregningRefusjonOverstyringerDto> forrigerefusjonOverstyringaggregat,
                                                                                Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlag) {
        List<BeregningRefusjonOverstyringDto> refusjonendringerMedOverstyrtPeriode = refusjonOverstyringaggregat.getRefusjonOverstyringer()
                .stream()
                .filter(ro -> !ro.getRefusjonPerioder().isEmpty())
                .collect(Collectors.toList());
        List<RefusjonoverstyringPeriodeEndring> endringer = new ArrayList<>();
        refusjonendringerMedOverstyrtPeriode.forEach(refusjonOverstyringHosAG -> {
            List<BeregningRefusjonPeriodeDto> nyeRefusjonperioderHosAG = refusjonOverstyringHosAG.getRefusjonPerioder();
            List<BeregningRefusjonPeriodeDto> forrigeRefusjonsperioderHosAG = finnForrigePerioderHosAG(forrigerefusjonOverstyringaggregat,
                    refusjonOverstyringHosAG.getArbeidsgiver());
            List<RefusjonoverstyringPeriodeEndring> endringerForAG = utledEndringerIPerioder(refusjonOverstyringHosAG.getArbeidsgiver(),
                    nyeRefusjonperioderHosAG,
                    beregningsgrunnlag,
                    forrigeRefusjonsperioderHosAG,
                    forrigeBeregningsgrunnlag);
            endringer.addAll(endringerForAG);
        });
        return new RefusjonoverstyringEndring(endringer);
    }

    private static List<RefusjonoverstyringPeriodeEndring> utledEndringerIPerioder(Arbeidsgiver arbeidsgiver,
                                                                                   List<BeregningRefusjonPeriodeDto> nyeRefusjonperioderHosAG,
                                                                                   BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                   List<BeregningRefusjonPeriodeDto> forrigeRefusjonsperioderHosAG,
                                                                                   Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlag) {
        List<RefusjonoverstyringPeriodeEndring> endringer = new ArrayList<>();
        nyeRefusjonperioderHosAG.forEach(periode -> {
            Optional<BeregningRefusjonPeriodeDto> matchetArbeidsforhold = forrigeRefusjonsperioderHosAG.stream()
                    .filter(p -> matcherReferanse(periode, p))
                    .findFirst();
            var saksbehandletRefusjon = finnSaksbehandletRefusjonFørDato(arbeidsgiver, beregningsgrunnlag, periode);
            var forrigeSaksbehandletRefusjon = forrigeBeregningsgrunnlag.flatMap(bg -> matchetArbeidsforhold.flatMap(p -> finnSaksbehandletRefusjonFørDato(arbeidsgiver, bg, p)));

            DatoEndring datoEndring = new DatoEndring(matchetArbeidsforhold.map(BeregningRefusjonPeriodeDto::getStartdatoRefusjon).orElse(null), periode.getStartdatoRefusjon());
            var refusjonEndring = saksbehandletRefusjon.map(Beløp::fra).map(ModellTyperMapper::beløpTilDto).map(ref -> new RefusjonEndring(forrigeSaksbehandletRefusjon.map(Beløp::fra).map(ModellTyperMapper::beløpTilDto).orElse(null), ref));
            if (arbeidsgiver.getErVirksomhet()) {
                endringer.add(new RefusjonoverstyringPeriodeEndring(new Organisasjon(arbeidsgiver.getIdentifikator()), periode.getArbeidsforholdRef().getUUIDReferanse(), datoEndring, refusjonEndring.orElse(null)));
            } else {
                endringer.add(new RefusjonoverstyringPeriodeEndring(new AktørIdPersonident(arbeidsgiver.getIdentifikator()), periode.getArbeidsforholdRef().getUUIDReferanse(), datoEndring, refusjonEndring.orElse(null)));
            }
        });
        return endringer;
    }

    private static Optional<BigDecimal> finnSaksbehandletRefusjonFørDato(Arbeidsgiver arbeidsgiver, BeregningsgrunnlagDto beregningsgrunnlag, BeregningRefusjonPeriodeDto refusjonPeriode) {
        var matchetPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getPeriode().inkluderer(refusjonPeriode.getStartdatoRefusjon().minusDays(1)))
                .findFirst();
        var matchendeAndel = matchetPeriode.stream()
                .flatMap(andel -> andel.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(andel -> andel.getArbeidsgiver().isPresent() && andel.getArbeidsgiver().get().equals(arbeidsgiver) &&
                        Objects.equals(andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()), refusjonPeriode.getArbeidsforholdRef()))
                .findFirst();
        return matchendeAndel
                .flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                .flatMap(BGAndelArbeidsforholdDto::getRefusjon)
                .map(Refusjon::getSaksbehandletRefusjonPrÅr)
                .map(Beløp::safeVerdi);
    }

    private static boolean matcherReferanse(BeregningRefusjonPeriodeDto periode, BeregningRefusjonPeriodeDto p) {
        String ref1 = p.getArbeidsforholdRef().getReferanse();
        String ref2 = periode.getArbeidsforholdRef().getReferanse();
        return Objects.equals(ref1, ref2);
    }

    private static List<BeregningRefusjonPeriodeDto> finnForrigePerioderHosAG(Optional<BeregningRefusjonOverstyringerDto> forrigerefusjonOverstyringaggregat, Arbeidsgiver ag) {
        List<BeregningRefusjonOverstyringDto> forrigeRefusjonOverstyringer = forrigerefusjonOverstyringaggregat
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());
        return forrigeRefusjonOverstyringer
                .stream()
                .filter(refOverstyring -> refOverstyring.getArbeidsgiver().equals(ag))
                .findFirst()
                .map(BeregningRefusjonOverstyringDto::getRefusjonPerioder)
                .orElse(Collections.emptyList());
    }
}
