package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulus.beregning.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoForGUIRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagGrunnlagForReferanseRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregningsgrunnlag"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class HentKalkulusRestTjeneste {


    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;

    public HentKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public HentKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                    BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                    KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                    BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.beregningsgrunnlagDtoTjeneste = beregningsgrunnlagDtoTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlag for angitt behandling", summary = ("Returnerer beregningsgrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG, ressurs = FAGSAK)
    @Path("/fastsatt")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentFastsattBeregningsgrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse, ytelseTyperKalkulusStøtter);
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .filter(grunnlag -> grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
                .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
                .map(MapBeregningsgrunnlag::map)
                .map(bgDto -> Response.ok(bgDto).build())
                .findFirst()
                .orElse(Response.noContent().build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent BeregningsgrunnlagGrunnlag for angitt grunnlagsreferanse", summary = ("Returnerer BeregningsgrunnlagGrunnlag for grunnlagsreferanse."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG, ressurs = FAGSAK)
    @Path("/grunnlagForReferanse")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentBeregningsgrunnlagGrunnlagForReferanse(@NotNull @Valid HentBeregningsgrunnlagGrunnlagForReferanseRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse, ytelseTyperKalkulusStøtter);
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitetForReferanse(koblingId, spesifikasjon.getGrunnlagReferanse());
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .map(MapDetaljertBeregningsgrunnlag::mapGrunnlag)
                .map(bgDto -> Response.ok(bgDto).build())
                .findFirst()
                .orElse(Response.noContent().build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent aktivt BeregningsgrunnlagGrunnlag for angitt behandling", summary = ("Returnerer aktivt BeregningsgrunnlagGrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG, ressurs = FAGSAK)
    @Path("/grunnlag")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentAktivtBeregningsgrunnlagGrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse, ytelseTyperKalkulusStøtter);
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .map(MapDetaljertBeregningsgrunnlag::mapGrunnlag)
                .map(bgDto -> Response.ok(bgDto).build())
                .findFirst()
                .orElse(Response.noContent().build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent aktivt beregningsgrunnlag for angitt behandling", summary = ("Returnerer aktivt beregningsgrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG, ressurs = FAGSAK)
    @Path("/aktivtBeregningsgrunnlag")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentAktivtBeregningsgrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse, ytelseTyperKalkulusStøtter);
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
                .map(MapDetaljertBeregningsgrunnlag::map)
                .map(bgDto -> Response.ok(bgDto).build())
                .findFirst()
                .orElse(Response.noContent().build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlagDto for angitt behandling som brukes frontend", summary = ("Returnerer beregningsgrunnlagDto for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG, ressurs = FAGSAK)
    @Path("/beregningsgrunnlag")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBeregningsgrunnlagDto(@NotNull @Valid HentBeregningsgrunnlagDtoForGUIRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Optional<Long> koblingId = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse, ytelseTyperKalkulusStøtter);
        if(koblingId.isEmpty()){
            return Response.noContent().build();
        }

        BeregningsgrunnlagInput beregningsgrunnlagInput = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(koblingId.get());

        Set<no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdReferanseDto> referanser = MapIAYTilKalulator.mapArbeidsgiverReferanser(spesifikasjon.getReferanser());
        List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger = MapIAYTilKalulator.mapArbeidsgiverOpplysninger(spesifikasjon.getArbeidsgiverOpplysninger());
        BeregningsgrunnlagRestInput restInput = new BeregningsgrunnlagRestInput(beregningsgrunnlagInput, arbeidsgiverOpplysninger, referanser);
        BeregningsgrunnlagDto beregningsgrunnlagDto = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(restInput);
        return Response.ok(beregningsgrunnlagDto).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent grunnlag for frisinn", summary = ("Returnerer frisinngrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG, ressurs = FAGSAK)
    @Path("/frisinnGrunnlag")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentFrisinnGrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Optional<Long> koblingId = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse, ytelseTyperKalkulusStøtter);
        if (koblingId.isEmpty()) {
            return Response.noContent().build();
        }
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId.get());
        BeregningsgrunnlagInput input = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(koblingId.get());
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
                .map(bg -> MapBeregningsgrunnlagFRISINN.map(bg, input.getIayGrunnlag().getOppgittOpptjening(), input.getYtelsespesifiktGrunnlag()))
                .map(bgDto -> Response.ok(bgDto).build())
                .findFirst()
                .orElse(Response.noContent().build());
    }


    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagGrunnlagForReferanseRequestAbacDto extends HentBeregningsgrunnlagGrunnlagForReferanseRequest implements AbacDto {


        @JsonCreator
        public HentBeregningsgrunnlagGrunnlagForReferanseRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                                    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                    @JsonProperty(value = "grunnlagReferanse", required = true) @NotNull @Valid UUID grunnlagReferanse
                                                    ) {
            super(eksternReferanse, ytelseSomSkalBeregnes, grunnlagReferanse);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse());
            return abacDataAttributter;
        }
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagRequestAbacDto extends HentBeregningsgrunnlagRequest implements AbacDto {


        @JsonCreator
        public HentBeregningsgrunnlagRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                               @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(eksternReferanse, ytelseSomSkalBeregnes);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse());
            return abacDataAttributter;
        }
    }


    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagDtoForGUIRequestAbacDto extends HentBeregningsgrunnlagDtoForGUIRequest implements AbacDto {


        public HentBeregningsgrunnlagDtoForGUIRequestAbacDto() {
            // For Json deserialisering
        }

        @Deprecated
        public HentBeregningsgrunnlagDtoForGUIRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                                             @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                             @JsonProperty(value = "arbeidsgiverOpplysninger", required = true) @NotNull @Valid List<no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
            super(eksternReferanse, ytelseSomSkalBeregnes, arbeidsgiverOpplysninger);
        }

        public HentBeregningsgrunnlagDtoForGUIRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                                             @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                             @JsonProperty(value = "arbeidsgiverOpplysninger", required = true) @NotNull @Valid List<no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger,
                                                             @JsonProperty(value = "referanser") @Valid Set<no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdReferanseDto> referanser) {
            super(eksternReferanse, ytelseSomSkalBeregnes, arbeidsgiverOpplysninger, referanser);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse());
            return abacDataAttributter;
        }
    }


}
