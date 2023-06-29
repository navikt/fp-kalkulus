package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.BEREGNINGSGRUNNLAG;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.SimulerGraderingMotInntektTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.SimulerTilkomneAktiviteterTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.typer.StatusOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.request.v1.tilkommetAktivitet.UtledTilkommetAktivitetForRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.tilkommetAktivitet.UtledTilkommetAktivitetListeRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.tilkommetAktivitet.UtledetTilkommetAktivitet;
import no.nav.folketrygdloven.kalkulus.response.v1.tilkommetAktivitet.UtledetTilkommetAktivitetListe;
import no.nav.folketrygdloven.kalkulus.response.v1.tilkommetAktivitet.UtledetTilkommetAktivitetPrReferanse;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "utledTilkommetAktivitet"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class TilkommetAktivitetRestTjeneste {


    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    private KoblingRepository koblingRepository;

    private SimulerGraderingMotInntektTjeneste simulerGraderingMotInntektTjeneste;


    public TilkommetAktivitetRestTjeneste() {
        // for CDI
    }

    @Inject
    public TilkommetAktivitetRestTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                               KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                               KoblingRepository koblingRepository,
                                               SimulerGraderingMotInntektTjeneste simulerGraderingMotInntektTjeneste) {
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
        this.simulerGraderingMotInntektTjeneste = simulerGraderingMotInntektTjeneste;
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("utledTilkommetAktivitetForKoblinger")
    @Operation(description = "Simulerer tilkommet aktivitet", tags = "utledTilkommetInntekt", summary = ("Utled tilkommet aktivitet"))
    @BeskyttetRessurs(action = READ, property = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response utledTilkommetAktivitetForKoblinger(@NotNull @Valid UtledTilkommetAktivitetListeRequestAbacDto spesifikasjon) {
        var referanser = spesifikasjon.getListe().stream().map(UtledTilkommetAktivitetForRequest::getEksternReferanse).map(KoblingReferanse::new).toList();
        var koblinger = koblingRepository.hentKoblingerFor(
                referanser,
                spesifikasjon.getYtelseType());
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var inputer = kalkulatorInputTjeneste.hentForKoblinger(beregningsgrunnlag.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet()));

        final List<UtledetTilkommetAktivitetPrReferanse> simuleringer = beregningsgrunnlag.stream().map(bg -> {
            var kobling = koblinger.stream().filter(it -> it.getId().equals(bg.getKoblingId())).findFirst().orElseThrow();
            var input = inputer.get(kobling.getId());
            var beregningsgrunnlagInput = lagBeregningsgrunnlagInput(kobling, input, bg);
            var tilkommetAktivitetPerioder = SimulerTilkomneAktiviteterTjeneste.utledTilkommetAktivitetPerioder(beregningsgrunnlagInput);
            final List<UtledetTilkommetAktivitet> aktiviteter = mapTilUtledetTilkommetAktivitet(tilkommetAktivitetPerioder);

            return new UtledetTilkommetAktivitetPrReferanse(
                    koblinger.stream().filter(it -> it.getId().equals(bg.getKoblingId())).findFirst().orElseThrow().getKoblingReferanse().getReferanse(),
                    aktiviteter
                    );
        }).toList();
        return Response.ok(new UtledetTilkommetAktivitetListe(simuleringer)).build();
    }

    private BeregningsgrunnlagInput lagBeregningsgrunnlagInput(KoblingEntitet kobling,
            KalkulatorInputDto input,
            BeregningsgrunnlagGrunnlagEntitet aktivGrunnlagEntitet) {
        return MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, Optional.of(aktivGrunnlagEntitet), Collections.emptyList());
    }

    private static List<UtledetTilkommetAktivitet> mapTilUtledetTilkommetAktivitet(LocalDateTimeline<Set<StatusOgArbeidsgiver>> tilkommetAktivitetPerioder) {
        final Map<StatusOgArbeidsgiver, List<Periode>> resultat = new HashMap<>();
        tilkommetAktivitetPerioder.stream().forEach(s -> {
            s.getValue().stream().forEach(statusOgArbeidsgiver -> {
                List<Periode> perioder = resultat.get(statusOgArbeidsgiver);
                if (perioder == null) {
                    perioder = new ArrayList<>();
                    resultat.put(statusOgArbeidsgiver, perioder);
                }
                perioder.add(new Periode(s.getFom(), s.getTom()));
            });
        });

        final List<UtledetTilkommetAktivitet> aktiviteter = resultat.entrySet()
                .stream()
                .map(entry -> new UtledetTilkommetAktivitet(entry.getKey().aktivitetStatus(), mapArbeidsgiver(entry.getKey().arbeidsgiver()), entry.getValue()))
                .collect(Collectors.toList());

        return aktiviteter;
    }

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver a) {
        if (a == null) {
            return null;
        }
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class UtledTilkommetAktivitetListeRequestAbacDto extends UtledTilkommetAktivitetListeRequest implements AbacDto {

        public UtledTilkommetAktivitetListeRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }
}
