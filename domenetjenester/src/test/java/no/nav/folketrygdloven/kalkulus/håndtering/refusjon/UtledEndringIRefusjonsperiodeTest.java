package no.nav.folketrygdloven.kalkulus.håndtering.refusjon;

import static no.nav.folketrygdloven.kalkulus.felles.jpa.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.DatoEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonoverstyringEndring;

class UtledEndringIRefusjonsperiodeTest {

    @Test
    public void skal_lage_endring_når_ingen_tidligere_vurdering_finnes() {
        // Arrange
        LocalDate refusjonFom = LocalDate.of(2020,1,1);
        var stp = refusjonFom.minusDays(10);
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("99999999");
        BeregningRefusjonPeriodeDto periode = new BeregningRefusjonPeriodeDto(InternArbeidsforholdRefDto.nullRef(), refusjonFom);
        BeregningRefusjonOverstyringDto refusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periode), null);
        BeregningRefusjonOverstyringerDto aggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring).build();
        var bg = lagPeriodisertBg(refusjonFom, stp, ag, InternArbeidsforholdRefDto.nullRef(), null);
        var forrigeBg = lagBgUtenPeriodisering(stp, ag);


        // Act
        RefusjonoverstyringEndring endringAggregat = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(aggregat, bg, Optional.empty(), Optional.of(forrigeBg));

        // Assert
        assertThat(endringAggregat).isNotNull();
        assertThat(endringAggregat.getRefusjonperiodeEndringer()).hasSize(1);
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsgiver().getIdent()).isEqualTo(ag.getIdentifikator());
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsforholdRef()).isNull();
        DatoEndring datoEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattRefusjonFomEndring();
        assertThat(datoEndring).isNotNull();
        assertThat(datoEndring.getFraVerdi()).isNull();
        assertThat(datoEndring.getTilVerdi()).isEqualTo(refusjonFom);
        var refusjonEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattDelvisRefusjonFørDatoEndring();
        assertThat(refusjonEndring).isNull();
    }

    @Test
    public void skal_lage_endring_når_ingen_matchende_ag_på_tidligere_vurdering_finnes() {
        // Arrange
        LocalDate refusjonFom = LocalDate.of(2020,1,1);
        var stp = refusjonFom.minusDays(10);
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("99999999");
        Arbeidsgiver ikkeMatchendeAG = Arbeidsgiver.virksomhet("99999998");

        BeregningRefusjonPeriodeDto periode = new BeregningRefusjonPeriodeDto(InternArbeidsforholdRefDto.nullRef(), refusjonFom);
        BeregningRefusjonOverstyringDto refusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periode), null);
        BeregningRefusjonOverstyringDto ikkeMatchendeRefusjonOverstyring = new BeregningRefusjonOverstyringDto(ikkeMatchendeAG, null, Collections.singletonList(periode), null);

        BeregningRefusjonOverstyringerDto aggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring).build();
        BeregningRefusjonOverstyringerDto forrigeAggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(ikkeMatchendeRefusjonOverstyring).build();
        var bg = lagPeriodisertBg(refusjonFom, stp, ag, InternArbeidsforholdRefDto.nullRef(), BigDecimal.TEN);
        var forrigeBg = lagPeriodisertBg(refusjonFom, stp, ikkeMatchendeAG, InternArbeidsforholdRefDto.nullRef(), null);

        // Act
        RefusjonoverstyringEndring endringAggregat = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(aggregat, bg, Optional.of(forrigeAggregat), Optional.of(forrigeBg));

        // Assert
        assertThat(endringAggregat).isNotNull();
        assertThat(endringAggregat.getRefusjonperiodeEndringer()).hasSize(1);
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsgiver().getIdent()).isEqualTo(ag.getIdentifikator());
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsforholdRef()).isNull();
        DatoEndring datoEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattRefusjonFomEndring();
        assertThat(datoEndring).isNotNull();
        assertThat(datoEndring.getFraVerdi()).isNull();
        assertThat(datoEndring.getTilVerdi()).isEqualTo(refusjonFom);
        var refusjonEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattDelvisRefusjonFørDatoEndring();
        assertThat(refusjonEndring).isNotNull();
        assertThat(refusjonEndring.getFraRefusjon()).isNull();
        assertThat(refusjonEndring.getTilRefusjon()).isEqualTo(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(10));
    }

    @Test
    public void skal_lage_endring_når_ingen_matchende_ref_på_tidligere_vurdering_finnes() {
        // Arrange
        LocalDate refusjonFom = LocalDate.of(2020,1,1);
        var stp = refusjonFom.minusDays(10);
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("99999999");

        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BeregningRefusjonPeriodeDto periode = new BeregningRefusjonPeriodeDto(ref, refusjonFom);
        BeregningRefusjonPeriodeDto ikkeMatchendePeriode = new BeregningRefusjonPeriodeDto(InternArbeidsforholdRefDto.nyRef(), refusjonFom);
        BeregningRefusjonOverstyringDto refusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periode), null);
        BeregningRefusjonOverstyringDto ikkeMatchendeRefusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(ikkeMatchendePeriode), null);

        BeregningRefusjonOverstyringerDto aggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring).build();
        BeregningRefusjonOverstyringerDto forrigeAggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(ikkeMatchendeRefusjonOverstyring).build();

        var bg = lagPeriodisertBg(refusjonFom, stp, ag, ref, BigDecimal.TEN);
        var forrigeBg = lagPeriodisertBg(refusjonFom, stp, ag, InternArbeidsforholdRefDto.nyRef(), BigDecimal.TEN);

        // Act
        RefusjonoverstyringEndring endringAggregat = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(aggregat, bg, Optional.of(forrigeAggregat), Optional.of(forrigeBg));

        // Assert
        assertThat(endringAggregat).isNotNull();
        assertThat(endringAggregat.getRefusjonperiodeEndringer()).hasSize(1);
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsgiver().getIdent()).isEqualTo(ag.getIdentifikator());
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsforholdRef()).isEqualTo(ref.getUUIDReferanse());
        DatoEndring datoEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattRefusjonFomEndring();
        assertThat(datoEndring).isNotNull();
        assertThat(datoEndring.getFraVerdi()).isNull();
        assertThat(datoEndring.getTilVerdi()).isEqualTo(refusjonFom);
        var refusjonEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattDelvisRefusjonFørDatoEndring();
        assertThat(refusjonEndring).isNotNull();
        assertThat(refusjonEndring.getFraRefusjon()).isNull();
        assertThat(refusjonEndring.getTilRefusjon()).isEqualTo(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(10));
    }

    @Test
    public void skal_lage_endring_når_tidligere_vurdering_finnes() {
        // Arrange
        LocalDate forrigeFom = LocalDate.of(2020,1,1);
        LocalDate nyFom = LocalDate.of(2020,2,1);
        var stp = nyFom.minusMonths(2);

        Arbeidsgiver ag = Arbeidsgiver.virksomhet("99999999");

        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BeregningRefusjonPeriodeDto periode = new BeregningRefusjonPeriodeDto(ref, nyFom);
        BeregningRefusjonPeriodeDto forrigePeriode = new BeregningRefusjonPeriodeDto(ref, forrigeFom);
        BeregningRefusjonOverstyringDto refusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periode), null);
        BeregningRefusjonOverstyringDto forrigeOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(forrigePeriode), null);

        BeregningRefusjonOverstyringerDto aggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring).build();
        BeregningRefusjonOverstyringerDto forrigeAggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(forrigeOverstyring).build();


        var bg = lagPeriodisertBg(nyFom, stp, ag, ref, BigDecimal.TEN);
        var forrigeBg = lagPeriodisertBg(forrigeFom, stp, ag, ref, BigDecimal.ONE);

        // Act
        RefusjonoverstyringEndring endringAggregat = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(aggregat, bg, Optional.of(forrigeAggregat), Optional.of(forrigeBg));

        // Assert
        assertThat(endringAggregat).isNotNull();
        assertThat(endringAggregat.getRefusjonperiodeEndringer()).hasSize(1);
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsgiver().getIdent()).isEqualTo(ag.getIdentifikator());
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsforholdRef()).isEqualTo(ref.getUUIDReferanse());
        DatoEndring datoEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattRefusjonFomEndring();
        assertThat(datoEndring).isNotNull();
        assertThat(datoEndring.getFraVerdi()).isEqualTo(forrigeFom);
        assertThat(datoEndring.getTilVerdi()).isEqualTo(nyFom);
        var refusjonEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattDelvisRefusjonFørDatoEndring();
        assertThat(refusjonEndring).isNotNull();
        assertThat(refusjonEndring.getFraRefusjon()).isEqualTo(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(1));
        assertThat(refusjonEndring.getTilRefusjon()).isEqualTo(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(10));
    }



    private BeregningsgrunnlagDto lagPeriodisertBg(LocalDate refusjonFom, LocalDate stp, Arbeidsgiver ag, InternArbeidsforholdRefDto arbeidsforholdRef, BigDecimal saksbehandletRefusjonPrÅr) {
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(stp)
                .build();

        var periode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(stp, refusjonFom.minusDays(1))
                .build(bg);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag).medArbeidsforholdRef(arbeidsforholdRef).medSaksbehandletRefusjonPrÅr(Beløp.fra(saksbehandletRefusjonPrÅr)))
                .build(periode1);

        var periode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(refusjonFom, TIDENES_ENDE)
                .build(bg);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag).medArbeidsforholdRef(arbeidsforholdRef))
                .build(periode2);
        return bg;
    }

    private BeregningsgrunnlagDto lagBgUtenPeriodisering(LocalDate stp, Arbeidsgiver ag) {
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(stp)
                .build();

        var periode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(stp, TIDENES_ENDE)
                .build(bg);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag).medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef()))
                .build(periode1);

        return bg;
    }
}
