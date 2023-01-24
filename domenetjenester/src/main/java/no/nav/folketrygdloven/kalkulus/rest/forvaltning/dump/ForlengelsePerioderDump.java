package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

@ApplicationScoped
public class ForlengelsePerioderDump implements DebugDumpSak {

    private EntityManager entityManager;

    public ForlengelsePerioderDump() {
        // for proxys
    }

    @Inject
    public ForlengelsePerioderDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {
        var sql = """
                select k.id kobling_id, fom, tom, aktiv from kobling k
                inner join forlengelse_perioder fp on k.id = fp.kobling_id
                inner join forlengelse_periode f on fp.id = f.forlengelse_perioder_id
                    where k.saksnummer = :saksnummer
                    order by fp.opprettet_tid asc ;
                   """;


        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());

        String path = "forlengelseperioder.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());

    }

}
