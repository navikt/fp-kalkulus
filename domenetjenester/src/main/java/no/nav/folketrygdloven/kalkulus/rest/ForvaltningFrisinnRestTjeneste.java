package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.DRIFT;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.forvaltning.DiffResultatDto;
import no.nav.folketrygdloven.kalkulus.forvaltning.KontrollerBeregningsinputTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.request.v1.KontrollerInputForSakerRequest;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "frisinnForvaltning"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class ForvaltningFrisinnRestTjeneste {

    private KontrollerBeregningsinputTjeneste kontrollerBeregningsinputTjeneste;
    private KoblingRepository koblingRepository;

    public ForvaltningFrisinnRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningFrisinnRestTjeneste(KontrollerBeregningsinputTjeneste kontrollerBeregningsinputTjeneste,
                                          KoblingRepository koblingRepository) {
        this.kontrollerBeregningsinputTjeneste = kontrollerBeregningsinputTjeneste;
        this.koblingRepository = koblingRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Kontroller beregningsinput", summary = ("Kontrollerer input for beregning for en liste med saksnummer."), tags = "frisinnForvaltning")
    @BeskyttetRessurs(action = READ, resource = DRIFT)
    @Path("/kontrollerInput")
    public Response hentGrunnbeløp(@NotNull @Valid KontrollerInputForSakerRequestAbacDto spesifikasjon) {
        List<DiffResultatDto> differ = new ArrayList<>();
        for (String saksnummer : spesifikasjon.getSaksnummer()) {
            Optional<KoblingEntitet> kobling = koblingRepository.hentSisteKoblingForSaksnummer(new Saksnummer(saksnummer));
            kobling.flatMap(koblingEntitet -> kontrollerBeregningsinputTjeneste.kontrollerInputForKobling(koblingEntitet)).ifPresent(differ::add);
        }
        String csv = byggTekst(differ);
        return Response.ok(csv, MediaType.TEXT_PLAIN).build();
    }

    private String byggTekst(List<DiffResultatDto> liste) {
        var sb = new StringBuilder(2048);
        sb.append("saksnummer,erDiff,sokerNaering,sokerFrilans,gjeldendeInntektSoktOm,gjeldendeInntektIkkeSoktOm,reberegnetInntektSoktOm,"
                + "reberegnetInntektIkkeSoktOm\n");
        for (DiffResultatDto diff : liste) {
            int reberegnetSøktOm = diff.getReberegnetInntektSøktOm().setScale(0, RoundingMode.UP).intValue();
            int gjeldendeSøktOm = diff.getGjeldendeInntektSøktOm().setScale(0, RoundingMode.UP).intValue();
            int reberegnetIkkeSøktOm = diff.getReberegnetInntektIkkeSøktOm().setScale(0, RoundingMode.UP).intValue();
            int gjeldendeIkkeSøktOm = diff.getGjeldendeInntektIkkeSøktOm().setScale(0, RoundingMode.UP).intValue();
            boolean erDiff = diff.isHarDiff();
            boolean søkerNæring = diff.getStatuserDetErSøktOm().contains(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
            boolean søkerFrilans = diff.getStatuserDetErSøktOm().contains(AktivitetStatus.FRILANSER);
            String saksnummer = diff.getSaksnummer();
            Object[] args = new Object[] { saksnummer, erDiff, søkerNæring, søkerFrilans, gjeldendeSøktOm, gjeldendeIkkeSøktOm, reberegnetSøktOm, reberegnetIkkeSøktOm };
            String fmt = "%s,".repeat(args.length);
            var s = String.format(fmt.substring(0, fmt.length() - 1), args);
            sb.append(s).append('\n');
        }
        return sb.toString();
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class KontrollerInputForSakerRequestAbacDto extends KontrollerInputForSakerRequest implements no.nav.vedtak.sikkerhet.abac.AbacDto {


        @JsonCreator
        public KontrollerInputForSakerRequestAbacDto(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull List<String> saksnummer) {
            super(saksnummer);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }


}
