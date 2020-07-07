package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;


import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapAktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapInntektskategoriRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapOpptjeningAktivitetFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapPeriodeÅrsakFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;

public class MapBeregningsgrunnlagFraRegelTilVL {
    private static final Map<AktivitetStatus, SammenligningsgrunnlagType> AKTIVITETSTATUS_SAMMENLIGNINGSGRUNNLAGTYPE_MAP = Map.of(
            AktivitetStatus.ATFL_SN, SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN,
            AktivitetStatus.AT, SammenligningsgrunnlagType.SAMMENLIGNING_AT,
            AktivitetStatus.FL, SammenligningsgrunnlagType.SAMMENLIGNING_FL,
            AktivitetStatus.SN, SammenligningsgrunnlagType.SAMMENLIGNING_SN
    );

    protected enum Steg {
        FORESLÅ,
        FORDEL,
        FASTSETT,
    }

    public BeregningsgrunnlagDto mapForeslåBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatGrunnlag,
                                                                     List<RegelResultat> regelResultater, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        return map(resultatGrunnlag, regelResultater, eksisterendeVLGrunnlag, Steg.FORESLÅ);
    }

    public BeregningsgrunnlagDto mapFastsettBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatGrunnlag,
                                                                      List<RegelResultat> regelResultater, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        return map(resultatGrunnlag, regelResultater, eksisterendeVLGrunnlag, Steg.FASTSETT);
    }

    public static BeregningsgrunnlagDto mapVurdertBeregningsgrunnlag(List<RegelResultat> regelResultater, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(eksisterendeVLGrunnlag).build();
        Iterator<RegelResultat> regelResultatIterator = regelResultater.iterator();
        for (var periode : nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            RegelResultat regelResultat = regelResultatIterator.next();
            BeregningsgrunnlagPeriodeDto.oppdater(periode)
                    .medRegelEvalueringVilkårsvurdering(regelResultat.getRegelSporing().getInput(), regelResultat.getRegelSporing().getSporing())
                    .build(nyttBeregningsgrunnlag);
        }
        return nyttBeregningsgrunnlag;
    }

    private BeregningsgrunnlagDto map(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatGrunnlag, List<RegelResultat> regelResultater, BeregningsgrunnlagDto eksisterendeVLGrunnlag, Steg steg) {
        mapSammenligningsgrunnlag(resultatGrunnlag.getSammenligningsGrunnlag(), eksisterendeVLGrunnlag);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(eksisterendeVLGrunnlag).build();
        if (nyttBeregningsgrunnlag.getSammenligningsgrunnlagPrStatusListe().isEmpty()) {
            mapSammenligningsgrunnlagPrStatus(resultatGrunnlag.getSammenligningsGrunnlagPrAktivitetstatus(), nyttBeregningsgrunnlag);
        }

        Objects.requireNonNull(resultatGrunnlag, "resultatGrunnlag");
        Objects.requireNonNull(regelResultater, "regelResultater");
        if (resultatGrunnlag.getBeregningsgrunnlagPerioder().size() != regelResultater.size()) {
            throw new IllegalArgumentException("Antall beregningsresultatperioder ("
                    + resultatGrunnlag.getBeregningsgrunnlagPerioder().size()
                    + ") må være samme som antall regelresultater ("
                    + regelResultater.size() + ")");
        }
        MapAktivitetStatusMedHjemmel.mapAktivitetStatusMedHjemmel(resultatGrunnlag.getAktivitetStatuser(), nyttBeregningsgrunnlag, resultatGrunnlag.getBeregningsgrunnlagPerioder().get(0));

        mapPerioder(regelResultater, nyttBeregningsgrunnlag, steg, resultatGrunnlag.getBeregningsgrunnlagPerioder());

        return nyttBeregningsgrunnlag;
    }

    protected void mapPerioder(List<RegelResultat> regelResultater, BeregningsgrunnlagDto eksisterendeVLGrunnlag, Steg steg, List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder) {
        Iterator<RegelResultat> resultat = regelResultater.iterator();

        int vlBGnummer = 0;
        for (var resultatBGPeriode : beregningsgrunnlagPerioder) {

            RegelResultat regelResultat = resultat.next();
            BeregningsgrunnlagPeriodeDto eksisterendePeriode = (vlBGnummer < eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().size())
                    ? eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().get(vlBGnummer)
                    : null;
            BeregningsgrunnlagPeriodeDto mappetPeriode = mapBeregningsgrunnlagPeriode(resultatBGPeriode, regelResultat, eksisterendePeriode, eksisterendeVLGrunnlag, steg);
            for (BeregningsgrunnlagPrStatus regelAndel : resultatBGPeriode.getBeregningsgrunnlagPrStatus()) {
                if (regelAndel.getAndelNr() == null) {
                    mapAndelMedArbeidsforhold(mappetPeriode, regelAndel);
                } else {
                    mapAndel(mappetPeriode, regelAndel, steg);
                }
            }
            vlBGnummer++;
            fastsettAgreggerteVerdier(mappetPeriode, eksisterendeVLGrunnlag);
        }
    }

    private static void mapAndel(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel, Steg steg) {
        mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndel.getAndelNr().equals(bgpsa.getAndelsnr()))
                .forEach(resultatAndel -> mapBeregningsgrunnlagPrStatus(mappetPeriode, regelAndel, resultatAndel, steg));
    }

    protected void mapAndelMedArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold : regelAndel.getArbeidsforhold()) {
            mapEksisterendeAndelForArbeidsforhold(mappetPeriode, regelAndel, regelAndelForArbeidsforhold);
        }
    }

    protected static void mapEksisterendeAndelForArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel, BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelOpt = mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndelForArbeidsforhold.getAndelNr().equals(bgpsa.getAndelsnr()))
                .findFirst();
        if (andelOpt.isPresent()) {
            BeregningsgrunnlagPrStatusOgAndelDto resultatAndel = andelOpt.get();
            mapBeregningsgrunnlagPrStatusForATKombinert(mappetPeriode, regelAndel, resultatAndel);
        }
    }

    private static void fastsettAgreggerteVerdier(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        Optional<BigDecimal> bruttoPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> bgpsa.getBruttoPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .reduce(BigDecimal::add);
        Optional<BigDecimal> avkortetPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> bgpsa.getAvkortetPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getAvkortetPrÅr)
                .reduce(BigDecimal::add);
        Optional<BigDecimal> redusertPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> bgpsa.getRedusertPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getRedusertPrÅr)
                .reduce(BigDecimal::add);
        BeregningsgrunnlagPeriodeDto.oppdater(periode)
                .medBruttoPrÅr(bruttoPrÅr.orElse(null))
                .medAvkortetPrÅr(avkortetPrÅr.orElse(null))
                .medRedusertPrÅr(redusertPrÅr.orElse(null))
                .build(eksisterendeVLGrunnlag);
    }

    private static void mapBeregningsgrunnlagPrStatusForATKombinert(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                                    BeregningsgrunnlagPrStatus resultatBGPStatus,
                                                                    BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold arbeidsforhold : resultatBGPStatus.getArbeidsforhold()) {
            if (gjelderSammeAndel(vlBGPAndel, arbeidsforhold)) {
                BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(vlBGPAndel));
                andelBuilder = settFasteVerdier(andelBuilder, arbeidsforhold);
                if (skalByggeBGArbeidsforhold(arbeidsforhold, vlBGPAndel)) {
                    BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = mapArbeidsforhold(vlBGPAndel, arbeidsforhold);
                    andelBuilder.medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
                }
                andelBuilder
                        .build(vlBGPeriode);
                return;
            }
        }
    }

    private static BGAndelArbeidsforholdDto.Builder mapArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel,
                                                                      BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return BGAndelArbeidsforholdDto.Builder.oppdater(vlBGPAndel.getBgAndelArbeidsforhold())
                .medNaturalytelseBortfaltPrÅr(arbeidsforhold.getNaturalytelseBortfaltPrÅr().orElse(null))
                .medNaturalytelseTilkommetPrÅr(arbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(null))
                .medRefusjonskravPrÅr(arbeidsforhold.getRefusjonskravPrÅr().orElse(null));
    }

    protected static BeregningsgrunnlagPrStatusOgAndelDto.Builder settFasteVerdier(BeregningsgrunnlagPrStatusOgAndelDto.Builder builder, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        if (arbeidsforhold.getBeregningsperiode() != null && arbeidsforhold.getBeregningsperiode().getFom() != null) {
            builder.medBeregningsperiode(arbeidsforhold.getBeregningsperiode().getFom(), arbeidsforhold.getBeregningsperiode().getTom());
        }
        return builder
                .medBeregnetPrÅr(verifisertBeløp(arbeidsforhold.getBeregnetPrÅr()))
                .medOverstyrtPrÅr(verifisertBeløp(arbeidsforhold.getOverstyrtPrÅr()))
                .medFordeltPrÅr(verifisertBeløp(arbeidsforhold.getFordeltPrÅr()))
                .medAvkortetPrÅr(verifisertBeløp(arbeidsforhold.getAvkortetPrÅr()))
                .medRedusertPrÅr(verifisertBeløp(arbeidsforhold.getRedusertPrÅr()))
                .medMaksimalRefusjonPrÅr(arbeidsforhold.getMaksimalRefusjonPrÅr())
                .medAvkortetRefusjonPrÅr(arbeidsforhold.getAvkortetRefusjonPrÅr())
                .medRedusertRefusjonPrÅr(arbeidsforhold.getRedusertRefusjonPrÅr())
                .medAvkortetBrukersAndelPrÅr(verifisertBeløp(arbeidsforhold.getAvkortetBrukersAndelPrÅr()))
                .medRedusertBrukersAndelPrÅr(verifisertBeløp(arbeidsforhold.getRedusertBrukersAndelPrÅr()))
                .medFastsattAvSaksbehandler(arbeidsforhold.getFastsattAvSaksbehandler())
                .medLagtTilAvSaksbehandler(arbeidsforhold.getLagtTilAvSaksbehandler())
                .medArbforholdType(MapOpptjeningAktivitetFraRegelTilVL.map(arbeidsforhold.getArbeidsforhold().getAktivitet()))
                .medInntektskategori(MapInntektskategoriRegelTilVL.map(arbeidsforhold.getInntektskategori()));
    }

    private static boolean skalByggeBGArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel) {
        return vlBGPAndel.getBgAndelArbeidsforhold().isPresent() &&
                (arbeidsforhold.getNaturalytelseBortfaltPrÅr().isPresent()
                        || arbeidsforhold.getNaturalytelseTilkommetPrÅr().isPresent()
                || arbeidsforhold.getRefusjonskravPrÅr().isPresent());
    }

    private static BigDecimal verifisertBeløp(BigDecimal beløp) {
        return beløp == null ? null : beløp.max(BigDecimal.ZERO);
    }

    private static boolean gjelderSammeAndel(BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        if (vlBGPAndel.getAndelsnr() != null && arbeidsforhold.getAndelNr() != null) {
            return vlBGPAndel.getAndelsnr().equals(arbeidsforhold.getAndelNr());
        }
        if (vlBGPAndel.getAktivitetStatus().erFrilanser()) {
            return arbeidsforhold.erFrilanser();
        }
        if (arbeidsforhold.erFrilanser()) {
            return false;
        }
        if (!vlBGPAndel.getInntektskategori().equals(MapInntektskategoriRegelTilVL.map(arbeidsforhold.getInntektskategori()))) {
            return false;
        }
        if (!matcherArbeidsgivere(vlBGPAndel, arbeidsforhold)) {
            return false;
        }
        if (!matcherOpptjeningsaktivitet(vlBGPAndel, arbeidsforhold)) {
            return false;
        }
        return vlBGPAndel.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .filter(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold)
                .map(ref -> Objects.equals(ref, InternArbeidsforholdRefDto.ref(arbeidsforhold.getArbeidsforhold().getArbeidsforholdId())))
                .orElse(arbeidsforhold.getArbeidsforhold().getArbeidsforholdId() == null);
    }

    private static boolean matcherOpptjeningsaktivitet(BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        if (arbeidsforhold.getArbeidsforhold() != null) {
            return Objects.equals(vlBGPAndel.getArbeidsforholdType(), MapOpptjeningAktivitetFraRegelTilVL.map(arbeidsforhold.getArbeidsforhold().getAktivitet()));
        }
        return vlBGPAndel.getArbeidsforholdType() == null;
    }

    private static boolean matcherArbeidsgivere(BeregningsgrunnlagPrStatusOgAndelDto andel, BeregningsgrunnlagPrArbeidsforhold forhold) {
        Arbeidsgiver arbeidsgiver = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver).orElse(null);
        if (forhold.getArbeidsgiverId() == null) {
            return arbeidsgiver == null;
        } else
            return arbeidsgiver != null && Objects.equals(forhold.getArbeidsgiverId(), arbeidsgiver.getIdentifikator());
    }

    private static void mapBeregningsgrunnlagPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                      BeregningsgrunnlagPrStatus resultatBGPStatus,
                                                      BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatusOgAndel,
                                                      Steg steg) {
        boolean gjelderForeslå = steg.equals(Steg.FORESLÅ);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(vlBGPStatusOgAndel));
        if (resultatBGPStatus.getBeregningsperiode() != null && resultatBGPStatus.getBeregningsperiode().getFom() != null) {
            builder.medBeregningsperiode(resultatBGPStatus.getBeregningsperiode().getFom(), resultatBGPStatus.getBeregningsperiode().getTom());
        }

        builder
                .medBeregnetPrÅr(verifisertBeløp(resultatBGPStatus.getBeregnetPrÅr()))
                .medOverstyrtPrÅr(verifisertBeløp(resultatBGPStatus.getOverstyrtPrÅr()))
                .medFordeltPrÅr(verifisertBeløp(resultatBGPStatus.getFordeltPrÅr()))
                .medAvkortetPrÅr(verifisertBeløp(resultatBGPStatus.getAvkortetPrÅr()))
                .medRedusertPrÅr(verifisertBeløp(resultatBGPStatus.getRedusertPrÅr()))
                .medAvkortetBrukersAndelPrÅr(gjelderForeslå ? null : verifisertBeløp(resultatBGPStatus.getAvkortetPrÅr()))
                .medRedusertBrukersAndelPrÅr(gjelderForeslå ? null : verifisertBeløp(resultatBGPStatus.getRedusertPrÅr()))
                .medMaksimalRefusjonPrÅr(gjelderForeslå ? null : BigDecimal.ZERO)
                .medAvkortetRefusjonPrÅr(gjelderForeslå ? null : BigDecimal.ZERO)
                .medRedusertRefusjonPrÅr(gjelderForeslå ? null : BigDecimal.ZERO)

                .medPgi(resultatBGPStatus.getGjennomsnittligPGI(), resultatBGPStatus.getPgiListe())
                .medÅrsbeløpFraTilstøtendeYtelse(resultatBGPStatus.getÅrsbeløpFraTilstøtendeYtelse())
                .medNyIArbeidslivet(resultatBGPStatus.getNyIArbeidslivet())
                .medInntektskategori(MapInntektskategoriRegelTilVL.map(resultatBGPStatus.getInntektskategori()))
                .medFastsattAvSaksbehandler(resultatBGPStatus.erFastsattAvSaksbehandler())
                .medLagtTilAvSaksbehandler(resultatBGPStatus.erLagtTilAvSaksbehandler())
                .medBesteberegningPrÅr(resultatBGPStatus.getBesteberegningPrÅr())
                .medOrginalDagsatsFraTilstøtendeYtelse(resultatBGPStatus.getOrginalDagsatsFraTilstøtendeYtelse())
                .build(vlBGPeriode);
    }

    private static BeregningsgrunnlagPeriodeDto mapBeregningsgrunnlagPeriode(final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatGrunnlagPeriode,
                                                                             RegelResultat regelResultat,
                                                                             final BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                                             BeregningsgrunnlagDto eksisterendeVLGrunnlag,
                                                                             Steg steg) {
        if (vlBGPeriode == null) {
            BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.builder()
                    .medBeregningsgrunnlagPeriode(
                            resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(),
                            resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getTomOrNull()
                    )
                    .leggTilPeriodeÅrsaker(mapPeriodeÅrsaker(resultatGrunnlagPeriode.getPeriodeÅrsaker()));
            leggTilRegelEvaluering(regelResultat, steg, builder);
            BeregningsgrunnlagPeriodeDto periode = builder
                    .build(eksisterendeVLGrunnlag);
            opprettBeregningsgrunnlagPrStatusOgAndel(eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().get(0), periode);
            return periode;
        }
        BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.oppdater(vlBGPeriode)
                .medBeregningsgrunnlagPeriode(
                        resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(),
                        resultatGrunnlagPeriode.getBeregningsgrunnlagPeriode().getTomOrNull()
                )
                .tilbakestillPeriodeÅrsaker()
                .leggTilPeriodeÅrsaker(mapPeriodeÅrsaker(resultatGrunnlagPeriode.getPeriodeÅrsaker()));
        BeregningsgrunnlagPeriodeDto.Builder periodeBuilderMedRegel = leggTilRegelEvaluering(regelResultat, steg, periodeBuilder);
        regelResultat.getRegelSporingFinnGrenseverdi()
                .ifPresent(regelSporing -> periodeBuilderMedRegel.medRegelEvalueringFinnGrenseverdi(regelSporing.getInput(), regelSporing.getSporing()));
        periodeBuilder.build(eksisterendeVLGrunnlag);
        return vlBGPeriode;
    }

    private static BeregningsgrunnlagPeriodeDto.Builder leggTilRegelEvaluering(RegelResultat regelResultat, Steg steg, BeregningsgrunnlagPeriodeDto.Builder periodeBuilder) {
        if (steg.equals(Steg.FORESLÅ)) {
            periodeBuilder.medRegelEvalueringForeslå(regelResultat.getRegelSporing().getInput(), regelResultat.getRegelSporing().getSporing());
        } else if (steg.equals(Steg.FORDEL)) {
            periodeBuilder.medRegelEvalueringFordel(regelResultat.getRegelSporing().getInput(), regelResultat.getRegelSporing().getSporing());
        } else {
            periodeBuilder.medRegelEvalueringFastsett(regelResultat.getRegelSporing().getInput(), regelResultat.getRegelSporing().getSporing());
        }
        return periodeBuilder;
    }

    private static void opprettBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPeriodeDto kopierFra, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        kopierFra.getBeregningsgrunnlagPrStatusOgAndelList().forEach(bgpsa -> {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medArbforholdType(bgpsa.getArbeidsforholdType())
                    .medAktivitetStatus(bgpsa.getAktivitetStatus())
                    .medInntektskategori(bgpsa.getInntektskategori());
            Optional<Arbeidsgiver> arbeidsgiver = bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver);
            Optional<InternArbeidsforholdRefDto> arbeidsforholdRef = bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef);
            if (arbeidsgiver.isPresent() || arbeidsforholdRef.isPresent()) {
                BGAndelArbeidsforholdDto arbeidsforhold = bgpsa.getBgAndelArbeidsforhold().get();
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(arbeidsgiver.orElse(null))
                        .medArbeidsforholdRef(arbeidsforholdRef.orElse(null))
                        .medArbeidsperiodeFom(arbeidsforhold.getArbeidsperiodeFom())
                        .medArbeidsperiodeTom(arbeidsforhold.getArbeidsperiodeTom().orElse(null))
                        .medHjemmel(arbeidsforhold.getHjemmelForRefusjonskravfrist());
                andelBuilder
                        .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
            }
            andelBuilder.build(beregningsgrunnlagPeriode);
        });
    }

    private static void mapSammenligningsgrunnlag(final SammenligningsGrunnlag resultatSammenligningsGrunnlag, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        if (resultatSammenligningsGrunnlag != null) {
            SammenligningsgrunnlagDto.Builder builder = SammenligningsgrunnlagDto.builder()
                    .medSammenligningsperiode(
                            resultatSammenligningsGrunnlag.getSammenligningsperiode().getFom(),
                            resultatSammenligningsGrunnlag.getSammenligningsperiode().getTom()
                    )
                    .medRapportertPrÅr(resultatSammenligningsGrunnlag.getRapportertPrÅr())
                    .medAvvikPromilleNy(resultatSammenligningsGrunnlag.getAvvikPromilleUtenAvrunding())
                    .medAvvikPromille(resultatSammenligningsGrunnlag.getAvvikPromille());
            BeregningsgrunnlagDto.Builder.oppdater(Optional.of(eksisterendeVLGrunnlag))
                    .medSammenligningsgrunnlag(builder);
        }
    }

    private static void mapSammenligningsgrunnlagPrStatus(final EnumMap<AktivitetStatus, SammenligningsGrunnlag> sammenligningsgrunnlag,
                                                          BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        sammenligningsgrunnlag.entrySet().forEach(s -> {
            if (!AKTIVITETSTATUS_SAMMENLIGNINGSGRUNNLAGTYPE_MAP.containsKey(s.getKey())) {
                throw new IllegalArgumentException("Finner ingen mapping mellom AktivitetStatus " + s.getKey() + " og SammenligningsgrunnlagType");
            }
            BeregningsgrunnlagDto.Builder.oppdater(Optional.of(eksisterendeVLGrunnlag)).leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusDto.builder()
                    .medSammenligningsperiode(s.getValue().getSammenligningsperiode().getFom(), s.getValue().getSammenligningsperiode().getTom())
                    .medRapportertPrÅr(s.getValue().getRapportertPrÅr())
                    .medAvvikPromilleNy(s.getValue().getAvvikPromilleUtenAvrunding())
                    .medSammenligningsgrunnlagType(AKTIVITETSTATUS_SAMMENLIGNINGSGRUNNLAGTYPE_MAP.get(s.getKey())));
        });
    }

    private static List<PeriodeÅrsak> mapPeriodeÅrsaker(List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak> periodeÅrsaker) {
        return periodeÅrsaker.stream()
                .map(MapPeriodeÅrsakFraRegelTilVL::map)
                .collect(Collectors.toList());
    }
}
