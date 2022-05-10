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
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class KalkulatorInputDump implements DebugDumpSak {

    private static final Logger log = LoggerFactory.getLogger(KalkulatorInputDump.class);

    private EntityManager entityManager;
    private KoblingRepository koblingRepository;

    public KalkulatorInputDump() {
        // for proxys
    }

    @Inject
    public KalkulatorInputDump(EntityManager entityManager, KoblingRepository koblingRepository) {
        this.entityManager = entityManager;
        this.koblingRepository = koblingRepository;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {
        var koblinger = koblingRepository.hentAlleKoblingerForSaksnummer(saksnummer);
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        TypedQuery<Tuple> query = entityManager.createQuery("from KalkulatorInput where koblingId in :koblingId",
                        Tuple.class)
                .setParameter("koblingId", koblingIder);

        String path = "kalkulatorinput.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            log.info("Fant ingen innhold for KalkulatorInputDump for saksnummer " + saksnummer);
            return List.of();
        }

        log.info("Fant innhold for KalkulatorInputDump for saksnummer " + saksnummer);


        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());
    }

}
