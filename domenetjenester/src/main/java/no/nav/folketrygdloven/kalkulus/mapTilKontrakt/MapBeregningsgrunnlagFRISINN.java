package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.EffektivÅrsinntektTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.mappers.MapTilAvslagsårsakerFRISINN;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.BeregningsgrunnlagFRISINNDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.BeregningsgrunnlagPeriodeFRISINNDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.BeregningsgrunnlagPrStatusOgAndelFRISINNDto;

public class MapBeregningsgrunnlagFRISINN {
    private final static BigDecimal ANTALL_G_GRENSEVERDI;

    static {
        ANTALL_G_GRENSEVERDI = KonfigTjeneste.getAntallGØvreGrenseverdi();
    }

    private MapBeregningsgrunnlagFRISINN() {
        // SKjul konstruktør
    }

    public static BeregningsgrunnlagFRISINNDto map(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet,
                                                   Optional<OppgittOpptjeningDto> oppgittOpptjening,
                                                   FrisinnGrunnlag frisinnGrunnlag) {
        return new BeregningsgrunnlagFRISINNDto(
                beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
                mapAktivitetstatuser(beregningsgrunnlagEntitet),
                mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet, oppgittOpptjening, frisinnGrunnlag, beregningsgrunnlagEntitet.getGrunnbeløp().getVerdi())
        );
    }

    private static List<BeregningsgrunnlagPeriodeFRISINNDto> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet, Optional<OppgittOpptjeningDto> oppgittOpptjening, FrisinnGrunnlag frisinnGrunnlag, BigDecimal gbeløp) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder()
                .stream()
                .map(p -> MapBeregningsgrunnlagFRISINN.mapPeriode(p, oppgittOpptjening, frisinnGrunnlag, gbeløp)).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeFRISINNDto mapPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                                                  Optional<OppgittOpptjeningDto> oppgittOpptjening,
                                                                  FrisinnGrunnlag frisinnGrunnlag,
                                                                  BigDecimal gbeløp) {
        BigDecimal inntektstak = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
        .map(a -> MapInntektstakFRISINN.map(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList(),
                a.getAktivitetStatus(), frisinnGrunnlag, oppgittOpptjening, gbeløp))
                .reduce(BigDecimal::add)
                .orElse(ANTALL_G_GRENSEVERDI.multiply(gbeløp));
        List<BeregningsgrunnlagPrStatusOgAndelFRISINNDto> andeler = mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList(), oppgittOpptjening, frisinnGrunnlag, gbeløp);
        return new BeregningsgrunnlagPeriodeFRISINNDto(
                andeler,
                new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                mapFraBeløp(beregningsgrunnlagPeriode.getBruttoPrÅr()),
                mapFraBeløp(beregningsgrunnlagPeriode.getAvkortetPrÅr()),
                mapFraBeløp(beregningsgrunnlagPeriode.getRedusertPrÅr()),
                no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(inntektstak),
                beregningsgrunnlagPeriode.getDagsats(),
                beregningsgrunnlagPeriode.getPeriodeÅrsaker());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelFRISINNDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList, Optional<OppgittOpptjeningDto> oppgittOpptjening, FrisinnGrunnlag frisinnGrunnlag, BigDecimal gbeløp) {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
                .map(a -> MapBeregningsgrunnlagFRISINN.mapAndel(a, oppgittOpptjening, frisinnGrunnlag, gbeløp)).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelFRISINNDto mapAndel(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel,
                                                                        Optional<OppgittOpptjeningDto> oppgittOpptjening,
                                                                        FrisinnGrunnlag frisinnGrunnlag,
                                                                        BigDecimal gbeløp) {
        List<BeregningsgrunnlagPrStatusOgAndel> andelerSammePeriode = beregningsgrunnlagPrStatusOgAndel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPrStatusOgAndelList();
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andelerSammePeriode,
                beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus(), frisinnGrunnlag, oppgittOpptjening, gbeløp);

        Optional<Avslagsårsak> avslagsårsak = oppgittOpptjening.flatMap(oo ->
                MapTilAvslagsårsakerFRISINN.map(beregningsgrunnlagPrStatusOgAndel, andelerSammePeriode, frisinnGrunnlag, oo, gbeløp));
        return new BeregningsgrunnlagPrStatusOgAndelFRISINNDto(
                beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus(),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getAvkortetPrÅr()),
                finnLøpendeInntekt(beregningsgrunnlagPrStatusOgAndel, oppgittOpptjening),
                no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(inntektstak),
                beregningsgrunnlagPrStatusOgAndel.getDagsats(),
                beregningsgrunnlagPrStatusOgAndel.getInntektskategori(),
                avslagsårsak.orElse(null));
    }

    private static Beløp finnLøpendeInntekt(BeregningsgrunnlagPrStatusOgAndel andel, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        // Her må vi finne løpende inntekt basert på status og periode
        IntervallEntitet periode = andel.getBeregningsgrunnlagPeriode().getPeriode();
        if (andel.getAktivitetStatus().erFrilanser()) {
            return finnInntektIPeriode(finnOppgittInntektFL(oppgittOpptjening), periode);
        } else if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            return finnInntektIPeriode(finnOppgittInntektSN(oppgittOpptjening), periode);
        }
        return Beløp.ZERO;
    }

    private static Beløp finnInntektIPeriode(List<OppgittPeriodeInntekt> periodeInntekter, IntervallEntitet periode) {
        return periodeInntekter.stream()
                .filter(i -> i.getPeriode().getFomDato().equals(periode.getFomDato()))
                .map(EffektivÅrsinntektTjenesteFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .reduce(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp::adder)
                .map(ModellTyperMapper::beløpTilDto)
                .orElse(Beløp.ZERO);
    }

    private static List<OppgittPeriodeInntekt> finnOppgittInntektFL(Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        return oppgittOpptjening
                .filter(oo -> oo.getFrilans().isPresent())
                .flatMap(OppgittOpptjeningDto::getFrilans)
                .stream()
                .flatMap(oo -> oo.getOppgittFrilansInntekt().stream())
                .map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }

    private static List<OppgittPeriodeInntekt> finnOppgittInntektSN(Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        return oppgittOpptjening
                .stream()
                .flatMap(oo -> oo.getEgenNæring().stream())
                .map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }


    private static List<AktivitetStatus> mapAktivitetstatuser(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getAktivitetStatuser().stream().map(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus)
                .collect(Collectors.toList());
    }

    private static Beløp mapFraBeløp(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp beløp) {
        return beløp != null ? Beløp.fra(beløp.getVerdi()) : null;
    }


}
