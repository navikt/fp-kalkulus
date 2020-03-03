package no.nav.folketrygdloven.kalkulus.rest.testutils;

import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Fagsystem;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittAnnenAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseStørrelseDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektsKilde;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektspostType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.RelatertYtelseTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.VirksomhetType;


public class BeregningIAYTestUtil {


    /**
     * Lager oppgitt opptjening for frilans.
     *
     * @param erNyOppstartet spesifiserer om frilans er nyoppstartet
     */
    public static OppgittOpptjeningDtoBuilder leggTilOppgittOpptjeningForFL(boolean erNyOppstartet, LocalDate fom) {
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        OppgittFrilansDto frilans = new OppgittFrilansDto();
        frilans.setErNyoppstartet(erNyOppstartet);
        OppgittAnnenAktivitetDto annenAktivitet = new OppgittAnnenAktivitetDto(Intervall.fraOgMed(fom), ArbeidType.FRILANSER);
        oppgittOpptjeningBuilder.leggTilAnnenAktivitet(annenAktivitet);
        oppgittOpptjeningBuilder.leggTilFrilansOpplysninger(frilans);
        return oppgittOpptjeningBuilder;
    }

    public static void byggInntektForBehandling(AktørId aktørId,
                                                LocalDate skjæringstidspunktOpptjening,
                                                InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, List<BigDecimal> inntektPrMnd,
                                                boolean virksomhetPåInntekt, Arbeidsgiver arbeidsgiver) {

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        InntektDtoBuilder inntektBeregningBuilder = aktørInntekt
            .getInntektBuilder(InntektsKilde.INNTEKT_BEREGNING, OpptjeningsnøkkelDto.forArbeidsforholdIdMedArbeidgiver(null, arbeidsgiver));

        // Lager et år (12 mnd) med inntekt for beregning
        byggInntekt(inntektBeregningBuilder, skjæringstidspunktOpptjening, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        aktørInntekt.leggTilInntekt(inntektBeregningBuilder);

        InntektDtoBuilder inntektSammenligningBuilder = aktørInntekt
            .getInntektBuilder(InntektsKilde.INNTEKT_SAMMENLIGNING, OpptjeningsnøkkelDto.forArbeidsforholdIdMedArbeidgiver(null, arbeidsgiver));

        // Lager et år (12 mnd) med inntekt for sammenligningsgrunnlag
        byggInntekt(inntektSammenligningBuilder, skjæringstidspunktOpptjening, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        aktørInntekt.leggTilInntekt(inntektSammenligningBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
    }

    private static void byggInntekt(InntektDtoBuilder builder, LocalDate skjæringstidspunktOpptjening, List<BigDecimal> inntektPrMnd, boolean virksomhetPåInntekt,
                                    Arbeidsgiver arbeidsgiver) {
        if (virksomhetPåInntekt) {
            for (int i = 0; i <= 12; i++) {
                BigDecimal inntekt = getInntekt(inntektPrMnd, i);
                builder
                    .leggTilInntektspost(
                        lagInntektspost(skjæringstidspunktOpptjening.minusMonths(i + 1L).plusDays(1), skjæringstidspunktOpptjening.minusMonths(i), inntekt))
                    .medArbeidsgiver(arbeidsgiver);
            }
        } else {
            for (int i = 0; i <= 12; i++) {
                BigDecimal inntekt = getInntekt(inntektPrMnd, i);
                builder.leggTilInntektspost(
                    lagInntektspost(skjæringstidspunktOpptjening.minusMonths(i + 1L).plusDays(1), skjæringstidspunktOpptjening.minusMonths(i), inntekt));
            }
        }
    }

    private static BigDecimal getInntekt(List<BigDecimal> inntektPrMnd, int i) {
        BigDecimal inntekt;
        if (inntektPrMnd.size() >= i + 1) {
            inntekt = inntektPrMnd.get(i);
        } else {
            inntekt = inntektPrMnd.get(inntektPrMnd.size() - 1);
        }
        return inntekt;
    }

    private static InntektspostDtoBuilder lagInntektspost(LocalDate fom, LocalDate tom, BigDecimal lønn) {
        return InntektspostDtoBuilder.ny()
            .medBeløp(lønn)
            .medPeriode(fom, tom)
            .medInntektspostType(InntektspostType.LØNN);
    }
}
