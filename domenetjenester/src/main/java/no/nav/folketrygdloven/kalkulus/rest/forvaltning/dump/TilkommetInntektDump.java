package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

@ApplicationScoped
public class TilkommetInntektDump implements DebugDumpSak {

    private EntityManager entityManager;


    public TilkommetInntektDump() {
        // for proxys
    }

    @Inject
    public TilkommetInntektDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {

        var sql = """
                select cast(k.kobling_referanse as varchar)  ekstern_referanse,
                        k.id kobling_id,
                        periode.bg_periode_fom fom,
                        periode.bg_periode_tom tom,
                        ti.aktivitetStatus,
                        ti.arbeidsgiver_orgnr orgnr,
                        ti.arbeidsgiver_aktor_id aktørId,
                        ti.arbeidsforholdRef,
                        ti.brutto_inntekt_pr_aar brutto,
                        ti.tilkommet_inntekt_pr_aar tilkommet,
                        ti.skal_redusere_utbetaling reduserer
                     from KOBLING k
                              inner join GR_BEREGNINGSGRUNNLAG gr on gr.kobling_id = k.id
                              inner join BEREGNINGSGRUNNLAG_PERIODE periode on periode.beregningsgrunnlag_id = gr.beregningsgrunnlag_id
                              inner join TILKOMMET_INNTEKT ti on ti.bg_periode_id = periode.id
                    where k.saksnummer = :saksnummer and gr.aktiv = true;
                   """;


        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());

        String path = "tilkommetinntekt.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());
    }

}
