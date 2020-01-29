package no.nav.folketrygdloven.kalkulator.rest;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;

public class MapBeregningsgrunnlagFraRestTilDomene {


    public static BeregningAktivitetAggregatDto mapAktivitetAggregat(BeregningAktivitetAggregatRestDto beregningAktivitetAggregat) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening());
        beregningAktivitetAggregat.getBeregningAktiviteter().stream()
            .map(MapBeregningsgrunnlagFraRestTilDomene::mapAktivitet)
            .forEach(builder::leggTilAktivitet);
        return builder.build();
    }

    private static BeregningAktivitetDto mapAktivitet(BeregningAktivitetRestDto beregningAktivitetRestDto) {
        return BeregningAktivitetDto.builder()
            .medPeriode(beregningAktivitetRestDto.getPeriode())
            .medOpptjeningAktivitetType(beregningAktivitetRestDto.getOpptjeningAktivitetType())
            .medArbeidsgiver(beregningAktivitetRestDto.getArbeidsgiver() == null ? null : mapArbeidsgiver(beregningAktivitetRestDto.getArbeidsgiver()))
            .medArbeidsforholdRef(beregningAktivitetRestDto.getArbeidsforholdRef())
            .build();
    }

    public static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medArbforholdType(andel.getArbeidsforholdType())
            .medLagtTilAvSaksbehandler(andel.getLagtTilAvSaksbehandler())
            .medAktivitetStatus(andel.getAktivitetStatus())
            .medInntektskategori(andel.getInntektskategori())
            .medAndelsnr(andel.getAndelsnr())
            .medBeregnetPrÅr(andel.getBeregnetPrÅr())
            .medOverstyrtPrÅr(andel.getOverstyrtPrÅr())
            .medFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler())
            .medAvkortetPrÅr(andel.getAvkortetPrÅr())
            .medBesteberegningPrÅr(andel.getBesteberegningPrÅr())
            .medRedusertPrÅr(andel.getRedusertPrÅr())
            .medMottarYtelse(andel.mottarYtelse().orElse(null), andel.getAktivitetStatus())
            .medFordeltPrÅr(andel.getFordeltPrÅr())
            .medNyIArbeidslivet(andel.getNyIArbeidslivet())
            .medRedusertBrukersAndelPrÅr(andel.getRedusertBrukersAndelPrÅr())
            .medRedusertRefusjonPrÅr(andel.getRedusertRefusjonPrÅr())
            .medAvkortetBrukersAndelPrÅr(andel.getAvkortetBrukersAndelPrÅr())
            .medAvkortetRefusjonPrÅr(andel.getAvkortetRefusjonPrÅr())
            .medMaksimalRefusjonPrÅr(andel.getMaksimalRefusjonPrÅr())
            .medOrginalDagsatsFraTilstøtendeYtelse(andel.getOrginalDagsatsFraTilstøtendeYtelse())
            .medÅrsbeløpFraTilstøtendeYtelse(andel.getÅrsbeløpFraTilstøtendeYtelseVerdi());
        if (andel.getPgiSnitt() != null && andel.getPgi1() != null && andel.getPgi2() != null && andel.getPgi3() != null) {
            builder
                .medPgi(andel.getPgiSnitt(), List.of(andel.getPgi1(), andel.getPgi2(), andel.getPgi3()));
        }
        if (andel.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(andel.getBeregningsperiodeFom(), andel.getBeregningsperiodeTom());
        }

        andel.getBgAndelArbeidsforhold()
            .map(MapBeregningsgrunnlagFraRestTilDomene::mapBgAndelArbeidsforhold)
            .ifPresent(builder::medBGAndelArbeidsforhold);
        return builder.build();
    }


    private static BGAndelArbeidsforholdDto.Builder mapBgAndelArbeidsforhold(BGAndelArbeidsforholdRestDto bgAndelArbeidsforholdRestDto) {
        return BGAndelArbeidsforholdDto.builder()
            .medArbeidsgiver(mapArbeidsgiver(bgAndelArbeidsforholdRestDto.getArbeidsgiver()))
            .medRefusjonskravPrÅr(bgAndelArbeidsforholdRestDto.getRefusjonskravPrÅr())
            .medArbeidsforholdRef(bgAndelArbeidsforholdRestDto.getArbeidsforholdRef())
            .medArbeidsperiodeFom(bgAndelArbeidsforholdRestDto.getArbeidsperiodeFom())
            .medArbeidsperiodeTom(bgAndelArbeidsforholdRestDto.getArbeidsperiodeFom())
            .medTidsbegrensetArbeidsforhold(bgAndelArbeidsforholdRestDto.getErTidsbegrensetArbeidsforhold())
            .medNaturalytelseBortfaltPrÅr(bgAndelArbeidsforholdRestDto.getNaturalytelseBortfaltPrÅr().orElse(null))
            .medNaturalytelseTilkommetPrÅr(bgAndelArbeidsforholdRestDto.getNaturalytelseTilkommetPrÅr().orElse(null))
            .medLønnsendringIBeregningsperioden(bgAndelArbeidsforholdRestDto.erLønnsendringIBeregningsperioden());

    }

    public static Arbeidsgiver mapArbeidsgiver(ArbeidsgiverMedNavn arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsgiver.virksomhet(arbeidsgiver.getOrgnr());
        }
        return Arbeidsgiver.person(arbeidsgiver.getAktørId());
    }

    public static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPeriodeDto.Builder builder = mapPeriodeBuilder(beregningsgrunnlagPeriode);
        return builder.build();
    }

    private static BeregningsgrunnlagPeriodeDto.Builder mapPeriodeBuilder(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom())
            .medAvkortetPrÅr(beregningsgrunnlagPeriode.getAvkortetPrÅr())
            .medRedusertPrÅr(beregningsgrunnlagPeriode.getRedusertPrÅr())
            .medBruttoPrÅr(beregningsgrunnlagPeriode.getBruttoPrÅr());
        beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(MapBeregningsgrunnlagFraRestTilDomene::mapAndel)
            .forEach(builder::leggTilBeregningsgrunnlagPrStatusOgAndel);
        return builder;
    }

    public static BeregningsgrunnlagDto mapBeregningsgrunnlag(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        BeregningsgrunnlagDto build = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(beregningsgrunnlag.getSkjæringstidspunkt())
            .leggTilFaktaOmBeregningTilfeller(beregningsgrunnlag.getFaktaOmBeregningTilfeller())
            .medGrunnbeløp(beregningsgrunnlag.getGrunnbeløp())
            .medOverstyring(beregningsgrunnlag.isOverstyrt())
            .build();

        BeregningsgrunnlagDto.Builder builder = BeregningsgrunnlagDto.Builder.oppdater(Optional.of(build));
        if (beregningsgrunnlag.getSammenligningsgrunnlag() != null) {
            builder.medSammenligningsgrunnlag(mapSammenligningsgrunnlag(beregningsgrunnlag.getSammenligningsgrunnlag(), build));
        }
        beregningsgrunnlag.getSammenligningsgrunnlagPrStatusListe().stream()
            .map(MapBeregningsgrunnlagFraRestTilDomene::mapSammenligningsgrunnlag)
            .forEach(builder::leggTilSammenligningsgrunnlag);

        beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(MapBeregningsgrunnlagFraRestTilDomene::mapPeriodeBuilder)
            .forEach(builder::leggTilBeregningsgrunnlagPeriode);

        return builder.build();
    }

    private static SammenligningsgrunnlagDto mapSammenligningsgrunnlag(SammenligningsgrunnlagRestDto sammenligningsgrunnlag, BeregningsgrunnlagDto bg) {
        return SammenligningsgrunnlagDto.builder()
            .medSammenligningsperiode(sammenligningsgrunnlag.getSammenligningsperiodeFom(), sammenligningsgrunnlag.getSammenligningsperiodeTom())
            .medRapportertPrÅr(sammenligningsgrunnlag.getRapportertPrÅr())
            .medAvvikPromille(sammenligningsgrunnlag.getAvvikPromille())
            .build(bg);
    }

    private static SammenligningsgrunnlagPrStatusDto.Builder mapSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusRestDto sammenligningsgrunnlag) {
        return SammenligningsgrunnlagPrStatusDto.builder()
            .medSammenligningsperiode(sammenligningsgrunnlag.getSammenligningsperiodeFom(), sammenligningsgrunnlag.getSammenligningsperiodeTom())
            .medRapportertPrÅr(sammenligningsgrunnlag.getRapportertPrÅr())
            .medAvvikPromille(sammenligningsgrunnlag.getAvvikPromille());
    }

    public static BeregningsgrunnlagGrunnlagDto mapBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagRestDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagGrunnlagDtoBuilder builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(mapAktivitetAggregat(beregningsgrunnlagGrunnlag.getRegisterAktiviteter()));
        beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().map(MapBeregningsgrunnlagFraRestTilDomene::mapBeregningsgrunnlag)
            .ifPresent(builder::medBeregningsgrunnlag);
        beregningsgrunnlagGrunnlag.getSaksbehandletAktiviteter().map(MapBeregningsgrunnlagFraRestTilDomene::mapAktivitetAggregat)
            .ifPresent(builder::medSaksbehandletAktiviteter);
        beregningsgrunnlagGrunnlag.getOverstyring()
            .ifPresent(builder::medOverstyring);
        return builder.build(beregningsgrunnlagGrunnlag.getBeregningsgrunnlagTilstand());
    }
}
