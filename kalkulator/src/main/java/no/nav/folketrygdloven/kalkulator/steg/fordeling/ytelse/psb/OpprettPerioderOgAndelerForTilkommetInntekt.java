package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.felles.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

class OpprettPerioderOgAndelerForTilkommetInntekt {

    private final BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();

    BeregningsgrunnlagDto opprettPerioderOgAndeler(BeregningsgrunnlagDto beregningsgrunnlag, List<AktivitetDto> tilkomneAktiviteter) {
        BeregningsgrunnlagDto nyttBg = new BeregningsgrunnlagDto(beregningsgrunnlag);
        for (AktivitetDto aktivitetDto : tilkomneAktiviteter) {
            var yrkesaktivitetDto = aktivitetDto.getYrkesaktivitetDto();
            Optional<LocalDate> startDatoAktivitet = finnStartdato(yrkesaktivitetDto, beregningsgrunnlag.getSkjæringstidspunkt());
            if (startDatoAktivitet.isPresent()) {
                nyttBg = SplittBGPerioder.splitt(beregningsgrunnlag, PeriodeÅrsak.TILKOMMET_INNTEKT, startDatoAktivitet.get());
                var skjæringstidspunkt = nyttBg.getSkjæringstidspunkt();
                nyttBg.getBeregningsgrunnlagPerioder().stream()
                        .filter(p -> !p.getBeregningsgrunnlagPeriodeFom().isBefore(startDatoAktivitet.get()))
                        .forEach(p -> leggTilNyAndel(
                                p,
                                aktivitetDto,
                                skjæringstidspunkt
                        ));
            }
        }
        return nyttBg;
    }

    private Optional<LocalDate> finnStartdato(YrkesaktivitetDto yrkesaktivitetDto, LocalDate skjæringstidspunkt) {
        return yrkesaktivitetDto.getAlleAktivitetsAvtaler().stream()
                .filter(a -> a.getPeriode().getFomDato().isAfter(skjæringstidspunkt))
                .min(Comparator.comparing(a -> a.getPeriode().getFomDato()))
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(Intervall::getFomDato);
    }

    private void leggTilNyAndel(BeregningsgrunnlagPeriodeDto p, AktivitetDto aktivitetDto, LocalDate skjæringstidspunkt) {
        YrkesaktivitetDto yrkesaktivitetDto = aktivitetDto.getYrkesaktivitetDto();

        var periode = FinnAnsettelsesPeriode.finnMinMaksPeriode(
                yrkesaktivitetDto.getAlleAktivitetsAvtaler(),
                skjæringstidspunkt);

        if(periode.isEmpty()) {
            throw new IllegalArgumentException("Fant ingen arbeidsperiode etter skjæringstidspunktet for aktivitet: "
                    + yrkesaktivitetDto.getArbeidsgiver());
        }

        if (yrkesaktivitetDto.getArbeidType().equals(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)) {
            Intervall beregningsperiode = beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(p.getBeregningsgrunnlagPeriodeFom());

            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                            .medArbeidsperiodeFom(periode.get().getFom())
                            .medArbeidsperiodeTom(periode.get().getTom())
                            .medArbeidsgiver(yrkesaktivitetDto.getArbeidsgiver()))
                    .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                    .medBeregningsperiode(beregningsperiode.getFomDato(), beregningsperiode.getTomDato())
                    .medKilde(AndelKilde.PROSESS_PERIODISERING_TILKOMMET_INNTEKT)
                    .build(p);
        } else if (yrkesaktivitetDto.getArbeidType().equals(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER)) {
            Intervall beregningsperiode = beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(p.getBeregningsgrunnlagPeriodeFom());
            if (p.getBeregningsgrunnlagPrStatusOgAndelList().stream().noneMatch(a -> a.getAktivitetStatus().erFrilanser())) {
                BeregningsgrunnlagPrStatusOgAndelDto.ny()
                        .medAktivitetStatus(AktivitetStatus.FRILANSER)
                        .medInntektskategori(Inntektskategori.FRILANSER)
                        .medBeregningsperiode(beregningsperiode.getFomDato(), beregningsperiode.getTomDato())
                        .medKilde(AndelKilde.PROSESS_PERIODISERING_TILKOMMET_INNTEKT)
                        .build(p);
            }
        }
    }
}
