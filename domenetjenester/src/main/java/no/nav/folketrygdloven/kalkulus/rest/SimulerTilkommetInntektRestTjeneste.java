package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulator.felles.inntektgradering.SimulerGraderingMotInntektTjeneste.ReduksjonVedGradering;
import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.BEREGNINGSGRUNNLAG;
import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.DRIFT;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
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
import no.nav.folketrygdloven.kalkulator.guitjenester.VurderNyeInntektsforholdDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.request.v1.simulerTilkommetInntekt.FinnSimulerTilkommetInntektInputRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.simulerTilkommetInntekt.SimulerTilkommetInntektForRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.simulerTilkommetInntekt.SimulerTilkommetInntektListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.simulerTilkommetInntekt.SimulerTilkommetInntektRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt.SimulertTilkommetInntekt;
import no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt.SimulertTilkommetInntektListe;
import no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt.SimulertTilkommetInntektPrReferanse;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "simulerTilkommetInntekt"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class SimulerTilkommetInntektRestTjeneste {


    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    private KoblingRepository koblingRepository;

    private SimulerGraderingMotInntektTjeneste simulerGraderingMotInntektTjeneste;


    public SimulerTilkommetInntektRestTjeneste() {
        // for CDI
    }

    @Inject
    public SimulerTilkommetInntektRestTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
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
    @Path("simulerTilkommetInntektForKoblinger")
    @Operation(description = "Simulerer tilkommet inntekt for koblinger", tags = "simulerTilkommetInntekt", summary = ("Simulerer tilkommet inntekt for koblinger"))
    @BeskyttetRessurs(action = READ, property = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response simulerTilkommetInntektForKoblinger(@NotNull @Valid SimulerTilkommetInntektListeRequestAbacDto spesifikasjon) {
        var referanser = spesifikasjon.getSimulerForListe().stream().map(SimulerTilkommetInntektForRequest::getEksternReferanse).map(KoblingReferanse::new).toList();
        var koblinger = koblingRepository.hentKoblingerFor(
                referanser,
                spesifikasjon.getYtelseType());
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var inputer = kalkulatorInputTjeneste.hentForKoblinger(beregningsgrunnlag.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet()));
        var fastsatteGrunnlag = beregningsgrunnlag.stream()
                .filter(bg -> bg.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
                .toList();
        var simuleringer = fastsatteGrunnlag.stream().map(bg -> {
            var kobling = koblinger.stream().filter(it -> it.getId().equals(bg.getKoblingId())).findFirst().orElseThrow();
            var input = inputer.get(kobling.getId());
            var beregningsgrunnlagInput = lagBeregningsgrunnlagInput(kobling, input, bg);
            var tilkommetAktivitetPerioder = simulerGraderingMotInntektTjeneste.finnTilkommetAktivitetPerioder(beregningsgrunnlagInput);
            return new SimulertTilkommetInntektPrReferanse(
                    koblinger.stream().filter(it -> it.getId().equals(bg.getKoblingId())).findFirst().orElseThrow().getKoblingReferanse().getReferanse(),
                    tilkommetAktivitetPerioder.stream().map(p -> new Periode(p.getFomDato(), p.getTomDato())).toList()
            );
        }).toList();
        return Response.ok(new SimulertTilkommetInntektListe(simuleringer)).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("simulerTilkommetInntektForSak")
    @Operation(description = "Simulerer tilkommet inntekt vurdering for sak", tags = "simulerTilkommetInntekt", summary = ("Simulerer tilkommet inntekt vurdering for sak"))
    @BeskyttetRessurs(action = READ, property = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response simulerTilkommetInntektForSak(@NotNull @Valid FinnSimulerTilkommetInntektInputRequestAbacDto spesifikasjon) {
        var koblinger = koblingRepository.hentAlleKoblingerForSaksnummer(new Saksnummer(spesifikasjon.getSaksnummer()));
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var inputer = kalkulatorInputTjeneste.hentForKoblinger(beregningsgrunnlag.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet()));
        var fastsatteGrunnlag = beregningsgrunnlag.stream()
                .filter(bg -> bg.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
                .map(List::of)
                .reduce(velgNyestePrSkjæringstidspunkt())
                .orElse(Collections.emptyList());
        var simuleringer = fastsatteGrunnlag.stream().map(bg -> {
            var kobling = koblinger.stream().filter(it -> it.getId().equals(bg.getKoblingId())).findFirst().orElseThrow();
            var input = inputer.get(kobling.getId());
            var beregningsgrunnlagInput = lagBeregningsgrunnlagInput(kobling, input, bg);
            return simulerGraderingMotInntektTjeneste.simulerGraderingMotInntekt(beregningsgrunnlagInput);
        }).flatMap(Collection::stream).toList();
        return Response.ok(simuleringer).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("simulerTilkommetInntektInput")
    @Operation(description = "Finn input som brukes til simulerering av tilkommet inntekt", tags = "simulerTilkommetInntekt", summary = ("Finn input som brukes til simulerering av tilkommet inntekt"))
    @BeskyttetRessurs(action = READ, property = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response simulerTilkommetInntektInput(@NotNull @Valid FinnSimulerTilkommetInntektInputRequestAbacDto spesifikasjon) {
        var koblinger = koblingRepository.hentAlleKoblingerForSaksnummer(new Saksnummer(spesifikasjon.getSaksnummer()));
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var inputer = kalkulatorInputTjeneste.hentForKoblinger(beregningsgrunnlag.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet()));
        var fastsatteGrunnlag = beregningsgrunnlag.stream()
                .filter(bg -> bg.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
                .map(List::of)
                .reduce(velgNyestePrSkjæringstidspunkt())
                .orElse(Collections.emptyList());
        var vurderDtoer = fastsatteGrunnlag.stream().map(bg -> {
            var kobling = koblinger.stream().filter(it -> it.getId().equals(bg.getKoblingId())).findFirst().orElseThrow();
            var input = inputer.get(kobling.getId());
            var beregningsgrunnlagInput = lagBeregningsgrunnlagInput(kobling, input, bg);
            var inputGrunnlag = simulerGraderingMotInntektTjeneste.lagInputGrunnlag(beregningsgrunnlagInput);
            return VurderNyeInntektsforholdDtoTjeneste.lagVurderNyttInntektsforholdDto(inputGrunnlag, beregningsgrunnlagInput.getIayGrunnlag(), beregningsgrunnlagInput.getYtelsespesifiktGrunnlag(), FagsakYtelseType.PLEIEPENGER_SYKT_BARN, List.of());
        }).toList();
        return Response.ok(vurderDtoer).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("simulerTilkommetInntekt")
    @Operation(description = "Simulerer tilkommet inntekt vurdering for periode", tags = "simulerTilkommetInntekt", summary = ("Simulerer aksjonspukt for tilkommet inntekt"))
    @BeskyttetRessurs(action = READ, property = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response simulerTilkommetInntekt(@NotNull @Valid SimulerTilkommetInntektRequestAbacDto spesifikasjon) {
        var koblinger = koblingRepository.hentKoblingerOpprettetIPeriode(spesifikasjon.getPeriode(), YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_SYKT_BARN);
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var inputer = kalkulatorInputTjeneste.hentForKoblinger(beregningsgrunnlag.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet()));
        var bgPrSaksnummer = beregningsgrunnlag.stream()
                .filter(bg -> bg.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
                .collect(Collectors.toMap(finnSaksnummer(koblinger),
                        List::of, velgNyestePrSkjæringstidspunkt()));

        var saksnummerMedReduksjonsinfo = bgPrSaksnummer.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> mapReduksjoner(koblinger, inputer, e)));

        return Response.ok(new SimulertTilkommetInntekt(
                finnAntallSakerMedTilkommetInntektAksjonspunkt(saksnummerMedReduksjonsinfo),
                finnSakerMedTilkommetInntektAksjonspunkt(saksnummerMedReduksjonsinfo),
                finnAntallSakerMedManuellFordeling(saksnummerMedReduksjonsinfo),
                finnAntallSakerMedReduksjon(spesifikasjon, saksnummerMedReduksjonsinfo),
                bgPrSaksnummer.size(),
                finnAntallSakerPrTilkommetStatus(saksnummerMedReduksjonsinfo))).build();
    }

    private static long finnAntallSakerMedTilkommetInntektAksjonspunkt(Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        return saksnummerMedReduksjon.entrySet().stream().filter(e -> !e.getValue().isEmpty()).count();
    }

    private static Set<String> finnSakerMedTilkommetInntektAksjonspunkt(Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        return saksnummerMedReduksjon.entrySet().stream().filter(e -> !e.getValue().isEmpty()).map(Map.Entry::getKey)
                .map(Saksnummer::getVerdi)
                .collect(Collectors.toSet());
    }

    private static long finnAntallSakerMedReduksjon(SimulerTilkommetInntektRequestAbacDto spesifikasjon,
                                                    Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        var dagsatsFeiltoleranse = spesifikasjon.getDagsatsFeiltoleranse() != null ? spesifikasjon.getDagsatsFeiltoleranse() : 0L;

        return saksnummerMedReduksjon.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(r -> (r.gjeldendeDagsats() - r.gradertDagsats()) > dagsatsFeiltoleranse))
                .count();
    }

    private static long finnAntallSakerMedManuellFordeling(Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        return saksnummerMedReduksjon.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty() && e.getValue().stream().anyMatch(ReduksjonVedGradering::erFordelt)).count();
    }

    private static Map<String, Integer> finnAntallSakerPrTilkommetStatus(Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        return saksnummerMedReduksjon.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(
                        Collectors.toMap(
                                e -> e.getValue().stream().flatMap(r -> r.tilkommetStatuser().stream()).distinct()
                                        .map(AktivitetStatus::getKode)
                                        .sorted(Comparator.naturalOrder())
                                        .reduce((s1, s2) -> s1 + "_" + s2)
                                        .orElse(""),
                                e -> 1,
                                Integer::sum)
                );
    }

    private List<ReduksjonVedGradering> mapReduksjoner(List<KoblingEntitet> koblinger,
                                                       Map<Long, KalkulatorInputDto> inputer,
                                                       Map.Entry<Saksnummer, List<BeregningsgrunnlagGrunnlagEntitet>> e) {
        return e.getValue().stream().map(bg ->
        {
            var kobling = koblinger.stream().filter(it -> it.getId().equals(bg.getKoblingId())).findFirst().orElseThrow();
            var input = inputer.get(kobling.getId());
            return finnPerioderMedReduksjonForBeregningsgrunnlag(bg, kobling, input);
        }).flatMap(Collection::stream).toList();
    }

    private static BinaryOperator<List<BeregningsgrunnlagGrunnlagEntitet>> velgNyestePrSkjæringstidspunkt() {
        return (eksisterendeListe, value) -> {
            var res = new ArrayList<>(eksisterendeListe);
            var bg = value.get(0);
            var eksisterendeMedSammeStp = finnEksisterendeMedSammeStp(eksisterendeListe, bg);
            if (eksisterendeMedSammeStp.isPresent()) {
                velgSistOpprettet(res, bg, eksisterendeMedSammeStp);
            }
            return res;
        };
    }

    private static void velgSistOpprettet(ArrayList<BeregningsgrunnlagGrunnlagEntitet> res, BeregningsgrunnlagGrunnlagEntitet bg, Optional<BeregningsgrunnlagGrunnlagEntitet> eksisterendeMedSammeStp) {
        if (eksisterendeMedSammeStp.get().getOpprettetTidspunkt().isBefore(bg.getOpprettetTidspunkt())) {
            res.remove(eksisterendeMedSammeStp.get());
            res.add(bg);
        }
    }

    private static Optional<BeregningsgrunnlagGrunnlagEntitet> finnEksisterendeMedSammeStp(List<BeregningsgrunnlagGrunnlagEntitet> eksisterendeListe, BeregningsgrunnlagGrunnlagEntitet bg) {
        return eksisterendeListe.stream().filter(it -> it.getBeregningsgrunnlag().get().getSkjæringstidspunkt().equals(bg.getBeregningsgrunnlag().get().getSkjæringstidspunkt())).findFirst();
    }

    private List<ReduksjonVedGradering> finnPerioderMedReduksjonForBeregningsgrunnlag(BeregningsgrunnlagGrunnlagEntitet bg,
                                                                                      KoblingEntitet k,
                                                                                      KalkulatorInputDto input) {
        var beregningsgrunnlagInput = lagBeregningsgrunnlagInput(k, input, bg);
        return simulerGraderingMotInntektTjeneste.simulerGraderingMotInntekt(beregningsgrunnlagInput);
    }


    private static Function<BeregningsgrunnlagGrunnlagEntitet, Saksnummer> finnSaksnummer(List<KoblingEntitet> koblinger) {
        return bg -> koblinger.stream()
                .filter(k -> k.getId().equals(bg.getKoblingId()))
                .findFirst().map(KoblingEntitet::getSaksnummer).orElseThrow();
    }

    private BeregningsgrunnlagInput lagBeregningsgrunnlagInput(KoblingEntitet kobling,
                                                               KalkulatorInputDto input,
                                                               BeregningsgrunnlagGrunnlagEntitet aktivGrunnlagEntitet) {
        return MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, Optional.of(aktivGrunnlagEntitet), Collections.emptyList());
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SimulerTilkommetInntektListeRequestAbacDto extends SimulerTilkommetInntektListeRequest implements AbacDto {

        public SimulerTilkommetInntektListeRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SimulerTilkommetInntektRequestAbacDto extends SimulerTilkommetInntektRequest implements AbacDto {

        public SimulerTilkommetInntektRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class FinnSimulerTilkommetInntektInputRequestAbacDto extends FinnSimulerTilkommetInntektInputRequest implements AbacDto {

        public FinnSimulerTilkommetInntektInputRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }


}
