package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;

@ApplicationScoped
public class ForeslåRegelSporingDump implements DebugDumpSak {

    private EntityManager entityManager;
    private KoblingTjeneste koblingTjeneste;
    private RegelsporingRepository regelsporingRepository;


    public ForeslåRegelSporingDump() {
        // for proxys
    }

    @Inject
    public ForeslåRegelSporingDump(EntityManager entityManager, KoblingTjeneste koblingTjeneste, RegelsporingRepository regelsporingRepository) {
        this.entityManager = entityManager;
        this.koblingTjeneste = koblingTjeneste;
        this.regelsporingRepository = regelsporingRepository;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {

        var sql = """
                select  rsp.kobling_id,
                        rsp.regel_type,
                        DATE(rsp.fom),
                        rsp.regel_evaluering_json,
                        ri_komp.regel_input_json
                     from KOBLING k
                              inner join REGEL_SPORING_PERIODE rsp on rsp.kobling_id = k.id
                              inner join REGEL_INPUT_KOMPRIMERING ri_komp on ri_komp.regel_input_hash = rsp.regel_input_hash
                    where k.saksnummer = :saksnummer and rsp.aktiv = true ;
                   """;


        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());


        Stream<Tuple> resultStream = query.getResultStream();

        return resultStream
                .flatMap(e -> Stream.of(
                        new DumpOutput(String.format("kobling-%s/%s-%s-regel-evaluering.json", e.get(0), e.get(1), e.get(2)), e.get(3).toString()),
                                new DumpOutput(String.format("kobling-%s/%s-%s-regel-input.json", e.get(0), e.get(1), e.get(2)), e.get(4).toString())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
