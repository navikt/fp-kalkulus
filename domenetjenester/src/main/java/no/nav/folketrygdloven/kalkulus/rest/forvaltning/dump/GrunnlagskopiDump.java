package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

@ApplicationScoped
public class GrunnlagskopiDump implements DebugDumpSak {

    private EntityManager entityManager;


    public GrunnlagskopiDump() {
        // for proxys
    }

    @Inject
    public GrunnlagskopiDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {

        var sql = """
                select cast(k.kobling_referanse as varchar)  kopiert_til_ekstern_referanse,
                        kopi.kopiert_til_kobling_id kopiert_til,
                        kopi.kopiert_fra_kobling_id kopiert_fra,
                        kopi.kopiert_grunnlag_id kopiert_grunnlag_id,
                        gr.steg_opprettet kopiert_grunnlag_tilstand
                     from KOBLING k
                              inner join KOBLING_GRUNNLAGSKOPI_SPORING kopi on kopi.kopiert_til_kobling_id = k.id
                              inner join GR_BEREGNINGSGRUNNLAG gr on gr.id = kopi.kopiert_grunnlag_id
                    where k.saksnummer = :saksnummer and kopi.aktiv = true
                   """;


        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());

        String path = "grunnlagskopi.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());
    }

}
