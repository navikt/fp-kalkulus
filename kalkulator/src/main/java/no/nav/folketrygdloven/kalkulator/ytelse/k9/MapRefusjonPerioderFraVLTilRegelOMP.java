package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall.GODKJENT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;
import no.nav.folketrygdloven.kalkulator.felles.frist.ArbeidsgiverRefusjonskravTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.frist.KravOgUtfall;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class MapRefusjonPerioderFraVLTilRegelOMP extends MapRefusjonPerioderFraVLTilRegelK9 {


    public MapRefusjonPerioderFraVLTilRegelOMP() {
        super();
    }

    @Override
    protected Map<Arbeidsgiver, LocalDateTimeline<Utfall>> mapRefusjonVurderingUtfallPrArbeidsgiver(BeregningsgrunnlagInput input) {

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = input.getIayGrunnlag();
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = input.getBeregningsgrunnlagGrunnlag().getGjeldendeAktiviteter();
        Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        var fristvurdertTidslinjePrArbeidsgiver = ArbeidsgiverRefusjonskravTjeneste.lagFristTidslinjePrArbeidsgiver(
                filter.getYrkesaktiviteterForBeregning(),
                input.getKravPrArbeidsgiver(),
                gjeldendeAktiviteter,
                input.getSkjæringstidspunktForBeregning(),
                refusjonOverstyringer,
                FagsakYtelseType.OMSORGSPENGER);
        return fristvurdertTidslinjePrArbeidsgiver.entrySet().stream()
                .map(e -> new HashMap.SimpleEntry<>(mapArbeidsgiver(e.getKey()), godkjennAlle(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver.getErVirksomhet() ?
                no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver.medOrgnr(arbeidsgiver.getIdentifikator()) :
                no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver.medAktørId(arbeidsgiver.getIdentifikator());
    }


    private LocalDateTimeline<Utfall> godkjennAlle(LocalDateTimeline<KravOgUtfall> kravOgUtfallTidslinje) {
        List<LocalDateSegment<Utfall>> utfallSegmenter = kravOgUtfallTidslinje.stream().map(s -> new LocalDateSegment<>(
                        s.getLocalDateInterval(),
                        GODKJENT))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(utfallSegmenter).compress();
    }


}
