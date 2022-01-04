package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.PersistenceException;

class PayloadUtil {

    static String getPayload(Clob payload, AtomicReference<String> payloadStringRef) {
        var payloadString = payloadStringRef.get();
        if (payloadString != null && !payloadString.isBlank()) {
            return payloadString; // quick return, deserialisert tidligere
        }

        if (payload == null || (payloadString != null && payloadString.isEmpty())) {
            return null; // quick return, har ikke eller er tom
        }

        payloadString = ""; // dummy value for å signalisere at er allerede deserialisert
        try {
            BufferedReader in = new BufferedReader(payload.getCharacterStream());
            String line;
            StringBuilder sb = new StringBuilder(2048);
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            payloadString = sb.toString();
        } catch (SQLException | IOException e) {
            throw new PersistenceException("Kunne ikke lese payload: ", e);
        }
        payloadStringRef.set(payloadString);
        return payloadString;

    }
}
