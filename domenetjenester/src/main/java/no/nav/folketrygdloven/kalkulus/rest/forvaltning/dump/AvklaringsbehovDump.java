package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class AvklaringsbehovDump implements DebugDumpSak {

    private static final Logger log = LoggerFactory.getLogger(AvklaringsbehovDump.class);

    private EntityManager entityManager;
    private KoblingRepository koblingRepository;

    public AvklaringsbehovDump() {
        // for proxys
    }

    @Inject
    public AvklaringsbehovDump(EntityManager entityManager, KoblingRepository koblingRepository) {
        this.entityManager = entityManager;
        this.koblingRepository = koblingRepository;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {
        var sql = """
                select cast(k.kobling_referanse as varchar)  ekstern_referanse,
                        k.id kobling_id,
                        ab.avklaringsbehov_def,
                        ab.avklaringsbehov_status,
                        ab.begrunnelse
                     from KOBLING k
                              inner join AVKLARINGSBEHOV ab on ab.kobling_id = k.id
                    where k.saksnummer = :saksnummer
                    order by ab.opprettet_tid asc ;
                   """;


        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());


        String path = "avklaringsbehov.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return List.of();
        }

        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());
    }

}
