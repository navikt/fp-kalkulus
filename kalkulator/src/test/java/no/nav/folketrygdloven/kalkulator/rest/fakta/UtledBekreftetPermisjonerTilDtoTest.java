package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapArbeidsgiver;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.BekreftetPermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BekreftetPermisjonStatus;


public class UtledBekreftetPermisjonerTilDtoTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock();

    @Test
    public void skal_returne_empty_hvis_bekreftet_permisjon_ikke_er_present(){

        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet("1");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.plusDays(1);

        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        AktivitetsAvtaleDtoBuilder aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = lagAktørArbeidBuilder(behandlingReferanse, List.of(ya));

        ArbeidsforholdInformasjonDtoBuilder informasjonBuilder = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
        ArbeidsforholdOverstyringDtoBuilder overstyringBuilder = informasjonBuilder.getOverstyringBuilderFor(mapArbeidsgiver(arbeidsgiver), ref);
        informasjonBuilder.leggTil(overstyringBuilder);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagGrunnlag(aktørArbeidBuilder, Optional.of(informasjonBuilder.build()));

        // Act
        Optional<no.nav.folketrygdloven.kalkulator.rest.dto.PermisjonDto> permisjonDto = UtledBekreftetPermisjonerTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).isEmpty();

    }

    @Test
    public void skal_returne_empty_hvis_bekreftet_permisjon_slutter_før_stp(){

        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet("1");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusYears(1);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);

        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        AktivitetsAvtaleDtoBuilder aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = lagAktørArbeidBuilder(behandlingReferanse, List.of(ya));

        ArbeidsforholdInformasjonDtoBuilder informasjonBuilder = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
        ArbeidsforholdOverstyringDtoBuilder overstyringBuilder = informasjonBuilder.getOverstyringBuilderFor(mapArbeidsgiver(arbeidsgiver), ref)
            .medBekreftetPermisjon(new BekreftetPermisjonDto(fom, tom, BekreftetPermisjonStatus.BRUK_PERMISJON));
        informasjonBuilder.leggTil(overstyringBuilder);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagGrunnlag(aktørArbeidBuilder, Optional.of(informasjonBuilder.build()));

        // Act
        Optional<no.nav.folketrygdloven.kalkulator.rest.dto.PermisjonDto> permisjonDto = UtledBekreftetPermisjonerTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).isEmpty();

    }

    @Test
    public void skal_returne_empty_hvis_bekreftet_permisjon_starter_etter_stp(){

        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet("1");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        LocalDate fom = SKJÆRINGSTIDSPUNKT.plusDays(1);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.plusYears(1);

        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        AktivitetsAvtaleDtoBuilder aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = lagAktørArbeidBuilder(behandlingReferanse, List.of(ya));

        ArbeidsforholdInformasjonDtoBuilder informasjonBuilder = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
        ArbeidsforholdOverstyringDtoBuilder overstyringBuilder = informasjonBuilder.getOverstyringBuilderFor(mapArbeidsgiver(arbeidsgiver), ref)
            .medBekreftetPermisjon(new BekreftetPermisjonDto(fom, tom, BekreftetPermisjonStatus.BRUK_PERMISJON));
        informasjonBuilder.leggTil(overstyringBuilder);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagGrunnlag(aktørArbeidBuilder, Optional.of(informasjonBuilder.build()));

        // Act
        Optional<no.nav.folketrygdloven.kalkulator.rest.dto.PermisjonDto> permisjonDto = UtledBekreftetPermisjonerTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).isEmpty();

    }

    @Test
    public void skal_returne_empty_hvis_bekreftet_permisjon_ikke_har_status_BRUK_PERMISJON(){

        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet("1");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        LocalDate fom = SKJÆRINGSTIDSPUNKT.plusDays(1);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.plusYears(1);

        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        AktivitetsAvtaleDtoBuilder aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = lagAktørArbeidBuilder(behandlingReferanse, List.of(ya));

        ArbeidsforholdInformasjonDtoBuilder informasjonBuilder = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
        ArbeidsforholdOverstyringDtoBuilder overstyringBuilder = informasjonBuilder.getOverstyringBuilderFor(mapArbeidsgiver(arbeidsgiver), ref)
            .medBekreftetPermisjon(new BekreftetPermisjonDto(fom, tom, BekreftetPermisjonStatus.IKKE_BRUK_PERMISJON));
        informasjonBuilder.leggTil(overstyringBuilder);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagGrunnlag(aktørArbeidBuilder, Optional.of(informasjonBuilder.build()));

        // Act
        Optional<no.nav.folketrygdloven.kalkulator.rest.dto.PermisjonDto> permisjonDto = UtledBekreftetPermisjonerTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).isEmpty();

    }

    @Test
    public void skal_returne_permisjonDto(){

        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet("1");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.plusDays(1);

        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        AktivitetsAvtaleDtoBuilder aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = lagAktørArbeidBuilder(behandlingReferanse, List.of(ya));

        ArbeidsforholdInformasjonDtoBuilder informasjonBuilder = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
        ArbeidsforholdOverstyringDtoBuilder overstyringBuilder = informasjonBuilder.getOverstyringBuilderFor(mapArbeidsgiver(arbeidsgiver), ref)
            .medBekreftetPermisjon(new BekreftetPermisjonDto(fom, tom, BekreftetPermisjonStatus.BRUK_PERMISJON));
        informasjonBuilder.leggTil(overstyringBuilder);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagGrunnlag(aktørArbeidBuilder, Optional.of(informasjonBuilder.build()));

        // Act
        Optional<no.nav.folketrygdloven.kalkulator.rest.dto.PermisjonDto> permisjonDto = UtledBekreftetPermisjonerTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).hasValueSatisfying(p -> {
            assertThat(p.getPermisjonFom()).isEqualTo(fom);
            assertThat(p.getPermisjonTom()).isEqualTo(tom);
        });

    }

    private InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder lagAktørArbeidBuilder(BehandlingReferanse behandlingReferanse, List<YrkesaktivitetDtoBuilder> yrkesaktiviteter) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder
            .oppdatere(Optional.empty()).medAktørId(behandlingReferanse.getAktørId());
        yrkesaktiviteter.forEach(aktørArbeidBuilder::leggTilYrkesaktivitet);
        return aktørArbeidBuilder;
    }

    private InntektArbeidYtelseGrunnlagDto lagGrunnlag(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                                       Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjonOpt) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
            .leggTilAktørArbeid(aktørArbeidBuilder);
        InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
            .medData(inntektArbeidYtelseAggregatBuilder);
        arbeidsforholdInformasjonOpt.ifPresent(inntektArbeidYtelseGrunnlagBuilder::medInformasjon);
        return inntektArbeidYtelseGrunnlagBuilder.build();
    }

    private YrkesaktivitetDtoBuilder lagYrkesaktivitetBuilder(YrkesaktivitetDtoBuilder yrkesaktivitetBuilder, AktivitetsAvtaleDtoBuilder aktivitetsAvtale,
                                                              ArbeidsgiverMedNavn arbeidsgiver, InternArbeidsforholdRefDto ref) {
        yrkesaktivitetBuilder
            .medArbeidsforholdId(ref)
            .medArbeidsgiver(mapArbeidsgiver(arbeidsgiver))
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);
        return yrkesaktivitetBuilder;
    }

    private AktivitetsAvtaleDtoBuilder lagAktivitetsAvtaleBuilder(YrkesaktivitetDtoBuilder yrkesaktivitetBuilder, LocalDate fom, LocalDate tom) {
        return yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
    }

    private BGAndelArbeidsforholdRestDto lagBGAndelArbeidsforhold(Optional<ArbeidsgiverMedNavn> arbeidsgiverOpt, InternArbeidsforholdRefDto ref) {
        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(BigDecimal.valueOf(500_000))
            .build();
        BeregningsgrunnlagPeriodeRestDto periode = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
        BGAndelArbeidsforholdRestDto.Builder builder = BGAndelArbeidsforholdRestDto.builder()
            .medArbeidsforholdRef(ref);
        arbeidsgiverOpt.ifPresent(builder::medArbeidsgiver);
        BeregningsgrunnlagPrStatusOgAndelRestDto andel = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(builder)
            .build(periode);
        return andel.getBgAndelArbeidsforhold().orElseThrow();
    }

}
