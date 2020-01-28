package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatusV2;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering extends MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL {

    private static Map<AktivitetStatusV2, AktivitetStatus> statusMap = new EnumMap<>(AktivitetStatusV2.class);
    private static Map<AktivitetStatus, OpptjeningAktivitetType> aktivitetTypeMap = new HashMap<>();

    static {
        statusMap.put(AktivitetStatusV2.SN, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        statusMap.put(AktivitetStatusV2.FL, AktivitetStatus.FRILANSER);
        aktivitetTypeMap.put(AktivitetStatus.FRILANSER, OpptjeningAktivitetType.FRILANS);
        aktivitetTypeMap.put(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, OpptjeningAktivitetType.NÆRING);
    }

    @Override
    protected void mapAndeler(BeregningsgrunnlagDto nyttBeregningsgrunnlag, SplittetPeriode splittetPeriode,
                              List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        andelListe.forEach(eksisterendeAndel -> mapEksisterendeAndel(splittetPeriode, beregningsgrunnlagPeriode, eksisterendeAndel));
        splittetPeriode.getNyeAndeler()
            .forEach(nyAndel -> mapNyAndelTaHensynTilSNFL(beregningsgrunnlagPeriode, nyttBeregningsgrunnlag.getSkjæringstidspunkt(), nyAndel));
    }

    private void mapNyAndelTaHensynTilSNFL(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, LocalDate skjæringstidspunkt, SplittetAndel nyAndel) {
        Intervall beregningsperiode;
        if (nyAndel.getAktivitetStatus() != null && AktivitetStatusV2.SN.equals(nyAndel.getAktivitetStatus())) {
            beregningsperiode = BeregningsperiodeTjeneste.fastsettBeregningsperiodeForSNAndeler(skjæringstidspunkt);
        } else {
            beregningsperiode = BeregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);
        }
        if (nyAndelErSNEllerFl(nyAndel)) {
            AktivitetStatus aktivitetStatus = mapAktivitetStatus(nyAndel.getAktivitetStatus());
            if (aktivitetStatus == null) {
                throw new IllegalStateException("Klarte ikke identifisere aktivitetstatus under periodesplitt. Status var " + nyAndel.getAktivitetStatus());
            }
            boolean eksisterende = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .anyMatch(a -> a.getAktivitetStatus().equals(aktivitetStatus) && a.getArbeidsforholdType().equals(aktivitetTypeMap.get(aktivitetStatus)) &&
                    a.getBeregningsperiodeFom().equals(beregningsperiode.getFomDato()) && a.getBeregningsperiodeTom().equals(beregningsperiode.getTomDato()));
            if (!eksisterende) {
                BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                    .medAktivitetStatus(aktivitetStatus)
                    .medArbforholdType(aktivitetTypeMap.get(aktivitetStatus))
                    .medBeregningsperiode(beregningsperiode.getFomDato(), beregningsperiode.getTomDato())
                    .build(beregningsgrunnlagPeriode);
            }
        } else {
            Arbeidsgiver arbeidsgiver = MapArbeidsforholdFraRegelTilVL.map(nyAndel.getArbeidsforhold());
            InternArbeidsforholdRefDto iaRef = InternArbeidsforholdRefDto.ref(nyAndel.getArbeidsforhold().getArbeidsforholdId());
            BGAndelArbeidsforholdDto.Builder andelArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(iaRef)
                .medArbeidsperiodeFom(nyAndel.getArbeidsperiodeFom())
                .medArbeidsperiodeTom(nyAndel.getArbeidsperiodeTom())
                .medRefusjonskravPrÅr(nyAndel.getRefusjonskravPrÅr());
            BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(andelArbeidsforholdBuilder)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                .medBeregningsperiode(beregningsperiode.getFomDato(), beregningsperiode.getTomDato())
                .build(beregningsgrunnlagPeriode);
        }
    }

    private AktivitetStatus mapAktivitetStatus(AktivitetStatusV2 aktivitetStatusV2) {
        if (aktivitetStatusV2 == null) {
            return null;
        }
        var status = statusMap.get(aktivitetStatusV2);
        if (status == null) {
            throw new IllegalStateException(
                "Mangler mapping til " + AktivitetStatus.class.getName() + " fra " + AktivitetStatusV2.class.getName() + "." + aktivitetStatusV2);
        }
        return status;
    }

    private boolean nyAndelErSNEllerFl(SplittetAndel nyAndel) {
        return nyAndel.getAktivitetStatus() != null
            && (nyAndel.getAktivitetStatus().equals(AktivitetStatusV2.SN) || nyAndel.getAktivitetStatus().equals(AktivitetStatusV2.FL));
    }

    private void mapEksisterendeAndel(SplittetPeriode splittetPeriode, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                      BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(eksisterendeAndel);
        Optional<BeregningsgrunnlagPrArbeidsforhold> regelMatchOpt = finnEksisterendeAndelFraRegel(splittetPeriode, eksisterendeAndel);
        regelMatchOpt.ifPresent(regelAndel -> {
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdDtoBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder();
            BGAndelArbeidsforholdDto.Builder andelArbeidsforholdBuilder = bgAndelArbeidsforholdDtoBuilder
                .medRefusjonskravPrÅr(regelAndel.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO));
            andelBuilder.medBGAndelArbeidsforhold(andelArbeidsforholdBuilder);
        });

        andelBuilder
            .build(beregningsgrunnlagPeriode);
    }

}
