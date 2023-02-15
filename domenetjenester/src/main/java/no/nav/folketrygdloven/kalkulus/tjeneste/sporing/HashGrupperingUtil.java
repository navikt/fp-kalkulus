package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import static ch.qos.logback.core.encoder.ByteArrayUtil.toHexString;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;


public class HashGrupperingUtil {

    private static final MessageDigest HASH_INSTANCE;

    static {
        try {
            HASH_INSTANCE = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private HashGrupperingUtil() {
    }

    public static Map<String, List<RegelSporingPeriode>> grupperRegelsporingerMD5(List<RegelSporingPeriode> regelSporingPerioder) {
        return grupperRegelsporinger(regelSporingPerioder, HashGrupperingUtil::lagMD5Hash);
    }

    static Map<String, List<RegelSporingPeriode>> grupperRegelsporinger(List<RegelSporingPeriode> regelSporingPerioder, Function<String, String> hashFunction) {
        //grupperer regeltype før det sendes inn til funksjon som har som antagelse at det er mye duplikater i input
        //og det vil det være i større grad om det grupperes på regeltype først, siden input vil være ulik pr type.
        Map<BeregningsgrunnlagPeriodeRegelType, List<RegelSporingPeriode>> prRegelType = regelSporingPerioder.stream().collect(Collectors.groupingBy(RegelSporingPeriode::getRegelType));

        return prRegelType.values()
                .parallelStream()
                .map(sporinger -> grupperPrHash(sporinger, RegelSporingPeriode::getRegelInput, hashFunction))
                .reduce(new HashMap<>(), HashGrupperingUtil::multimapUnion);
    }

    /**
     * Denne funksjonen er laget for å finne riktig hash raskere enn å naivt regne ut for hver input.
     * Det fungerer under antagelsen at input i stor grad har dupliserte elementer som skal hashes.
     * Utnytter at like elementer skal ha samme hash, og at det er raskere å sammenligne enn å regne hashen
     * <p>
     * Har en cutoff på antallet sammenligninger for å også fungere bra når det er større variasjon i input
     */
    static <T, V> Map<String, List<T>> grupperPrHash(List<T> input, Function<T, V> verdiFunksjon, Function<V, String> hashFunksjon) {
        final int maxSammenligningerFørLagerHashDirekte = 5;
        Map<String, List<T>> resultat = new HashMap<>();
        for (T element : input) {
            V verdi = verdiFunksjon.apply(element);
            String hash = null;
            int j = 0;
            var iterator = resultat.entrySet().iterator();
            while (hash == null && iterator.hasNext() && j < maxSammenligningerFørLagerHashDirekte) {
                Map.Entry<String, List<T>> e = iterator.next();
                V kandidatensVerdi = verdiFunksjon.apply(e.getValue().get(0));
                if (verdi.equals(kandidatensVerdi)) {
                    hash = e.getKey();
                }
                j++;
            }
            if (hash == null) {
                hash = hashFunksjon.apply(verdi);
            }
            resultat.computeIfAbsent(hash, key -> new ArrayList<>()).add(element);
        }
        return resultat;
    }

    public static String lagMD5Hash(String input) {
        byte[] md5Hash = HASH_INSTANCE.digest(input.getBytes());
        return toHexString(md5Hash);
    }

    private static <K, V> Map<K, List<V>> multimapUnion(Map<K, List<V>> lhs, Map<K, List<V>> rhs) {
        Map<K, List<V>> resultat = new HashMap<>(lhs);
        for (Map.Entry<K, List<V>> e : rhs.entrySet()) {
            resultat.computeIfAbsent(e.getKey(), (k) -> new ArrayList<>()).addAll(e.getValue());
        }
        return resultat;
    }
}
