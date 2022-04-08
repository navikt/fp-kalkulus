package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

@ApplicationScoped
public class KalkulatorInputDump implements DebugDumpSak {

    private EntityManager entityManager;

    KalkulatorInputDump() {
        // for proxys
    }

    @Inject
    KalkulatorInputDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {
        var sql = """
                select k.kobling_referanse, ki.input, ki.opprettet_tid, ki.aktiv from kobling k
                inner join kalkulator_input ki on k.id = ki.kobling_id
                    where k.saksnummer = :saksnummer
                    order by b.opprettet_tid asc ;
                   """;

        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());
        String path = "kalkulatorinput.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return List.of();
        }

        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());
    }

}
