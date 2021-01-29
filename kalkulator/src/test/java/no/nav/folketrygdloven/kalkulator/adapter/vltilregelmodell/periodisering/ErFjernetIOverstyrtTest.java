package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.BekreftetPermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BekreftetPermisjonStatus;

class ErFjernetIOverstyrtTest {
    private static final LocalDate STP = LocalDate.of(2020,1,1);
    private static InntektArbeidYtelseGrunnlagDtoBuilder IAY_BUILDER;
    private static InntektArbeidYtelseAggregatBuilder BUILDER;
    private static InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder ARBEID_BUILDER;
    private static ArbeidsforholdInformasjonDtoBuilder ARBFOR_INFO_BUILDER;
    private static BeregningAktivitetAggregatDto.Builder BG_AKTIVITET_AGGREGAT_BUILDER;


    @BeforeEach
    public void setup() {
        // Nullstiller aggregat
        IAY_BUILDER = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BG_AKTIVITET_AGGREGAT_BUILDER = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(STP);
        BUILDER = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        ARBEID_BUILDER = BUILDER.getAktørArbeidBuilder();
        ARBFOR_INFO_BUILDER = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
    }

    @Test
    public void arbeidsforhold_som_var_i_permisjon_og_ligger_i_beregningsaggregat_er_ikke_fjernet() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitet(ag, ref);
        lagPermisjonForAG(STP.minusMonths(3), STP.plusMonths(2), BekreftetPermisjonStatus.BRUK_PERMISJON, ag, ref);
        lagBgAktivitet(ag, ref, Intervall.fraOgMed(STP.minusYears(2)));
        InntektArbeidYtelseGrunnlagDto grunnlag = ferdigstillIAYGrunnlag();
        BeregningAktivitetAggregatDto bgAggregat = ferdigstillBGAggregat();

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());

        // Act
        boolean erFjernet = ErFjernetIOverstyrt.erFjernetIOverstyrt(filter, ya.build(), bgAggregat, STP);

        assertThat(erFjernet).isFalse();
    }

    @Test
    public void arbeidsforhold_som_var_i_permisjon_på_stp_skal_måtte_ligge_i_beregningsaggregat() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitet(ag, ref);
        lagPermisjonForAG(STP.minusMonths(3), STP.plusMonths(2), BekreftetPermisjonStatus.BRUK_PERMISJON, ag, ref);
        InntektArbeidYtelseGrunnlagDto grunnlag = ferdigstillIAYGrunnlag();
        BeregningAktivitetAggregatDto bgAggregat = ferdigstillBGAggregat();

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());

        // Act
        boolean erFjernet = ErFjernetIOverstyrt.erFjernetIOverstyrt(filter, ya.build(), bgAggregat, STP);

        assertThat(erFjernet).isFalse();
    }

    @Test
    public void arbeidsforhold_som_ikke_var_i_permisjon_på_stp_skal_måtte_ligge_i_beregningsaggregat() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitet(ag, ref);
        InntektArbeidYtelseGrunnlagDto grunnlag = ferdigstillIAYGrunnlag();
        BeregningAktivitetAggregatDto bgAggregat = ferdigstillBGAggregat();

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());

        // Act
        boolean erFjernet = ErFjernetIOverstyrt.erFjernetIOverstyrt(filter, ya.build(), bgAggregat, STP);

        assertThat(erFjernet).isTrue();
    }

    private BeregningAktivitetAggregatDto ferdigstillBGAggregat() {
        return BG_AKTIVITET_AGGREGAT_BUILDER.build();
    }

    private void lagBgAktivitet(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, Intervall periode) {
        BeregningAktivitetDto aktivitet = BeregningAktivitetDto.builder().medArbeidsforholdRef(ref).medArbeidsgiver(ag).medPeriode(periode).build();
        BG_AKTIVITET_AGGREGAT_BUILDER.leggTilAktivitet(aktivitet);
    }

    private InntektArbeidYtelseGrunnlagDto ferdigstillIAYGrunnlag() {
        IAY_BUILDER.medData(BUILDER);
        IAY_BUILDER.medInformasjon(ARBFOR_INFO_BUILDER.build());
        return IAY_BUILDER.build();
    }

    private void lagPermisjonForAG(LocalDate fom, LocalDate tom, BekreftetPermisjonStatus status, Arbeidsgiver ag, InternArbeidsforholdRefDto ref) {
        ArbeidsforholdOverstyringDtoBuilder osBuilder = ARBFOR_INFO_BUILDER.getOverstyringBuilderFor(ag, ref);
        osBuilder.medBekreftetPermisjon(new BekreftetPermisjonDto(fom, tom, status));
        ARBFOR_INFO_BUILDER.leggTil(osBuilder);
    }

    private YrkesaktivitetDtoBuilder lagYrkesaktivitet(Arbeidsgiver ag, InternArbeidsforholdRefDto ref) {
        YrkesaktivitetDtoBuilder yaBuilder = ARBEID_BUILDER
                .getYrkesaktivitetBuilderForType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsgiver(ag)
                .medArbeidsforholdId(ref)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(Intervall.fraOgMed(STP.minusYears(5))));
        ARBEID_BUILDER.leggTilYrkesaktivitet(yaBuilder);
        return yaBuilder;

    }



}
