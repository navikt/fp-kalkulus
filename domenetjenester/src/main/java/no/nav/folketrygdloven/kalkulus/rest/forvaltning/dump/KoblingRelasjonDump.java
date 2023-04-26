package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

@ApplicationScoped
public class KoblingRelasjonDump implements DebugDumpSak {

    private EntityManager entityManager;

    public KoblingRelasjonDump() {
        // for proxys
    }

    @Inject
    public KoblingRelasjonDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {
        var sql = """
                select kr.kobling_id, kr.original_kobling_id from kobling k
                inner join kobling_relasjon kr on k.id = kr.kobling_id
                    where k.saksnummer = :saksnummer
                    order by kr.opprettet_tid asc ;
                   """;


        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());

        String path = "koblingrelasjoner.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());

    }

}
