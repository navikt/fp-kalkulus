package no.nav.folketrygdloven.kalkulus.app.sikkerhet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.k9.felles.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.k9.felles.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.k9.felles.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.k9.felles.util.Tuple;

@Dependent
@Alternative
@Priority(2)
public class XacmlRequestBuilderTjenesteImpl implements XacmlRequestBuilderTjeneste {

    public XacmlRequestBuilderTjenesteImpl() {
    }

    private DomeneAbacAttributter domeneAbacAttributter;

    @Inject
    public XacmlRequestBuilderTjenesteImpl(DomeneAbacAttributter domeneAbacAttributter) {
        this.domeneAbacAttributter = domeneAbacAttributter;
    }

    @Override
    public XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(FellesAbacAttributter.XACML_1_0_ACTION_ACTION_ID, pdpRequest.getString(FellesAbacAttributter.XACML_1_0_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);

        Set<Tuple<String, String>> identer = hentIdenter(pdpRequest,
                FellesAbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE,
                FellesAbacAttributter.RESOURCE_FELLES_PERSON_FNR);

        if (identer.isEmpty()) {
            populerResources(xacmlBuilder, pdpRequest, null);
        } else {
            for (Tuple<String, String> ident : identer) {
                populerResources(xacmlBuilder, pdpRequest, ident);
            }
        }

        return xacmlBuilder;
    }

    private void populerResources(XacmlRequestBuilder xacmlBuilder, PdpRequest pdpRequest, Tuple<String, String> ident) {
        List<String> aksjonspunktTyper = pdpRequest.getListOfString(domeneAbacAttributter.getAttributtnøkkelAksjonspunktType());
        if (aksjonspunktTyper.isEmpty()) {
            xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident, null));
        } else {
            for (String aksjonspunktType : aksjonspunktTyper) {
                xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident, aksjonspunktType));
            }
        }
    }

    private XacmlAttributeSet byggRessursAttributter(PdpRequest pdpRequest, Tuple<String, String> ident, String aksjonspunktType) {
        XacmlAttributeSet resourceAttributeSet = new XacmlAttributeSet();
        resourceAttributeSet.addAttribute(FellesAbacAttributter.RESOURCE_FELLES_DOMENE, pdpRequest.getString(FellesAbacAttributter.RESOURCE_FELLES_DOMENE));
        resourceAttributeSet.addAttribute(FellesAbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE, pdpRequest.getString(FellesAbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE));
        setOptionalValueinAttributeSet(resourceAttributeSet, pdpRequest, domeneAbacAttributter.getAttributtnøkkelBehandlingsuuid());
        if (ident != null) {
            resourceAttributeSet.addAttribute(ident.getElement1(), ident.getElement2());
        }

        return resourceAttributeSet;
    }

    private void setOptionalValueinAttributeSet(XacmlAttributeSet resourceAttributeSet, PdpRequest pdpRequest, String key) {
        pdpRequest.getOptional(key).ifPresent(s -> resourceAttributeSet.addAttribute(key, s));
    }


    private Set<Tuple<String, String>> hentIdenter(PdpRequest pdpRequest, String... identNøkler) {
        Set<Tuple<String, String>> identer = new HashSet<>();
        for (String key : identNøkler) {
            identer.addAll(pdpRequest.getListOfString(key).stream().map(it -> new Tuple<>(key, it)).collect(Collectors.toList()));
        }
        return identer;
    }
}
