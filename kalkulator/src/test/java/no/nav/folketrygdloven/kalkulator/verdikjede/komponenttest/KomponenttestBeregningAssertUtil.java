package no.nav.folketrygdloven.kalkulator.verdikjede.komponenttest;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;

class KomponenttestBeregningAssertUtil {


    static void assertBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag,
                                         LocalDate skjæringstidspunktForBeregning,
                                         List<AktivitetStatus> aktivitetStatuser) {
        assertThat(beregningsgrunnlag.getSkjæringstidspunkt()).isEqualTo(skjæringstidspunktForBeregning);
        assertThat(beregningsgrunnlag.getGrunnbeløp() != null).isTrue();
        assertThat(beregningsgrunnlag.getAktivitetStatuser()).hasSize(aktivitetStatuser.size());
        for (int i = 0; i < aktivitetStatuser.size(); i++) {
            assertThat(beregningsgrunnlag.getAktivitetStatuser().get(i).getAktivitetStatus()).isEqualTo(aktivitetStatuser.get(i));
        }
    }

    static void assertBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                Intervall periode,
                                                BigDecimal beregnetPrÅr, Long dagsats, BigDecimal overstyrtPrÅr, BigDecimal refusjonskravPrÅr) {
        assertThat(beregningsgrunnlagPeriode.getDagsats()).isEqualTo(dagsats);
        if (overstyrtPrÅr != null) {
            assertThat(beregningsgrunnlagPeriode.getBruttoPrÅr()).isEqualByComparingTo(overstyrtPrÅr);
        }
        if (refusjonskravPrÅr == null){
            assertThat(beregningsgrunnlagPeriode.getTotaltRefusjonkravIPeriode().compareTo(BigDecimal.ZERO) == 0).isTrue();

        } else {
            assertThat(beregningsgrunnlagPeriode.getTotaltRefusjonkravIPeriode()).isEqualByComparingTo(refusjonskravPrÅr);
        }
        assertThat(beregningsgrunnlagPeriode.getBeregnetPrÅr()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(periode.getFomDato());
        if (periode.getTomDato() == null) {
            assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).isNull();
        } else {
            assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(periode.getTomDato());
        }
    }

    static void assertBeregningsgrunnlagAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                              BigDecimal beregnetPrÅr, AktivitetStatus aktivitetStatus,
                                              Inntektskategori inntektskategori,
                                              LocalDate beregningsperiodFom,
                                              LocalDate beregningsperiodeTom, BigDecimal refusjonskravPrÅr, BigDecimal overstyrtPrÅr) {

        assertThat(andel.getOverstyrtPrÅr()).isEqualTo(overstyrtPrÅr);
        assertThat(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
            .as("RefusjonskravPrÅr")
            .isEqualTo(refusjonskravPrÅr);
        if (beregnetPrÅr == null) {
            assertThat(andel.getBeregnetPrÅr()).as("BeregnetPrÅr").isNull();
        } else {
            assertThat(andel.getBeregnetPrÅr()).as("BeregnetPrÅr").isEqualByComparingTo(beregnetPrÅr);
        }
        assertThat(andel.getBeregningsperiodeFom()).isEqualTo(beregningsperiodFom);
        assertThat(andel.getBeregningsperiodeTom()).isEqualTo(beregningsperiodeTom);
        assertThat(andel.getAktivitetStatus()).isEqualTo(aktivitetStatus);
        assertThat(andel.getInntektskategori()).isEqualTo(inntektskategori);

    }

    static void assertSammenligningsgrunnlag(SammenligningsgrunnlagDto sammenligningsgrunnlag,
                                             BigDecimal rapportertInntekt,
                                             BigDecimal avvik){

        assertThat(sammenligningsgrunnlag.getRapportertPrÅr()).isEqualTo(rapportertInntekt);
        assertThat(sammenligningsgrunnlag.getAvvikPromilleNy().compareTo(avvik)).isEqualTo(0);

    }

    static void assertSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag, BigDecimal rapportertInntekt,
                                             BigDecimal avvik, SammenligningsgrunnlagType sammenligningsgrunnlagType){

        assertThat(sammenligningsgrunnlag.getRapportertPrÅr()).isEqualTo(rapportertInntekt);
        assertThat(sammenligningsgrunnlag.getAvvikPromilleNy().compareTo(avvik)).isEqualTo(0);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagType()).isEqualTo(sammenligningsgrunnlagType);
    }
}
