package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.k9_DRIFT;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;

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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulus.forvaltning.AksjonspunktMigreringTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.request.v1.migrerAksjonspunkt.MigrerAksjonspunktListeRequest;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "aksjonspunktForvaltning"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class MigrerAksjonspunktRestTjeneste {


    private AksjonspunktMigreringTjeneste migreringTjeneste;

    public MigrerAksjonspunktRestTjeneste() {
        // for CDI
    }

    @Inject
    public MigrerAksjonspunktRestTjeneste(AksjonspunktMigreringTjeneste aksjonspunktMigreringTjeneste) {
        this.migreringTjeneste = aksjonspunktMigreringTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/migrerAksjonspunkter")
    @Operation(description = "Migrerer/kopierer aksjonspunkt fra k9-sak til ft-kalkulus", tags = "migrerAksjonspunkt", summary = ("Migrerer aksjonspunkt."))
    @BeskyttetRessurs(action = CREATE, resource = k9_DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response migrerAksjonspunkt(@NotNull @Valid MigrerAksjonspunktListeRequestAbacDto spesifikasjon) {
        var avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.fraKode(spesifikasjon.getAvklaringsbehovKode());
        switch (avklaringsbehovDefinisjon) {
            case AVKLAR_AKTIVITETER -> migreringTjeneste.avklarAktiviteterMigrering(spesifikasjon.getAksjonspunktdata());
            case VURDER_FAKTA_FOR_ATFL_SN -> migreringTjeneste.vurderFaktaBeregningMigrering(spesifikasjon.getAksjonspunktdata());
            case FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS -> migreringTjeneste.vurderFastsettVedAvvikATFLMigrering(spesifikasjon.getAksjonspunktdata());
            case FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET -> migreringTjeneste.nyIArbeidslivetSNMigrering(spesifikasjon.getAksjonspunktdata());
            case FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD -> migreringTjeneste.fastsettForTidsbegrensetMigrering(spesifikasjon.getAksjonspunktdata());
            case VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE -> migreringTjeneste.vurderVarigEndringMigrering(spesifikasjon.getAksjonspunktdata());
            case FORDEL_BEREGNINGSGRUNNLAG -> migreringTjeneste.fordelBeregningsgrunnlagMigrering(spesifikasjon.getAksjonspunktdata());
            default -> throw new IllegalArgumentException("Ukjent aksjonspunktkode " + avklaringsbehovDefinisjon.getKode());
        }
        return Response.ok().build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class MigrerAksjonspunktListeRequestAbacDto extends MigrerAksjonspunktListeRequest implements AbacDto {

        public MigrerAksjonspunktListeRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }


}
