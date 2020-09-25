package no.nav.folketrygdloven.kalkulus.mapFraEntitet;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagArbeidstakerAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagFrilansAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;


public class BGMapperTilKalkulus {
    public static SammenligningsgrunnlagDto mapSammenligningsgrunnlag(Sammenligningsgrunnlag fraFagsystem) {
        SammenligningsgrunnlagDto.Builder builder = SammenligningsgrunnlagDto.builder();
        builder.medAvvikPromilleNy(fraFagsystem.getAvvikPromilleNy());
        builder.medRapportertPrÅr(fraFagsystem.getRapportertPrÅr());
        builder.medSammenligningsperiode(fraFagsystem.getSammenligningsperiodeFom(), fraFagsystem.getSammenligningsperiodeTom());
        return builder.build();
    }

    public static BeregningsgrunnlagAktivitetStatusDto.Builder mapAktivitetStatus(BeregningsgrunnlagAktivitetStatus fraFagsystem) {
        BeregningsgrunnlagAktivitetStatusDto.Builder builder = new BeregningsgrunnlagAktivitetStatusDto.Builder();
        builder.medAktivitetStatus(AktivitetStatus.fraKode(fraFagsystem.getAktivitetStatus().getKode()));
        builder.medHjemmel(Hjemmel.fraKode(fraFagsystem.getHjemmel().getKode()));

        return builder;
    }

