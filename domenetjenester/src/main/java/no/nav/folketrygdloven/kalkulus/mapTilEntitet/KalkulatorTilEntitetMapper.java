package no.nav.folketrygdloven.kalkulus.mapTilEntitet;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.OpptjeningAktivitetType;


/**
 * Skal etterhvert benytte seg av kontrakten som skal lages i ft-Kalkulus, benytter foreløping en, en-til-en mapping på klassenivå...
 *
 */
public class KalkulatorTilEntitetMapper {

    public static BeregningsgrunnlagTilstand mapTilstand(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        return BeregningsgrunnlagTilstand.fraKode(beregningsgrunnlagTilstand.getKode());
    }

    public static BeregningsgrunnlagEntitet mapBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlagFraKalkulus) {
        BeregningsgrunnlagEntitet.Builder builder = BeregningsgrunnlagEntitet.builder();

        //med
        builder.medGrunnbeløp(new Beløp(beregningsgrunnlagFraKalkulus.getGrunnbeløp().getVerdi()));
        builder.medOverstyring(beregningsgrunnlagFraKalkulus.isOverstyrt());
        if (beregningsgrunnlagFraKalkulus.getRegelinputPeriodisering() != null) {
            builder.medRegelinputPeriodisering(beregningsgrunnlagFraKalkulus.getRegelinputPeriodisering());
        }
        if (beregningsgrunnlagFraKalkulus.getRegelInputBrukersStatus() != null && beregningsgrunnlagFraKalkulus.getRegelloggBrukersStatus() != null) {
            builder.medRegelloggBrukersStatus(beregningsgrunnlagFraKalkulus.getRegelInputBrukersStatus(), beregningsgrunnlagFraKalkulus.getRegelloggBrukersStatus());
        }
        if (beregningsgrunnlagFraKalkulus.getRegelInputSkjæringstidspunkt() != null && beregningsgrunnlagFraKalkulus.getRegelloggSkjæringstidspunkt() != null) {
            builder.medRegelloggSkjæringstidspunkt(beregningsgrunnlagFraKalkulus.getRegelInputSkjæringstidspunkt(), beregningsgrunnlagFraKalkulus.getRegelloggSkjæringstidspunkt());
        }
        builder.medSkjæringstidspunkt(beregningsgrunnlagFraKalkulus.getSkjæringstidspunkt());
        if (beregningsgrunnlagFraKalkulus.getSammenligningsgrunnlag() != null) {
            builder.medSammenligningsgrunnlagOld(KalkulatorTilBGMapper.mapSammenligningsgrunnlag(beregningsgrunnlagFraKalkulus.getSammenligningsgrunnlag()));
        }

        //lister
        beregningsgrunnlagFraKalkulus.getAktivitetStatuser().forEach(beregningsgrunnlagAktivitetStatus -> builder.leggTilAktivitetStatus(KalkulatorTilBGMapper.mapAktivitetStatus(beregningsgrunnlagAktivitetStatus)));
        beregningsgrunnlagFraKalkulus.getBeregningsgrunnlagPerioder().forEach(beregningsgrunnlagPeriode -> builder.leggTilBeregningsgrunnlagPeriode(KalkulatorTilBGMapper.mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode)));
        builder.leggTilFaktaOmBeregningTilfeller(beregningsgrunnlagFraKalkulus.getFaktaOmBeregningTilfeller().stream().map(fakta -> FaktaOmBeregningTilfelle.fraKode(fakta.getKode())).collect(Collectors.toList()));
        beregningsgrunnlagFraKalkulus.getSammenligningsgrunnlagPrStatusListe().forEach(sammenligningsgrunnlagPrStatus -> builder.leggTilSammenligningsgrunnlag(KalkulatorTilBGMapper.mapSammenligningsgrunnlagMedStatus(sammenligningsgrunnlagPrStatus)));

        return builder.build();
    }

    public static BeregningRefusjonOverstyringerEntitet mapRefusjonOverstyring(BeregningRefusjonOverstyringerDto refusjonOverstyringerFraKalkulus) {
        BeregningRefusjonOverstyringerEntitet.Builder entitetBuilder = BeregningRefusjonOverstyringerEntitet.builder();

        refusjonOverstyringerFraKalkulus.getRefusjonOverstyringer().forEach(beregningRefusjonOverstyring -> {
            BeregningRefusjonOverstyringEntitet entitet = new BeregningRefusjonOverstyringEntitet(KalkulatorTilIAYMapper.mapArbeidsgiver(beregningRefusjonOverstyring.getArbeidsgiver()), beregningRefusjonOverstyring.getFørsteMuligeRefusjonFom());
            entitetBuilder.leggTilOverstyring(entitet);
        });
        return entitetBuilder.build();
    }

    public static BeregningAktivitetAggregatEntitet mapSaksbehandletAktivitet(BeregningAktivitetAggregatDto saksbehandletAktiviteterFraKalkulus) {
        BeregningAktivitetAggregatEntitet.Builder entitetBuilder = BeregningAktivitetAggregatEntitet.builder();
        entitetBuilder.medSkjæringstidspunktOpptjening(saksbehandletAktiviteterFraKalkulus.getSkjæringstidspunktOpptjening());
        saksbehandletAktiviteterFraKalkulus.getBeregningAktiviteter().forEach(mapBeregningAktivitet(entitetBuilder));
        return entitetBuilder.build();
    }

    private static Consumer<BeregningAktivitetDto> mapBeregningAktivitet(BeregningAktivitetAggregatEntitet.Builder entitetBuilder) {
        return beregningAktivitet -> {
            BeregningAktivitetEntitet.Builder builder = BeregningAktivitetEntitet.builder();
            builder.medArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef() == null ? null : KalkulatorTilIAYMapper.mapArbeidsforholdRed(beregningAktivitet.getArbeidsforholdRef()));
            builder.medArbeidsgiver(beregningAktivitet.getArbeidsgiver() == null ? null : KalkulatorTilIAYMapper.mapArbeidsgiver(beregningAktivitet.getArbeidsgiver()));
            builder.medOpptjeningAktivitetType(OpptjeningAktivitetType.fraKode(beregningAktivitet.getOpptjeningAktivitetType().getKode()));
            builder.medPeriode(mapDatoIntervall(beregningAktivitet.getPeriode()));
            entitetBuilder.leggTilAktivitet(builder.build());
        };
    }

    public static BeregningAktivitetOverstyringerEntitet mapAktivitetOverstyring(BeregningAktivitetOverstyringerDto beregningAktivitetOverstyringerFraKalkulus) {
        BeregningAktivitetOverstyringerEntitet.Builder entitetBuilder = BeregningAktivitetOverstyringerEntitet.builder();
        beregningAktivitetOverstyringerFraKalkulus.getOverstyringer().forEach(overstyring -> {
            BeregningAktivitetOverstyringEntitet.Builder builder = BeregningAktivitetOverstyringEntitet.builder();
            builder.medArbeidsforholdRef(overstyring.getArbeidsforholdRef() == null ? null : KalkulatorTilIAYMapper.mapArbeidsforholdRed(overstyring.getArbeidsforholdRef()));
            overstyring.getArbeidsgiver().ifPresent(arbeidsgiver -> builder.medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(arbeidsgiver)));
            builder.medHandling(overstyring.getHandling() == null ? null : BeregningAktivitetHandlingType.fraKode(overstyring.getHandling().getKode()));
            builder.medOpptjeningAktivitetType(OpptjeningAktivitetType.fraKode(overstyring.getOpptjeningAktivitetType().getKode()));
            builder.medPeriode(mapDatoIntervall(overstyring.getPeriode()));
            entitetBuilder.leggTilOverstyring(builder.build());
        });
        return entitetBuilder.build();
    }

    private static IntervallEntitet mapDatoIntervall(Intervall periode) {
        return IntervallEntitet.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato());
    }

    public static BeregningsgrunnlagGrunnlagEntitet mapGrunnlag(Long koblingId, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagFraKalkulus, BeregningsgrunnlagTilstand tilstand) {
        BeregningsgrunnlagGrunnlagBuilder oppdatere = BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty());

        beregningsgrunnlagFraKalkulus.getBeregningsgrunnlag().ifPresent(beregningsgrunnlagDto -> oppdatere.medBeregningsgrunnlag(mapBeregningsgrunnlag(beregningsgrunnlagDto)));
        beregningsgrunnlagFraKalkulus.getOverstyring().ifPresent(beregningAktivitetOverstyringerDto -> oppdatere.medOverstyring(mapAktivitetOverstyring(beregningAktivitetOverstyringerDto)));
        oppdatere.medRegisterAktiviteter(mapRegisterAktiviteter(beregningsgrunnlagFraKalkulus.getRegisterAktiviteter()));
        beregningsgrunnlagFraKalkulus.getSaksbehandletAktiviteter().ifPresent(beregningAktivitetAggregatDto -> oppdatere.medSaksbehandletAktiviteter(mapSaksbehandletAktivitet(beregningAktivitetAggregatDto)));
        beregningsgrunnlagFraKalkulus.getRefusjonOverstyringer().ifPresent(beregningRefusjonOverstyringerDto -> oppdatere.medRefusjonOverstyring(mapRefusjonOverstyring(beregningRefusjonOverstyringerDto)));

        return oppdatere.build(koblingId, tilstand);
    }

    private static BeregningAktivitetAggregatEntitet mapRegisterAktiviteter(BeregningAktivitetAggregatDto registerAktiviteter) {
        BeregningAktivitetAggregatEntitet.Builder builder = BeregningAktivitetAggregatEntitet.builder();
        registerAktiviteter.getBeregningAktiviteter().forEach(mapBeregningAktivitet(builder));
        builder.medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
        return builder.build();
    }
}
