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
public class BeregningsgrunnlagDump implements DebugDumpSak {

    private static final Logger log = LoggerFactory.getLogger(BeregningsgrunnlagDump.class);

    private EntityManager entityManager;

    public BeregningsgrunnlagDump() {
        // for proxys
    }

    @Inject
    public BeregningsgrunnlagDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(Saksnummer saksnummer) {
        var sql = """
                select     k.kobling_referanse                                         koblingreferanse,
                            gr.opprettet_tid                                            grunnlag_opprettet_tid,
                            gr.endret_tid                                               grunnlag_endret_tid,
                            gr.aktiv                                                    grunnlag_er_aktiv,
                            gr.steg_opprettet                                           grunnlag_steg_opprettet,
                            b.overstyrt                                                 beregning_er_overstyrt,
                            b.skjaringstidspunkt,
                            status.aktivitet_status                                     beregning_aktivitet_status,
                            periode.bg_periode_fom,
                            periode.bg_periode_tom,
                            andel.aktivitet_status                                      aktivitet_status_andel,
                            andel.dagsats_arbeidsgiver,
                            andel.dagsats_bruker,
                            andel.avkortet_pr_aar,
                            andel.avkortet_foer_gradering_pr_aar,
                            coalesce(andel.inntektskategori_manuell_fordeling,
                                     andel.inntektskategori_fordeling,
                                     andel.inntektskategori)                            gjeldende_inntektskategori,
                            andel.beregnet_pr_aar,
                            andel.overstyrt_pr_aar,
                            andel.fordelt_pr_aar,
                            coalesce(arb.arbeidsgiver_orgnr, arb.arbeidsgiver_aktor_id) arbeidsgiverident,
                            arbeidsforhold_intern_id,
                            coalesce(manuelt_fordelt_refusjon_pr_aar, saksbehandlet_refusjon_pr_aar, fordelt_refusjon_pr_aar, refusjonskrav_pr_aar) refusjon_pr_aar
                     from kobling k
                              inner join gr_beregningsgrunnlag gr on gr.kobling_id = k.id
                              inner join beregningsgrunnlag b on gr.beregningsgrunnlag_id = b.id
                              inner join bg_aktivitet_status status on b.id = status.beregningsgrunnlag_id
                              inner join beregningsgrunnlag_periode periode on b.id = periode.beregningsgrunnlag_id
                              inner join bg_pr_status_og_andel andel on periode.id = andel.bg_periode_id
                              left join bg_andel_arbeidsforhold arb on andel.id = arb.bg_andel_id
                    where k.saksnummer = :saksnummer
                    order by b.opprettet_tid asc ;
                   """;

        var query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("saksnummer", saksnummer.getVerdi());
        String path = "beregningsgrunnlag.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return List.of();
        }

        log.info("Fant innhold for BeregningsgrunnlagDump for saksnummer " + saksnummer);

        return CsvOutput.dumpResultSetToCsv(path, results)
                .map(List::of).orElse(List.of());
    }

}