    public static BeregningsgrunnlagPeriodeDto.Builder mapBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode fraFagsystem, Collection<InntektsmeldingDto> inntektsmeldinger, boolean medRegelEvaluering) {
        BeregningsgrunnlagPeriodeDto.Builder builder = new BeregningsgrunnlagPeriodeDto.Builder();

        //med
        builder.medAvkortetPrÅr(fraFagsystem.getAvkortetPrÅr());
        builder.medBeregningsgrunnlagPeriode(fraFagsystem.getBeregningsgrunnlagPeriodeFom(), fraFagsystem.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(fraFagsystem.getBruttoPrÅr());
        builder.medRedusertPrÅr(fraFagsystem.getRedusertPrÅr());

        if(medRegelEvaluering) {
            builder.medRegelEvalueringFastsett(fraFagsystem.getRegelInputFastsett(), fraFagsystem.getRegelEvalueringFastsett());
            builder.medRegelEvalueringFinnGrenseverdi(fraFagsystem.getRegelInputFinnGrenseverdi(), fraFagsystem.getRegelEvalueringFinnGrenseverdi());
            builder.medRegelEvalueringForeslå(fraFagsystem.getRegelInput(), fraFagsystem.getRegelEvaluering());
            builder.medRegelEvalueringVilkårsvurdering(fraFagsystem.getRegelInputVilkårvurdering(), fraFagsystem.getRegelEvalueringVilkårvurdering());
        }

        //legg til
        fraFagsystem.getPeriodeÅrsaker().forEach(periodeÅrsak -> builder.leggTilPeriodeÅrsak(PeriodeÅrsak.fraKode(periodeÅrsak.getKode())));
        fraFagsystem.getBeregningsgrunnlagPrStatusOgAndelList().forEach( statusOgAndel -> builder.leggTilBeregningsgrunnlagPrStatusOgAndel(mapStatusOgAndel(statusOgAndel, inntektsmeldinger)));

        return builder;
    }

    public static SammenligningsgrunnlagPrStatusDto.Builder mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatus fraFagsystem) {
        SammenligningsgrunnlagPrStatusDto.Builder builder = new SammenligningsgrunnlagPrStatusDto.Builder();
        builder.medAvvikPromilleNy(fraFagsystem.getAvvikPromilleNy());
        builder.medRapportertPrÅr(fraFagsystem.getRapportertPrÅr());
        builder.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.fraKode(fraFagsystem.getSammenligningsgrunnlagType().getKode()));
        builder.medSammenligningsperiode(fraFagsystem.getSammenligningsperiodeFom(), fraFagsystem.getSammenligningsperiodeTom());

        return builder;
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder mapStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel fraFagsystem, Collection<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.fraKode(fraFagsystem.getAktivitetStatus().getKode()))
            .medAndelsnr(fraFagsystem.getAndelsnr())
            .medArbforholdType(fraFagsystem.getArbeidsforholdType() == null ? null : OpptjeningAktivitetType.fraKode(fraFagsystem.getArbeidsforholdType().getKode()))
            .medAvkortetBrukersAndelPrÅr(fraFagsystem.getAvkortetBrukersAndelPrÅr())
            .medAvkortetPrÅr(fraFagsystem.getAvkortetPrÅr())
            .medAvkortetRefusjonPrÅr(fraFagsystem.getAvkortetRefusjonPrÅr())
            .medBeregnetPrÅr(fraFagsystem.getBeregnetPrÅr())
            .medBesteberegningPrÅr(fraFagsystem.getBesteberegningPrÅr())
            .medFastsattAvSaksbehandler(fraFagsystem.getFastsattAvSaksbehandler())
            .medOverstyrtPrÅr(fraFagsystem.getOverstyrtPrÅr())
            .medFordeltPrÅr(fraFagsystem.getFordeltPrÅr())
            .medRedusertPrÅr(fraFagsystem.getRedusertPrÅr())
            .medRedusertBrukersAndelPrÅr(fraFagsystem.getRedusertBrukersAndelPrÅr())
            .medMaksimalRefusjonPrÅr(fraFagsystem.getMaksimalRefusjonPrÅr())
            .medRedusertRefusjonPrÅr(fraFagsystem.getRedusertRefusjonPrÅr())
            .medNyIArbeidslivet(fraFagsystem.getNyIArbeidslivet())
            .medÅrsbeløpFraTilstøtendeYtelse(fraFagsystem.getÅrsbeløpFraTilstøtendeYtelse() == null ? null : fraFagsystem.getÅrsbeløpFraTilstøtendeYtelse().getVerdi())
            .medInntektskategori(fraFagsystem.getInntektskategori() == null ? null : Inntektskategori.fraKode(fraFagsystem.getInntektskategori().getKode()))
            .medLagtTilAvSaksbehandler(fraFagsystem.getLagtTilAvSaksbehandler())
            .medOrginalDagsatsFraTilstøtendeYtelse(fraFagsystem.getOrginalDagsatsFraTilstøtendeYtelse());

        if (fraFagsystem.getAktivitetStatus().erArbeidstaker()) {
            builder.medBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelDto.builder()
                .medMottarYtelse(fraFagsystem.mottarYtelse().orElse(null))
                .build());
        }

        if (fraFagsystem.getAktivitetStatus().erFrilanser() && (fraFagsystem.mottarYtelse().isPresent() || fraFagsystem.erNyoppstartet().isPresent())) {
            builder.medBeregningsgrunnlagFrilansAndel(BeregningsgrunnlagFrilansAndelDto.builder()
                .medMottarYtelse(fraFagsystem.mottarYtelse().orElse(null))
                .medNyoppstartet(fraFagsystem.erNyoppstartet().orElse(null))
                .build());
        }

        if (fraFagsystem.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(fraFagsystem.getBeregningsperiodeFom(), fraFagsystem.getBeregningsperiodeTom());
        }

        if (fraFagsystem.getPgiSnitt() != null) {
            builder.medPgi(fraFagsystem.getPgiSnitt(), List.of(fraFagsystem.getPgi1(), fraFagsystem.getPgi2(), fraFagsystem.getPgi3()));
        }

        fraFagsystem.getBgAndelArbeidsforhold().ifPresent(bgAndelArbeidsforhold -> builder.medBGAndelArbeidsforhold(BGMapperTilKalkulus.magBGAndelArbeidsforhold(bgAndelArbeidsforhold)));
        return builder;
    }


    private static boolean gjelderInntektsmeldingFor(BeregningsgrunnlagPrStatusOgAndel fraFagsystem, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        Optional<BGAndelArbeidsforhold> bgAndelArbeidsforholdOpt = fraFagsystem.getBgAndelArbeidsforhold();
        if (!Objects.equals(fraFagsystem.getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER) || !bgAndelArbeidsforholdOpt.isPresent()) {
            return false;
        }
        if (!Objects.equals(fraFagsystem.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsgiver).map(IAYMapperTilKalkulus::mapArbeidsgiver), Optional.of(arbeidsgiver))) {
            return false;
        }
        if (fraFagsystem.getArbeidsforholdRef().isEmpty() || !fraFagsystem.getArbeidsforholdRef().get().gjelderForSpesifiktArbeidsforhold()) {
            boolean harPeriodeAndelForSammeArbeidsgiverMedReferanse = fraFagsystem.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> a.getAktivitetStatus().erArbeidstaker())
                .filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .anyMatch(a -> a.getArbeidsforholdRef().isPresent() && a.getArbeidsforholdRef().get().gjelderForSpesifiktArbeidsforhold());

            if (harPeriodeAndelForSammeArbeidsgiverMedReferanse) {
                return false;
            }
        }
        return  bgAndelArbeidsforholdOpt.map(BGAndelArbeidsforhold::getArbeidsforholdRef)
            .map(IAYMapperTilKalkulus::mapArbeidsforholdRef).get().equals(arbeidsforholdRef);
    }


    private static BGAndelArbeidsforholdDto.Builder magBGAndelArbeidsforhold(BGAndelArbeidsforhold fraFagsystem) {
        BGAndelArbeidsforholdDto.Builder builder = BGAndelArbeidsforholdDto.builder();
        builder.medArbeidsforholdRef(IAYMapperTilKalkulus.mapArbeidsforholdRef(fraFagsystem.getArbeidsforholdRef()));
        builder.medArbeidsgiver(IAYMapperTilKalkulus.mapArbeidsgiver(fraFagsystem.getArbeidsgiver()));
        builder.medArbeidsperiodeFom(fraFagsystem.getArbeidsperiodeFom());
        builder.medLønnsendringIBeregningsperioden(fraFagsystem.erLønnsendringIBeregningsperioden());
        builder.medTidsbegrensetArbeidsforhold(fraFagsystem.getErTidsbegrensetArbeidsforhold());
        builder.medRefusjonskravPrÅr(fraFagsystem.getRefusjonskravPrÅr());
        builder.medHjemmel(fraFagsystem.getHjemmelForRefusjonskravfrist() == null ? Hjemmel.UDEFINERT : Hjemmel.fraKode(fraFagsystem.getHjemmelForRefusjonskravfrist().getKode()));

        fraFagsystem.getArbeidsperiodeTom().ifPresent(builder::medArbeidsperiodeTom);
        fraFagsystem.getNaturalytelseBortfaltPrÅr().ifPresent(builder::medNaturalytelseBortfaltPrÅr);
        fraFagsystem.getNaturalytelseTilkommetPrÅr().ifPresent(builder::medNaturalytelseTilkommetPrÅr);
        return builder;
    }
}
