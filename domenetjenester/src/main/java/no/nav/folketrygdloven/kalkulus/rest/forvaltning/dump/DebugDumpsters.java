package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

@ApplicationScoped
public class DebugDumpsters {

    private static final Logger log = LoggerFactory.getLogger(DebugDumpsters.class);

    private @Any
    Instance<DebugDumpSak> dumpere;

    protected DebugDumpsters() {
        //
    }

    @Inject
    public DebugDumpsters(@Any Instance<DebugDumpSak> dumpere) {
        this.dumpere = dumpere;
    }

    public StreamingOutput dumper(Saksnummer saksnummer) {
        var dumpsters = dumpere.stream().toList();
        List<DumpOutput> allDumps = dumpOutput(saksnummer, dumpsters);

        return new ZipOutput().dump(saksnummer, allDumps);
    }

    private List<DumpOutput> dumpOutput(Saksnummer saksnummer, List<DebugDumpSak> dumpers) {
        var dumperNames = dumpers.stream().map(d -> d.getClass().getName()).collect(Collectors.toList());
        log.info("Dumper fra: {}", dumperNames);

        List<DumpOutput> allDumps = dumpers.stream().flatMap(ddp -> {
            try {
                return ddp.dump(saksnummer).stream();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                return Stream.of(new DumpOutput(ddp.getClass().getSimpleName() + "-ERROR.txt", sw.toString()));
            }
        }).collect(Collectors.toList());
        return allDumps;
    }

}
