package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

@ApplicationScoped
public class KalkulatorInputDump implements DebugDumpSak {

    private EntityManager entityManager;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    public KalkulatorInputDump() {
        // for proxys
    }

    @Inject
    public KalkulatorInputDump(EntityManager entityManager, KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.entityManager = entityManager;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {
        var inputPrKobling = kalkulatorInputTjeneste.hentJsonInputForSak(saksnummer);
        var resultatListe = inputPrKobling.entrySet().stream()
                .map(e -> new DumpOutput(String.format("kobling-%s/kalkulator-input.json", e.getKey()), e.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));

        var sql = """
                select cast(k.kobling_referanse as varchar)  ekstern_referanse,
                        k.id kobling_id,
                        cast(ki.input as varchar),
                        ki.aktiv,
                        ki.opprettet_tid
                     from KOBLING k
                              inner join KALKULATOR_INPUT ki on ki.kobling_id = k.id
                    where k.saksnummer = :saksnummer
                    order by ki.opprettet_tid asc ;
                   """;


        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());

        String path = "kalkulatorinput.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return resultatListe;
        }

        resultatListe.addAll(CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of()));

        return resultatListe;
    }

}
