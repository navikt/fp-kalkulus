package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

@ApplicationScoped
public class KalkulatorInputDump implements DebugDumpSak {

    private static final Logger log = LoggerFactory.getLogger(BeregningsgrunnlagDump.class);

    private EntityManager entityManager;

    public KalkulatorInputDump() {
        // for proxys
    }

    @Inject
    public KalkulatorInputDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {
        var sql = """
                select k.kobling_referanse, ki.input, ki.opprettet_tid, ki.aktiv from kobling k
                inner join kalkulator_input ki on k.id = ki.kobling_id
                    where k.saksnummer = :saksnummer
                    order by ki.opprettet_tid asc ;
                   """;

        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());
        String path = "kalkulatorinput.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return List.of();
        }

        log.info("Fant innhold for KalkulatorInputDump for saksnummer " + saksnummer);


        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());
    }

}
