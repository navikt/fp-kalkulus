package no.nav.folketrygdloven.kalkulus.beregning;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.migrering.AvklaringsbehovMigreringDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AndelArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAndelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegningInntektEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegningMånedsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegninggrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.FastsattInntektskategori;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Refusjon;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelsporingRepository;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.migrering.BGAndelArbeidsforholdMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BaseMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningAktivitetAggregatMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningAktivitetMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningAktivitetOverstyringMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningAktivitetOverstyringerMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningRefusjonOverstyringMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningRefusjonOverstyringerMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningRefusjonPeriodeMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningsgrunnlagAktivitetStatusMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningsgrunnlagGrunnlagMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningsgrunnlagMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningsgrunnlagPeriodeMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BeregningsgrunnlagPrStatusOgAndelMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BesteberegningInntektMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BesteberegningMånedsgrunnlagMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.BesteberegninggrunnlagMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.FaktaAggregatMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.FaktaAktørMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.FaktaArbeidsforholdMigreringDto;
import no.nav.folketrygdloven.kalkulus.migrering.SammenligningsgrunnlagPrStatusMigreringDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class MigreringTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(MigreringTjeneste.class);

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private RegelsporingRepository regelsporingRepository;
    private AvklaringsbehovRepository avklaringsbehovRepository;

    public MigreringTjeneste() {
        // CDI
    }

    @Inject
    public MigreringTjeneste(BeregningsgrunnlagRepository repository,
                             RegelsporingRepository regelsporingRepository,
                             AvklaringsbehovRepository avklaringsbehovRepository) {
        this.beregningsgrunnlagRepository = repository;
        this.regelsporingRepository = regelsporingRepository;
        this.avklaringsbehovRepository = avklaringsbehovRepository;
    }

    public void ryddGrunnlagAvklaringsbehovOgRegelsporing(KoblingEntitet koblingEntitet) {
        LOG.info("Rydder aktivt grunnlag og regelsporinger på kobling {} med referanse {} pga ny migrering", koblingEntitet.getId(), koblingEntitet.getKoblingReferanse());
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingEntitet.getId());
        regelsporingRepository.slettAlleRegelsporinger(koblingEntitet.getId());
        avklaringsbehovRepository.slettAlleAvklaringsbehovForKobling(koblingEntitet.getId());
    }

    public Migreringsresultat mapOgLagreGrunnlag(KoblingEntitet koblingEntitet, BeregningsgrunnlagGrunnlagMigreringDto dto) {
        // Beregningsgrunnlag
        var grunnlag = mapGrunnlag(koblingEntitet, dto);
        var entitet = beregningsgrunnlagRepository.lagreMigrering(koblingEntitet.getId(), grunnlag);

        // Regelsporinger
        regelsporingRepository.migrerSporinger(dto.getGrunnlagsporinger(), dto.getPeriodesporinger(), koblingEntitet.getId());

        // Avklaringsbeov
        var avklaringsbehov = dto.getAvklaringsbehov().stream().map(a -> mapAvklaringsbehov(a, koblingEntitet)).toList();
        LOG.info("Lagrer {} avklaringsbehov på koblingId {}, koblingReferanse {} under migrering", avklaringsbehov.size(), koblingEntitet.getId(), koblingEntitet.getKoblingReferanse());
        avklaringsbehov.forEach(a -> avklaringsbehovRepository.lagre(a));

        var grunnlagSporinger = regelsporingRepository.hentAlleRegelSporingGrunnlag(koblingEntitet.getId());
        var periodeSporinger = regelsporingRepository.hentAlleRegelSporingPeriode(koblingEntitet.getId());
        return new Migreringsresultat(entitet, grunnlagSporinger, periodeSporinger, avklaringsbehov);
    }

    private AvklaringsbehovEntitet mapAvklaringsbehov(AvklaringsbehovMigreringDto avklaringsbehovMigreringDto, KoblingEntitet kobling) {
        var avklaringsbehovEntitet = new AvklaringsbehovEntitet(kobling, avklaringsbehovMigreringDto.getDefinisjon(),
            avklaringsbehovMigreringDto.getStatus(), avklaringsbehovMigreringDto.getBegrunnelse(), avklaringsbehovMigreringDto.getErTrukket(),
            avklaringsbehovMigreringDto.getVurdertAv(), avklaringsbehovMigreringDto.getVurdertTidspunkt());
        settOpprettetOgEndretFelter(avklaringsbehovEntitet, avklaringsbehovMigreringDto);
        return avklaringsbehovEntitet;
    }

    private BeregningsgrunnlagGrunnlagEntitet mapGrunnlag(KoblingEntitet koblingEntitet, BeregningsgrunnlagGrunnlagMigreringDto dto) {
        var builder = BeregningsgrunnlagGrunnlagBuilder.nytt();
        mapFaktaAggregat(dto.getFaktaAggregat()).ifPresent(builder::medFaktaAggregat);
        mapAktivitetOverstyringer(dto.getOverstyringer(), dto.getRegisterAktiviteter()).ifPresent(builder::medOverstyring);
        mapAktiviteter(dto.getRegisterAktiviteter()).ifPresent(builder::medRegisterAktiviteter);
        mapAktiviteter(dto.getSaksbehandletAktiviteter()).ifPresent(builder::medSaksbehandletAktiviteter);
        mapRefusjonoverstyringer(dto.getRefusjonOverstyringer()).ifPresent(builder::medRefusjonOverstyring);
        mapBeregningsgrunnlag(dto.getBeregningsgrunnlag()).ifPresent(builder::medBeregningsgrunnlag);
        var entitet = builder.build(koblingEntitet.getId(), dto.getBeregningsgrunnlagTilstand());
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private Optional<BeregningsgrunnlagEntitet> mapBeregningsgrunnlag(BeregningsgrunnlagMigreringDto dto) {
        if (dto == null) {
            return Optional.empty();
        }
        var builder = BeregningsgrunnlagEntitet.builder()
            .medOverstyring(dto.isOverstyrt())
            .medGrunnbeløp(mapBeløp(dto.getGrunnbeløp()))
            .medSkjæringstidspunkt(dto.getSkjæringstidspunkt());
        mapAktivitetstatuser(dto.getAktivitetStatuser()).forEach(builder::leggTilAktivitetstatus);
        mapBesteberegningGrunnlag(dto.getBesteberegninggrunnlag()).ifPresent(builder::medBesteberegninggrunnlag);
        mapSammenligningsgrunnlagListe(dto.getSammenligningsgrunnlagPrStatusListe()).forEach(builder::leggTilSammenligningsgrunnlag);
        mapFaktaTilfeller(dto.getFaktaOmBeregningTilfeller()).forEach(builder::leggTilFaktaTilfelle);
        mapBeregningsgrunnlagPerioder(dto.getBeregningsgrunnlagPerioder()).forEach(builder::leggTilBeregningsgrunnlagPeriode);
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return Optional.of(entitet);
    }

    private List<BeregningsgrunnlagPeriodeEntitet> mapBeregningsgrunnlagPerioder(List<BeregningsgrunnlagPeriodeMigreringDto> dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        return dto.stream().map(this::mapBeregningsgrunnlagperiode).toList();
    }

    private BeregningsgrunnlagPeriodeEntitet mapBeregningsgrunnlagperiode(BeregningsgrunnlagPeriodeMigreringDto dto) {
        var builder = BeregningsgrunnlagPeriodeEntitet.builder()
            .medBruttoPrÅr(mapBeløp(dto.getBruttoPrÅr()))
            .medAvkortetPrÅr(mapBeløp(dto.getAvkortetPrÅr()))
            .medRedusertPrÅr(mapBeløp(dto.getRedusertPrÅr()))
            .medBeregningsgrunnlagPeriode(dto.getPeriode().getFom(), dto.getPeriode().getTom());
        dto.getBeregningsgrunnlagPeriodeÅrsaker().forEach(builder::leggTilPeriodeÅrsak);
        dto.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(this::mapBeregningsgrunnlagAndel)
            .forEach(builder::leggTilBeregningsgrunnlagAndel);
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private BeregningsgrunnlagAndelEntitet mapBeregningsgrunnlagAndel(BeregningsgrunnlagPrStatusOgAndelMigreringDto dto) {
        var builder = BeregningsgrunnlagAndelEntitet.builder()
            .medGrunnlagPrÅr(mapÅrsgrunnlag(dto))
            .medAndelsnr(dto.getAndelsnr())
            .medArbforholdType(dto.getArbeidsforholdType())
            .medAktivitetStatus(dto.getAktivitetStatus())
            .medKilde(dto.getKilde())
            .medAvkortetBrukersAndelPrÅr(mapBeløp(dto.getAvkortetBrukersAndelPrÅr()))
            .medAvkortetRefusjonPrÅr(mapBeløp(dto.getAvkortetRefusjonPrÅr()))
            .medAvkortetPrÅr(mapBeløp(dto.getAvkortetPrÅr()))
            .medRedusertBrukersAndelPrÅr(mapBeløp(dto.getRedusertBrukersAndelPrÅr()))
            .medRedusertRefusjonPrÅr(mapBeløp(dto.getRedusertRefusjonPrÅr()))
            .medRedusertPrÅr(mapBeløp(dto.getRedusertPrÅr()))
            .medOrginalDagsatsFraTilstøtendeYtelse(dto.getOrginalDagsatsFraTilstøtendeYtelse())
            .medMaksimalRefusjonPrÅr(mapBeløp(dto.getMaksimalRefusjonPrÅr()))
            .medFastsattAvSaksbehandler(dto.getFastsattAvSaksbehandler())
            .medPgi(mapBeløp(dto.getPgiSnitt()), mapBeløp(dto.getPgi1(), dto.getPgi2(), dto.getPgi3()))
            .medFastsattInntektskategori(new FastsattInntektskategori(dto.getInntektskategori(), dto.getInntektskategoriAutomatiskFordeling(), dto.getInntektskategoriManuellFordeling()))
            .medÅrsbeløpFraTilstøtendeYtelse(mapBeløp(dto.getÅrsbeløpFraTilstøtendeYtelse()));
        if (dto.getBeregningsperiode() != null) {
            builder.medBeregningsperiode(dto.getBeregningsperiode().getFom(), dto.getBeregningsperiode().getTom());
        }
        mapArbeidsforhold(dto.getBgAndelArbeidsforhold()).ifPresent(builder::medAndelArbeidsforhold);
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private Optional<AndelArbeidsforholdEntitet> mapArbeidsforhold(BGAndelArbeidsforholdMigreringDto dto) {
        if (dto == null) {
            return Optional.empty();
        }
        AndelArbeidsforholdEntitet entitet = AndelArbeidsforholdEntitet.builder()
            .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()))
            .medArbeidsforholdRef(mapArbeidsforholdRef(dto.getArbeidsforholdRef()))
            .medArbeidsperiodeFom(dto.getArbeidsperiodeFom())
            .medArbeidsperiodeTom(dto.getArbeidsperiodeTom())
            .medNaturalytelseBortfaltPrÅr(mapBeløp(dto.getNaturalytelseBortfaltPrÅr()))
            .medNaturalytelseTilkommetPrÅr(mapBeløp(dto.getNaturalytelseTilkommetPrÅr()))
            .medRefusjon(finnRefusjon(dto).orElse(null))
            .build();
        settOpprettetOgEndretFelter(entitet, dto);
        return Optional.of(entitet);
    }

    private Optional<Refusjon> finnRefusjon(BGAndelArbeidsforholdMigreringDto dto) {
        if (dto.getRefusjonskravPrÅr() == null && dto.getSaksbehandletRefusjonPrÅr() == null && dto.getFordeltRefusjonPrÅr() == null && dto.getManueltFordeltRefusjonPrÅr() == null) {
            return Optional.empty();
        }
        return Optional.of(new Refusjon(mapBeløp(dto.getRefusjonskravPrÅr()), mapBeløp(dto.getSaksbehandletRefusjonPrÅr()),
            mapBeløp(dto.getFordeltRefusjonPrÅr()), mapBeløp(dto.getManueltFordeltRefusjonPrÅr()), null, null));
    }

    private List<Beløp> mapBeløp(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp... pgi) {
        var pgiVerdier = Arrays.asList(pgi);
        return pgiVerdier.stream().map(this::mapBeløp).toList();
    }

    private Årsgrunnlag mapÅrsgrunnlag(BeregningsgrunnlagPrStatusOgAndelMigreringDto dto) {
        return dto.getBruttoPrÅr() == null ?
            new Årsgrunnlag() : new Årsgrunnlag(
            mapBeløp(dto.getBeregnetPrÅr()),
            mapBeløp(dto.getFordeltPrÅr()),
            mapBeløp(dto.getManueltFordeltPrÅr()),
            mapBeløp(dto.getOverstyrtPrÅr()),
            mapBeløp(dto.getBesteberegningPrÅr()),
            mapBeløp(dto.getBruttoPrÅr())
        );
    }

    private List<BeregningsgrunnlagAktivitetStatusEntitet> mapAktivitetstatuser(List<BeregningsgrunnlagAktivitetStatusMigreringDto> dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        return dto.stream().map(this::mapAktivitetstatus).toList();
    }

    private List<FaktaOmBeregningTilfelle> mapFaktaTilfeller(List<FaktaOmBeregningTilfelle> dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        return dto;
    }

    private List<SammenligningsgrunnlagPrStatusEntitet> mapSammenligningsgrunnlagListe(List<SammenligningsgrunnlagPrStatusMigreringDto> dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        return dto.stream().map(this::mapSammenligningsgrunnlag).toList();
    }

    private SammenligningsgrunnlagPrStatusEntitet mapSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusMigreringDto dto) {
        var entitet = SammenligningsgrunnlagPrStatusEntitet.builder()
            .medAvvikPromille(mapTilPromille(dto.getAvvikPromille()))
            .medRapportertPrÅr(mapBeløp(dto.getRapportertPrÅr()))
            .medSammenligningsperiode(dto.getSammenligningsperiode().getFom(), dto.getSammenligningsperiode().getTom())
            .medSammenligningsgrunnlagType(dto.getSammenligningsgrunnlagType())
            .build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private Promille mapTilPromille(BigDecimal avvikPromille) {
        return avvikPromille == null ? null : new Promille(avvikPromille);
    }

    private Optional<BesteberegninggrunnlagEntitet> mapBesteberegningGrunnlag(BesteberegninggrunnlagMigreringDto dto) {
        if (dto == null) {
            return Optional.empty();
        }
        var builder = BesteberegninggrunnlagEntitet.builder().medAvvik(mapBeløp(dto.getAvvik()));
        dto.getSeksBesteMåneder().stream().map(this::mapBesteberegningMåned).forEach(builder::leggTilMånedsgrunnlag);
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return Optional.of(entitet);
    }

    private BesteberegningMånedsgrunnlagEntitet mapBesteberegningMåned(BesteberegningMånedsgrunnlagMigreringDto dto) {
        var builder = BesteberegningMånedsgrunnlagEntitet.builder().medPeriode(dto.getPeriode().getFom(), dto.getPeriode().getTom());
        dto.getInntekter().stream().map(this::mapBesteberegningInntekt).forEach(builder::leggTilInntekt);
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private BesteberegningInntektEntitet mapBesteberegningInntekt(BesteberegningInntektMigreringDto dto) {
        var entitet = BesteberegningInntektEntitet.builder()
            .medInntekt(mapBeløp(dto.getInntekt()))
            .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()))
            .medArbeidsforholdRef(mapArbeidsforholdRef(dto.getArbeidsforholdRef()))
            .medOpptjeningAktivitetType(dto.getOpptjeningAktivitetType())
            .build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private BeregningsgrunnlagAktivitetStatusEntitet mapAktivitetstatus(BeregningsgrunnlagAktivitetStatusMigreringDto dto) {
        var entitet = BeregningsgrunnlagAktivitetStatusEntitet.builder()
            .medAktivitetStatus(dto.getAktivitetStatus())
            .medHjemmel(dto.getHjemmel())
            .build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private Beløp mapBeløp(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp dto) {
        return dto == null ? null : new Beløp(dto.verdi());
    }

    private Optional<RefusjonOverstyringerEntitet> mapRefusjonoverstyringer(BeregningRefusjonOverstyringerMigreringDto dto) {
        if (dto == null || dto.getOverstyringer().isEmpty()) {
            return Optional.empty();
        }
        var builder = RefusjonOverstyringerEntitet.builder();
        dto.getOverstyringer().stream().map(this::mapRefusjonOverstyring).forEach(builder::leggTilOverstyring);
        var entitet = builder.build();
        return Optional.of(entitet);
    }

    private RefusjonOverstyringEntitet mapRefusjonOverstyring(BeregningRefusjonOverstyringMigreringDto dto) {
        var builder = RefusjonOverstyringEntitet.builder()
            .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()))
            .medErFristUtvidet(dto.getErFristUtvidet())
            .medFørsteMuligeRefusjonFom(dto.getFørsteMuligeRefusjonFom());
        dto.getRefusjonPerioder().stream().map(this::mapRefusjonPeriodeOverstyring).forEach(builder::leggTilRefusjonPeriode);
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private RefusjonPeriodeEntitet mapRefusjonPeriodeOverstyring(BeregningRefusjonPeriodeMigreringDto dto) {
        var entitet = new RefusjonPeriodeEntitet(mapArbeidsforholdRef(dto.getArbeidsforholdRef()),
            dto.getStartdatoRefusjon());
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private Optional<AktivitetAggregatEntitet> mapAktiviteter(BeregningAktivitetAggregatMigreringDto dto) {
        if (dto == null) {
            return Optional.empty();
        }
        var builder = AktivitetAggregatEntitet.builder().medSkjæringstidspunktOpptjening(dto.getSkjæringstidspunktOpptjening());
        dto.getAktiviteter().stream().map(this::mapAktivitet).forEach(builder::leggTilAktivitet);
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return Optional.of(entitet);
    }

    private AktivitetEntitet mapAktivitet(BeregningAktivitetMigreringDto dto) {
        var entitet = AktivitetEntitet.builder()
            .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()))
            .medArbeidsforholdRef(mapArbeidsforholdRef(dto.getArbeidsforholdRef()))
            .medPeriode(mapTilIntervall(dto.getPeriode()))
            .medOpptjeningAktivitetType(dto.getOpptjeningAktivitetType())
            .build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private Optional<AktivitetAggregatEntitet> mapAktivitetOverstyringer(BeregningAktivitetOverstyringerMigreringDto dto,
                                                                         BeregningAktivitetAggregatMigreringDto registerAktiviteter) {
        if (dto == null || registerAktiviteter == null) {
            return Optional.empty();
        }
        var builder = AktivitetAggregatEntitet.builder().medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
        if (dto.getOverstyringer() != null) {
            dto.getOverstyringer().stream().map(this::mapAktivitetOverstyring).forEach(builder::leggTilAktivitet);
        }
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return Optional.of(entitet);
    }

    private AktivitetEntitet mapAktivitetOverstyring(BeregningAktivitetOverstyringMigreringDto dto) {
        var entitet = AktivitetEntitet.builder()
            .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()))
            .medArbeidsforholdRef(mapArbeidsforholdRef(dto.getArbeidsforholdRef()))
            .medOverstyrHandlingType(dto.getHandlingType())
            .medOpptjeningAktivitetType(dto.getOpptjeningAktivitetType())
            .medPeriode(mapTilIntervall(dto.getPeriode()))
            .build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private static IntervallEntitet mapTilIntervall(Periode dto) {
        return IntervallEntitet.fraOgMedTilOgMed(dto.getFom(), dto.getTom());
    }

    private Optional<FaktaAggregatEntitet> mapFaktaAggregat(FaktaAggregatMigreringDto dto) {
        if (dto == null) {
            return Optional.empty();
        }
        var builder = FaktaAggregatEntitet.builder();
        mapFaktaAktør(dto.getFaktaAktør()).ifPresent(builder::medFaktaAktør);
        List<FaktaArbeidsforholdMigreringDto> arbeidsforholdFakta = dto.getFaktaArbeidsforholdListe() == null
            ? Collections.emptyList()
            : dto.getFaktaArbeidsforholdListe();
        arbeidsforholdFakta.stream().map(this::mapFaktaArbeidsforhold).forEach(builder::leggTilFaktaArbeidsforholdIgnorerOmEksisterer);
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return Optional.of(entitet);
    }

    private FaktaArbeidsforholdEntitet mapFaktaArbeidsforhold(FaktaArbeidsforholdMigreringDto dto) {
        var builder = FaktaArbeidsforholdEntitet.builder()
            .medArbeidsforholdRef(mapArbeidsforholdRef(dto.getArbeidsforholdRef()))
            .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()));
        if (dto.getErTidsbegrenset() != null) {
            builder.medErTidsbegrenset(dto.getErTidsbegrenset().getVurdering());
        }
        if (dto.getHarLønnsendringIBeregningsperioden() != null) {
            builder.medHarLønnsendringIBeregningsperioden(dto.getHarLønnsendringIBeregningsperioden().getVurdering());
        }
        if (dto.getHarMottattYtelse() != null) {
            builder.medHarMottattYtelse(dto.getHarMottattYtelse().getVurdering());
        }
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return entitet;
    }

    private Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getArbeidsgiverAktørId() == null ? Arbeidsgiver.virksomhet(arbeidsgiver.getArbeidsgiverOrgnr()) : Arbeidsgiver.person(new AktørId(arbeidsgiver.getArbeidsgiverAktørId()));
    }

    private InternArbeidsforholdRef mapArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
        return arbeidsforholdRef == null ? InternArbeidsforholdRef.nullRef() : InternArbeidsforholdRef.ref(arbeidsforholdRef.getAbakusReferanse());
    }

    private Optional<FaktaAktørEntitet> mapFaktaAktør(FaktaAktørMigreringDto dto) {
        if (dto == null) {
            return Optional.empty();
        }
        var builder = FaktaAktørEntitet.builder();
        if (dto.getErNyIArbeidslivetSN() != null) {
            builder.medErNyIArbeidslivetSN(dto.getErNyIArbeidslivetSN().getVurdering());
        }
        if (dto.getErNyoppstartetFL() != null) {
            builder.medErNyoppstartetFL(dto.getErNyoppstartetFL().getVurdering());
        }
        if (dto.getHarFLMottattYtelse() != null) {
            builder.medHarFLMottattYtelse(dto.getHarFLMottattYtelse().getVurdering());
        }
        if (dto.getSkalBesteberegnes() != null) {
            builder.medSkalBesteberegnes(dto.getSkalBesteberegnes().getVurdering());
        }
        if (dto.getSkalBeregnesSomMilitær() != null) {
            builder.medSkalBeregnesSomMilitær(dto.getSkalBeregnesSomMilitær().getVurdering());
        }
        if (dto.getMottarEtterlønnSluttpakke() != null) {
            builder.medMottarEtterlønnSluttpakke(dto.getMottarEtterlønnSluttpakke().getVurdering());
        }
        var entitet = builder.build();
        settOpprettetOgEndretFelter(entitet, dto);
        return Optional.of(entitet);
    }

    private void settOpprettetOgEndretFelter(BaseEntitet entitet, BaseMigreringDto dto) {
        entitet.setOpprettetTidspunkt(dto.getOpprettetTidspunkt());
        entitet.setOpprettetAv(dto.getOpprettetAv());
        entitet.setEndretTidspunkt(dto.getEndretTidspunkt());
        entitet.setEndretAv(dto.getEndretAv());
    }

    public record Migreringsresultat(BeregningsgrunnlagGrunnlagEntitet grunnlag, List<RegelSporingGrunnlagEntitet> grunnlagSporinger, List<RegelSporingPeriodeEntitet> periodeSporinger, List<AvklaringsbehovEntitet> avklaringsbehov){}
}
