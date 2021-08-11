package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.felles.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapHjemmelFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering extends MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL {

    private static Map<AktivitetStatusV2, AktivitetStatus> statusMap = new EnumMap<>(AktivitetStatusV2.class);
    private static Map<AktivitetStatus, OpptjeningAktivitetType> aktivitetTypeMap = new HashMap<>();

    static {
        statusMap.put(AktivitetStatusV2.SN, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        statusMap.put(AktivitetStatusV2.FL, AktivitetStatus.FRILANSER);
        statusMap.put(AktivitetStatusV2.DP, AktivitetStatus.DAGPENGER);
        aktivitetTypeMap.put(AktivitetStatus.FRILANSER, OpptjeningAktivitetType.FRILANS);
        aktivitetTypeMap.put(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, OpptjeningAktivitetType.NÆRING);
    }

    @Override
    protected void mapAndeler(BeregningsgrunnlagDto nyttBeregningsgrunnlag, SplittetPeriode splittetPeriode,
                              List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        andelListe.forEach(eksisterendeAndel -> mapEksisterendeAndel(splittetPeriode, beregningsgrunnlagPeriode, eksisterendeAndel));
        splittetPeriode.getNyeAndeler()
                .forEach(nyAndel -> mapNyAndel(beregningsgrunnlagPeriode, nyttBeregningsgrunnlag.getSkjæringstidspunkt(), nyAndel));
    }

    private void mapNyAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, LocalDate skjæringstidspunkt, SplittetAndel nyAndel) {
        Intervall beregningsperiode;
        // Antar at vi ikkje får nye andeler for ytelse FRISINN
        BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();
        if (nyAndel.getAktivitetStatus() != null && AktivitetStatusV2.SN.equals(nyAndel.getAktivitetStatus())) {
            beregningsperiode = beregningsperiodeTjeneste.fastsettBeregningsperiodeForSNAndeler(skjæringstidspunkt);
        } else {
            beregningsperiode = beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);
        }
        if (nyAndelErSNFlDP(nyAndel)) {
            AktivitetStatus aktivitetStatus = mapAktivitetStatus(nyAndel.getAktivitetStatus());
            if (aktivitetStatus == null) {
                throw new IllegalStateException("Klarte ikke identifisere aktivitetstatus under periodesplitt. Status var " + nyAndel.getAktivitetStatus());
            }
            boolean eksisterende = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .anyMatch(a -> a.getAktivitetStatus().equals(aktivitetStatus) && a.getArbeidsforholdType().equals(aktivitetTypeMap.get(aktivitetStatus)) &&
                            a.getBeregningsperiodeFom().equals(beregningsperiode.getFomDato()) && a.getBeregningsperiodeTom().equals(beregningsperiode.getTomDato()));
            if (!eksisterende) {
                BeregningsgrunnlagPrStatusOgAndelDto.ny()
                        .medKilde(AndelKilde.PROSESS_PERIODISERING)
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
                    .medRefusjonskravPrÅr(nyAndel.getRefusjonskravPrÅr())
                    .medHjemmel(MapHjemmelFraRegelTilVL.map(nyAndel.getAnvendtRefusjonskravfristHjemmel()));
            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medKilde(AndelKilde.PROSESS_PERIODISERING)
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

    private boolean nyAndelErSNFlDP(SplittetAndel nyAndel) {
        return nyAndel.getAktivitetStatus() != null
                && (nyAndel.getAktivitetStatus().equals(AktivitetStatusV2.SN)
                || nyAndel.getAktivitetStatus().equals(AktivitetStatusV2.FL)
                || nyAndel.getAktivitetStatus().equals(AktivitetStatusV2.DP));
    }

    private void mapEksisterendeAndel(SplittetPeriode splittetPeriode, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                      BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(eksisterendeAndel);
        Optional<EksisterendeAndel> regelMatchOpt = finnEksisterendeAndelFraRegel(splittetPeriode, eksisterendeAndel);
        regelMatchOpt.ifPresent(regelAndel -> {
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdDtoBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder();
            BGAndelArbeidsforholdDto.Builder andelArbeidsforholdBuilder = bgAndelArbeidsforholdDtoBuilder
                    .medRefusjonskravPrÅr(regelAndel.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO))
                    .medHjemmel(MapHjemmelFraRegelTilVL.map(regelAndel.getAnvendtRefusjonskravfristHjemmel()));
            andelBuilder.medBGAndelArbeidsforhold(andelArbeidsforholdBuilder);
        });

        andelBuilder
                .build(beregningsgrunnlagPeriode);
    }

}
