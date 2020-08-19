package no.nav.folketrygdloven.kalkulus.håndtering.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.DatoEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonoverstyringEndring;

class UtledEndringIRefusjonsperiodeTest {

    @Test
    public void skal_lage_endring_når_ingen_tidligere_vurdering_finnes() {
        // Arrange
        LocalDate refusjonFom = LocalDate.of(2020,1,1);
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("99999999");
        BeregningRefusjonPeriodeDto periode = new BeregningRefusjonPeriodeDto(InternArbeidsforholdRefDto.nullRef(), refusjonFom);
        BeregningRefusjonOverstyringDto refusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periode));
        BeregningRefusjonOverstyringerDto aggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring).build();

        // Act
        RefusjonoverstyringEndring endringAggregat = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(aggregat, Optional.empty());

        // Assert
        assertThat(endringAggregat).isNotNull();
        assertThat(endringAggregat.getRefusjonperiodeEndringer()).hasSize(1);
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsgiver().getIdent()).isEqualTo(ag.getIdentifikator());
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsforholdRef()).isNull();
        DatoEndring datoEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattRefusjonFomEndring();
        assertThat(datoEndring).isNotNull();
        assertThat(datoEndring.getFraVerdi()).isNull();
        assertThat(datoEndring.getTilVerdi()).isEqualTo(refusjonFom);
    }

    @Test
    public void skal_lage_endring_når_ingen_matchende_ag_på_tidligere_vurdering_finnes() {
        // Arrange
        LocalDate refusjonFom = LocalDate.of(2020,1,1);
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("99999999");
        Arbeidsgiver ikkeMatchendeAG = Arbeidsgiver.virksomhet("99999998");

        BeregningRefusjonPeriodeDto periode = new BeregningRefusjonPeriodeDto(InternArbeidsforholdRefDto.nullRef(), refusjonFom);
        BeregningRefusjonOverstyringDto refusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periode));
        BeregningRefusjonOverstyringDto ikkeMatchendeRefusjonOverstyring = new BeregningRefusjonOverstyringDto(ikkeMatchendeAG, null, Collections.singletonList(periode));

        BeregningRefusjonOverstyringerDto aggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring).build();
        BeregningRefusjonOverstyringerDto forrigeAggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(ikkeMatchendeRefusjonOverstyring).build();


        // Act
        RefusjonoverstyringEndring endringAggregat = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(aggregat, Optional.of(forrigeAggregat));

        // Assert
        assertThat(endringAggregat).isNotNull();
        assertThat(endringAggregat.getRefusjonperiodeEndringer()).hasSize(1);
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsgiver().getIdent()).isEqualTo(ag.getIdentifikator());
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsforholdRef()).isNull();
        DatoEndring datoEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattRefusjonFomEndring();
        assertThat(datoEndring).isNotNull();
        assertThat(datoEndring.getFraVerdi()).isNull();
        assertThat(datoEndring.getTilVerdi()).isEqualTo(refusjonFom);
    }

    @Test
    public void skal_lage_endring_når_ingen_matchende_ref_på_tidligere_vurdering_finnes() {
        // Arrange
        LocalDate refusjonFom = LocalDate.of(2020,1,1);
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("99999999");

        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BeregningRefusjonPeriodeDto periode = new BeregningRefusjonPeriodeDto(ref, refusjonFom);
        BeregningRefusjonPeriodeDto ikkeMatchendePeriode = new BeregningRefusjonPeriodeDto(InternArbeidsforholdRefDto.nyRef(), refusjonFom);
        BeregningRefusjonOverstyringDto refusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periode));
        BeregningRefusjonOverstyringDto ikkeMatchendeRefusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(ikkeMatchendePeriode));

        BeregningRefusjonOverstyringerDto aggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring).build();
        BeregningRefusjonOverstyringerDto forrigeAggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(ikkeMatchendeRefusjonOverstyring).build();

        // Act
        RefusjonoverstyringEndring endringAggregat = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(aggregat, Optional.of(forrigeAggregat));

        // Assert
        assertThat(endringAggregat).isNotNull();
        assertThat(endringAggregat.getRefusjonperiodeEndringer()).hasSize(1);
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsgiver().getIdent()).isEqualTo(ag.getIdentifikator());
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsforholdRef()).isEqualTo(ref.getReferanse());
        DatoEndring datoEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattRefusjonFomEndring();
        assertThat(datoEndring).isNotNull();
        assertThat(datoEndring.getFraVerdi()).isNull();
        assertThat(datoEndring.getTilVerdi()).isEqualTo(refusjonFom);
    }

    @Test
    public void skal_lage_endring_når_tidligere_vurdering_finnes() {
        // Arrange
        LocalDate forrigeFom = LocalDate.of(2020,1,1);
        LocalDate nyFom = LocalDate.of(2020,2,1);

        Arbeidsgiver ag = Arbeidsgiver.virksomhet("99999999");

        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BeregningRefusjonPeriodeDto periode = new BeregningRefusjonPeriodeDto(ref, nyFom);
        BeregningRefusjonPeriodeDto forrigePeriode = new BeregningRefusjonPeriodeDto(ref, forrigeFom);
        BeregningRefusjonOverstyringDto refusjonOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periode));
        BeregningRefusjonOverstyringDto forrigeOverstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(forrigePeriode));

        BeregningRefusjonOverstyringerDto aggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring).build();
        BeregningRefusjonOverstyringerDto forrigeAggregat = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(forrigeOverstyring).build();

        // Act
        RefusjonoverstyringEndring endringAggregat = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(aggregat, Optional.of(forrigeAggregat));

        // Assert
        assertThat(endringAggregat).isNotNull();
        assertThat(endringAggregat.getRefusjonperiodeEndringer()).hasSize(1);
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsgiver().getIdent()).isEqualTo(ag.getIdentifikator());
        assertThat(endringAggregat.getRefusjonperiodeEndringer().get(0).getArbeidsforholdRef()).isEqualTo(ref.getReferanse());
        DatoEndring datoEndring = endringAggregat.getRefusjonperiodeEndringer().get(0).getFastsattRefusjonFomEndring();
        assertThat(datoEndring).isNotNull();
        assertThat(datoEndring.getFraVerdi()).isEqualTo(forrigeFom);
        assertThat(datoEndring.getTilVerdi()).isEqualTo(nyFom);
    }


}
