package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;

class AvklaringsbehovUtlederTilkommetInntektTest {

    public static final String ARBEIDSGIVER_ORGNR = "123456789";
    public static final String ARBEIDSGIVER_ORGNR2 = "123456719";
    public static final String ARBEIDSGIVER_ORGNR3 = "123423429";

    public static final LocalDate STP = LocalDate.of(2022, 12, 8);

    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_kun_en_andel_fra_start() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()),
                lagUtbetalingsgrader(100, STP, STP.plusDays(20)));

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusMonths(10), InternArbeidsforholdRefDto.nullRef());

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));
        var tilkomneAndeler = finnTilkomneAndeler(periode, List.of(yrkesaktivitet), List.of(arbeidstakerandelFraStart), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart)), STP);

        assertThat(tilkomneAndeler.isEmpty()).isTrue();

    }

    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_en_andel_fra_start_og_direkte_overgang_uten_overlap_til_nytt_arbeid() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()),
                lagUtbetalingsgrader(100, STP, STP.plusDays(20)));

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()),
                lagUtbetalingsgrader(50, STP, STP.plusDays(20)));

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(9), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());


        var utbetalingsgradGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel));
        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));
        var tilkomneAndeler = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet), List.of(arbeidstakerandelFraStart, nyAndel), utbetalingsgradGrunnlag, STP);

        assertThat(tilkomneAndeler.isEmpty()).isTrue();
    }

    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_det_ikke_er_søkt_utbetalign_for_noen_aktiviteter() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(1), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(0, STP.plusDays(2), STP.plusDays(3)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()),
                List.of(lagPeriodeMedUtbetalingsgrad(50, STP.plusDays(1), STP.plusDays(1)),
                        lagPeriodeMedUtbetalingsgrad(50, STP.plusDays(4), STP.plusDays(20))));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(2), STP.plusDays(3));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), STP);

        assertThat(tilkommetAktivitet.size()).isEqualTo(0);
    }

    @Test
    void skal_finne_tilkommet_andel_dersom_en_andel_fra_start_med_overlapp_til_nytt_arbeid() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), STP);

        assertThat(tilkommetAktivitet.size()).isEqualTo(1);
        assertThat(tilkommetAktivitet.iterator().next().arbeidsgiver()).isEqualTo(arbeidsgiver2);
    }

    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_en_andel_fra_start_og_med_overlapp_til_nytt_arbeid_med_permisjon() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));

        YrkesaktivitetDtoBuilder.oppdatere(Optional.of(nyYrkesaktivitet))
                .leggTilPermisjon(PermisjonDtoBuilder.ny().medPeriode(periode)
                        .medProsentsats(BigDecimal.valueOf(100))
                        .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.VELFERDSPERMISJON));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), STP);

        assertThat(tilkommetAktivitet.isEmpty()).isTrue();
    }

    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_en_andel_fra_start_med_overlapp_til_nytt_arbeid_med_fullt_fravær() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP.plusDays(10), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), STP);

        assertThat(tilkommetAktivitet.isEmpty()).isTrue();
    }


    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_en_andel_fra_start_med_overlapp_til_nytt_arbeid_utenfor_aktuell_periode() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(5), STP.plusDays(9));

        var yrkesaktiviteter = List.of(yrkesaktivitet, nyYrkesaktivitet);
        var andelerFraStart = List.of(arbeidstakerandelFraStart, nyAndel);
        var utbetalingsgradGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel));
        var tilkomneAndeler = finnTilkomneAndeler(periode, yrkesaktiviteter, andelerFraStart, utbetalingsgradGrunnlag, STP);

        assertThat(tilkomneAndeler.isEmpty()).isTrue();
    }

    @Test
    void skal_finne_tilkommet_andel_dersom_en_andel_fra_start_med_overlapp_til_to_arbeidsforhold_hos_ulike_arbeidsgivere() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver3 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR3);
        var nyAndel2 = lagArbeidstakerandel(arbeidsgiver3, 3L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet2 = lagYrkesaktivitet(arbeidsgiver3, STP.plusDays(12), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP, STP.plusDays(15)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));
        var utbetalingsgradNyAndel2 = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver3, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(12), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(12), STP.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet, nyYrkesaktivitet2), List.of(arbeidstakerandelFraStart, nyAndel, nyAndel2), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel, utbetalingsgradNyAndel2)), STP);

        assertThat(tilkommetAktivitet.size()).isEqualTo(2);
        var iterator = tilkommetAktivitet.iterator();
        assertThat(iterator.next().arbeidsgiver()).isEqualTo(arbeidsgiver2);
        assertThat(iterator.next().arbeidsgiver()).isEqualTo(arbeidsgiver3);
    }

    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_en_andel_fra_start_med_overlapp_til_nytt_arbeidsforhold_i_hos_samme_arbeidsgiver() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var ref1 = InternArbeidsforholdRefDto.nyRef();
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, ref1);

        var ref2 = InternArbeidsforholdRefDto.nyRef();
        var nyAndel = lagArbeidstakerandel(arbeidsgiver, 2L, AndelKilde.PROSESS_PERIODISERING, ref2);

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), ref1);
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.plusDays(10), STP.plusDays(20), ref2);

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, ref1), lagUtbetalingsgrader(50, STP, STP.plusDays(15)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, ref2), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(12), STP.plusDays(15));

        var tilkomneAndeler = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), STP);

        assertThat(tilkomneAndeler.isEmpty()).isTrue();
    }

    @Test
    void skal_finne_to_tilkommne_andeler_dersom_en_andel_fra_start_uten_overlapp_til_to_overlappende_arbeidsforhold_hos_ulike_arbeidsgivere() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var arbeidsgiver3 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR3);
        var nyAndel2 = lagArbeidstakerandel(arbeidsgiver3, 3L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());

        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(9), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());
        var nyYrkesaktivitet2 = lagYrkesaktivitet(arbeidsgiver3, STP.plusDays(12), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP, STP.plusDays(20)));
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));
        var utbetalingsgradNyAndel2 = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver3, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(50, STP.plusDays(12), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(12), STP.plusDays(20));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet, nyYrkesaktivitet2), List.of(arbeidstakerandelFraStart, nyAndel, nyAndel2), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel, utbetalingsgradNyAndel2)), STP);

        assertThat(tilkommetAktivitet.size()).isEqualTo(2);
        var iterator = tilkommetAktivitet.iterator();
        assertThat(iterator.next().arbeidsgiver()).isEqualTo(arbeidsgiver2);
        assertThat(iterator.next().arbeidsgiver()).isEqualTo(arbeidsgiver3);

    }

    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_kun_en_frilansandel_fra_start() {
        var frilansandelFraStart = lagFrilansandel(1L, AndelKilde.PROSESS_START);
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagFrilansAktivitet(), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));
        var tilkomneAndeler = finnTilkomneAndeler(periode, List.of(), List.of(frilansandelFraStart), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart)), STP);

        assertThat(tilkomneAndeler.isEmpty()).isTrue();
    }

    @Test
    void skal_ikke_finne_tilkommet_andel_dersom_en_frilansandel_og_en_arbeidstakerandel_fra_start_med_direkte_overgang_til_nytt_arbeid() {
        var frilansandelFraStart = lagFrilansandel(1L, AndelKilde.PROSESS_START);
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagFrilansAktivitet(), lagUtbetalingsgrader(50, STP, STP.plusDays(20)));

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(9), InternArbeidsforholdRefDto.nullRef());

        var atFraStart = lagArbeidstakerandel(arbeidsgiver, 2L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradATFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()),
                lagUtbetalingsgrader(50, STP, STP.plusDays(20)));



        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 3L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()),
                lagUtbetalingsgrader(50, STP, STP.plusDays(20)));

        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());




        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));
        var tilkomneAndeler = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, nyYrkesaktivitet), List.of(frilansandelFraStart, atFraStart, nyAndel),
                new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradATFraStart, utbetalingsgradNyAndel)), STP);

        assertThat(tilkomneAndeler.isEmpty()).isTrue();
    }

    @Test
    void skal_finne_tilkommet_andel_dersom_en_frilansandel_fra_start_og_direkte_overgang_til_arbeid() {

        var frilansandelFraStart = lagFrilansandel(1L, AndelKilde.PROSESS_START);
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagFrilansAktivitet(), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()),
                lagUtbetalingsgrader(50, STP, STP.plusDays(20)));

        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());


        var utbetalingsgradGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel));
        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));
        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(nyYrkesaktivitet), List.of(frilansandelFraStart, nyAndel), utbetalingsgradGrunnlag, STP);

        assertThat(tilkommetAktivitet.size()).isEqualTo(1);
        var iterator = tilkommetAktivitet.iterator();
        assertThat(iterator.next().arbeidsgiver()).isEqualTo(arbeidsgiver2);
    }

    @Test
    void skal_finne_tilkommet_frilansandel_dersom_en_arbeidstakerandel_fra_start_med_overlapp_til_frilans() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagFrilansandel(2L, AndelKilde.PROSESS_PERIODISERING);
        var yrkesaktivitet2 = lagFrilansYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagFrilansAktivitet(), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, yrkesaktivitet2), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), STP);

        assertThat(tilkommetAktivitet.size()).isEqualTo(1);
        assertThat(tilkommetAktivitet.iterator().next().aktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
    }

    @Test
    void skal_finne_tilkommet_frilansandel_dersom_en_arbeidstakerandel_fra_start_med_overlapp_til_frilans_uten_inntekt_og_tilkommet_to_måneder_siden() {

        var stp = LocalDate.now().minusMonths(2).minusDays(10);

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, stp.minusMonths(10), stp.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, stp, stp.plusDays(20)));

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagFrilansandel(2L, AndelKilde.PROSESS_PERIODISERING);
        var yrkesaktivitet2 = lagFrilansYrkesaktivitet(arbeidsgiver2, stp.plusDays(10), stp.plusDays(15), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagFrilansAktivitet(), lagUtbetalingsgrader(50, stp.plusDays(10), stp.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(stp.plusDays(10), stp.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, yrkesaktivitet2), List.of(), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), stp);

        assertThat(tilkommetAktivitet.size()).isEqualTo(1);
        assertThat(tilkommetAktivitet.iterator().next().aktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
    }

    @Test
    void skal_finne_tilkommet_frilansandel_dersom_en_arbeidstakerandel_fra_start_med_overlapp_til_frilans_uten_inntekt_og_tilkommet_den_første_for_to_måneder_siden() {

        var stp = LocalDate.now().minusMonths(2).withDayOfMonth(1).minusDays(10);

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, stp.minusMonths(10), stp.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, stp, stp.plusDays(20)));

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagFrilansandel(2L, AndelKilde.PROSESS_PERIODISERING);
        var yrkesaktivitet2 = lagFrilansYrkesaktivitet(arbeidsgiver2, stp.plusDays(10), stp.plusDays(15), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagFrilansAktivitet(), lagUtbetalingsgrader(50, stp.plusDays(10), stp.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(stp.plusDays(10), stp.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, yrkesaktivitet2), List.of(), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), stp);

        assertThat(tilkommetAktivitet.size()).isEqualTo(1);
        assertThat(tilkommetAktivitet.iterator().next().aktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
    }

    @Test
    void skal_finne_tilkommet_frilansandel_dersom_en_arbeidstakerandel_fra_start_med_overlapp_til_frilans_uten_inntekt_og_siste_dag_den_første_for_to_måneder_siden() {

        var stp = LocalDate.now().minusMonths(2).withDayOfMonth(1).minusDays(15);

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, stp.minusMonths(10), stp.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, stp, stp.plusDays(20)));

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagFrilansandel(2L, AndelKilde.PROSESS_PERIODISERING);
        var yrkesaktivitet2 = lagFrilansYrkesaktivitet(arbeidsgiver2, stp.plusDays(10), stp.plusDays(15), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagFrilansAktivitet(), lagUtbetalingsgrader(50, stp.plusDays(10), stp.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(stp.plusDays(10), stp.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, yrkesaktivitet2), List.of(), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), stp);

        assertThat(tilkommetAktivitet.size()).isEqualTo(1);
        assertThat(tilkommetAktivitet.iterator().next().aktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
    }



    @Test
    void skal_ikke_finne_tilkommet_frilansandel_dersom_en_arbeidstakerandel_fra_start_med_overlapp_til_frilans_uten_inntekt_og_periode_er_passert_med_to_måneder() {

        var stp = LocalDate.of(2022, 8, 1);

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, stp.minusMonths(10), stp.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, stp, stp.plusDays(20)));

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagFrilansandel(2L, AndelKilde.PROSESS_PERIODISERING);
        var yrkesaktivitet2 = lagFrilansYrkesaktivitet(arbeidsgiver2, stp.plusDays(10), stp.plusDays(15), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagFrilansAktivitet(), lagUtbetalingsgrader(50, stp.plusDays(10), stp.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(stp.plusDays(10), stp.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet, yrkesaktivitet2), List.of(), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), stp);

        assertThat(tilkommetAktivitet.size()).isEqualTo(0);
    }


    @Test
    void skal_finne_tilkommet_næring_dersom_en_arbeidstakerandel_fra_start_med_overlapp_til_næring() {

        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
        var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
        var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(15), InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));

        var nyAndel = lagNæringsandel(2L, AndelKilde.PROSESS_PERIODISERING);
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagNæringsAktivitet(), lagUtbetalingsgrader(50, STP.plusDays(10), STP.plusDays(20)));

        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));

        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(yrkesaktivitet), List.of(arbeidstakerandelFraStart, nyAndel), new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel)), STP);

        assertThat(tilkommetAktivitet.size()).isEqualTo(1);
        assertThat(tilkommetAktivitet.iterator().next().aktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    void skal_finne_tilkommet_andel_dersom_en_dagpengeandel_fra_start_og_direkte_overgang_til_arbeid() {

        var frilansandelFraStart = lagDagpengeAndel(1L, AndelKilde.PROSESS_START);
        var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagDagpengeAktivitet(), lagUtbetalingsgrader(100, STP, STP.plusDays(20)));

        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
        var nyAndel = lagArbeidstakerandel(arbeidsgiver2, 2L, AndelKilde.PROSESS_PERIODISERING, InternArbeidsforholdRefDto.nullRef());
        var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()),
                lagUtbetalingsgrader(50, STP, STP.plusDays(20)));

        var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, STP.plusDays(10), STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());

        var utbetalingsgradGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(utbetalingsgradFraStart, utbetalingsgradNyAndel));
        var periode = Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusDays(15));
        var tilkommetAktivitet = finnTilkomneAndeler(periode, List.of(nyYrkesaktivitet), List.of(frilansandelFraStart, nyAndel), utbetalingsgradGrunnlag, STP);

        assertThat(tilkommetAktivitet.size()).isEqualTo(1);
        var iterator = tilkommetAktivitet.iterator();
        assertThat(iterator.next().arbeidsgiver()).isEqualTo(arbeidsgiver2);
    }


    private Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> finnTilkomneAndeler(Intervall periode,
                                                                                           List<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                           List<BeregningsgrunnlagPrStatusOgAndelDto> andelerFraStart,
                                                                                           PleiepengerSyktBarnGrunnlag utbetalingsgradGrunnlag,
                                                                                           LocalDate skjæringstidspunkt) {
        return finnTilkomneAndeler(periode, yrkesaktiviteter, lagInntektForFrilans(yrkesaktiviteter), andelerFraStart, utbetalingsgradGrunnlag, skjæringstidspunkt);
    }

    private Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> finnTilkomneAndeler(Intervall periode,
                                                                                           List<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                           Collection<InntektspostDto> inntektsposter,
                                                                                           List<BeregningsgrunnlagPrStatusOgAndelDto> andelerFraStart,
                                                                                           PleiepengerSyktBarnGrunnlag utbetalingsgradGrunnlag,
                                                                                           LocalDate skjæringstidspunkt) {
        var tidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(skjæringstidspunkt, yrkesaktiviteter, inntektsposter, andelerFraStart, utbetalingsgradGrunnlag);
        var segmenter = tidslinje.intersection(new LocalDateInterval(periode.getFomDato(), periode.getTomDato())).compress().toSegments();
        return segmenter.isEmpty() ? new LinkedHashSet<>() : segmenter.stream().map(LocalDateSegment::getValue)
                .filter(s -> !s.isEmpty()).findFirst().orElse(Set.of());
    }

    private Collection<InntektspostDto> lagInntektForFrilans(List<YrkesaktivitetDto> yrkesaktiviteter) {
        List<InntektDto> inntekter = new ArrayList<>();
        yrkesaktiviteter.stream().filter(ya -> ya.getArbeidType().equals(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER))
                        .forEach(ya -> {
                            var inntektDtoBuilder = InntektDtoBuilder.oppdatere(Optional.empty())
                                    .medArbeidsgiver(ya.getArbeidsgiver())
                                    .medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING);
                            ya.getAlleAnsettelsesperioder().stream()
                                    .forEach(p -> inntektDtoBuilder.leggTilInntektspost(InntektspostDtoBuilder.ny()
                                            .medInntektspostType(InntektspostType.LØNN)
                                            .medPeriode(p.getPeriode().getFomDato(), p.getPeriode().getTomDato())
                                            .medBeløp(BigDecimal.valueOf(10_000))));
                            inntekter.add(inntektDtoBuilder.build());
                        });
        return inntekter.stream().flatMap(i -> i.getAlleInntektsposter().stream()).toList();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagArbeidstakerandel(Arbeidsgiver arbeidsgiver2, long andelsnr, AndelKilde kilde, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver2).medArbeidsforholdRef(arbeidsforholdRef))
                .medKilde(kilde)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagFrilansandel(long andelsnr, AndelKilde kilde) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr)
                .medKilde(kilde)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build();
    }


    private BeregningsgrunnlagPrStatusOgAndelDto lagNæringsandel(long andelsnr, AndelKilde kilde) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr)
                .medKilde(kilde)
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build();
    }


    private BeregningsgrunnlagPrStatusOgAndelDto lagDagpengeAndel(long andelsnr, AndelKilde kilde) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr)
                .medKilde(kilde)
                .medAktivitetStatus(AktivitetStatus.DAGPENGER)
                .build();
    }


    private YrkesaktivitetDto lagYrkesaktivitet(Arbeidsgiver arbeidsgiver2, LocalDate fom, LocalDate tom, InternArbeidsforholdRefDto arbeidsforholdId) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver2)
                .medArbeidsforholdId(arbeidsforholdId)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                        .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                        .medErAnsettelsesPeriode(true))
                .build();
    }

    private YrkesaktivitetDto lagFrilansYrkesaktivitet(Arbeidsgiver arbeidsgiver2, LocalDate fom, LocalDate tom, InternArbeidsforholdRefDto arbeidsforholdId) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver2)
                .medArbeidsforholdId(arbeidsforholdId)
                .medArbeidType(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                        .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                        .medErAnsettelsesPeriode(true))
                .build();
    }


    private List<PeriodeMedUtbetalingsgradDto> lagUtbetalingsgrader(int i, LocalDate fom, LocalDate tom) {
        return List.of(lagPeriodeMedUtbetalingsgrad(i, fom, tom));
    }

    private PeriodeMedUtbetalingsgradDto lagPeriodeMedUtbetalingsgrad(int i, LocalDate fom, LocalDate tom) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(fom, tom), BigDecimal.valueOf(i));
    }

    private AktivitetDto lagAktivitet(Arbeidsgiver arbeidsgiver2, InternArbeidsforholdRefDto ref) {
        return new AktivitetDto(arbeidsgiver2, ref, UttakArbeidType.ORDINÆRT_ARBEID);
    }

    private AktivitetDto lagFrilansAktivitet() {
        return new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.FRILANS);
    }

    private AktivitetDto lagNæringsAktivitet() {
        return new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
    }


    private AktivitetDto lagDagpengeAktivitet() {
        return new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.DAGPENGER);
    }

}
