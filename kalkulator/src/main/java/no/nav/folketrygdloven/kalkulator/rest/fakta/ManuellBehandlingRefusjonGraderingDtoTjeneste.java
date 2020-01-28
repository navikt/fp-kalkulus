package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAktivitetAggregat;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAndel;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapPeriode;

import java.time.LocalDate;
import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class ManuellBehandlingRefusjonGraderingDtoTjeneste {

    private ManuellBehandlingRefusjonGraderingDtoTjeneste() {
        // Skjul
    }

    public static boolean skalSaksbehandlerRedigereInntekt(BeregningAktivitetAggregatRestDto beregningAktivitetAggregat,
                                                    AktivitetGradering aktivitetGradering,
                                                    BeregningsgrunnlagPeriodeRestDto periode,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger, Beløp grunnbeløp, LocalDate skjæringstidspunkt) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> FordelBeregningsgrunnlagTjeneste
                .vurderManuellBehandlingForAndel(mapPeriode(periode),
                    mapAndel(andelFraSteg),
                    aktivitetGradering,
                    mapAktivitetAggregat(beregningAktivitetAggregat),
                    inntektsmeldinger,
                    grunnbeløp,
                    skjæringstidspunkt).isPresent());
    }

    public static boolean skalSaksbehandlerRedigereRefusjon(BeregningAktivitetAggregatRestDto beregningAktivitetAggregat,
                                                     AktivitetGradering aktivitetGradering,
                                                     BeregningsgrunnlagPeriodeRestDto periode,
                                                     Collection<InntektsmeldingDto> inntektsmeldinger,
                                                     Beløp grunnbeløp, LocalDate skjæringstidspunkt) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> FordelBeregningsgrunnlagTjeneste
            .vurderManuellBehandlingForAndel(mapPeriode(periode),
                mapAndel(andelFraSteg),
                aktivitetGradering,
                mapAktivitetAggregat(beregningAktivitetAggregat),
                inntektsmeldinger,
                grunnbeløp,
                skjæringstidspunkt).isPresent()
            && RefusjonDtoTjeneste.skalKunneEndreRefusjon(andelFraSteg, periode, aktivitetGradering, inntektsmeldinger, grunnbeløp));
    }
}
