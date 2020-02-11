package no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.NaturalId;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;

@Entity(name = "Kobling")
@Table(name = "KOBLING")
public class KoblingEntitet extends BaseEntitet implements IndexKey {

    /**
     * Kalkulus intern kobling_id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KOBLING")
    private Long id;

    /**
     * Saksnummer (gruppererer alle koblinger under samme saksnummer - typisk generert av FPSAK, eller annet saksbehandlingsystem)
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer")))
    private Saksnummer saksnummer;

    /**
     * Ekstern Referanse (eks. behandlingUuid).
     */
    @NaturalId
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "referanse", column = @Column(name = "kobling_referanse", updatable = false, unique = true))
    })
    private KoblingReferanse koblingReferanse;

    @Convert(converter = YtelseTyperKalkulusStøtter.KodeverdiConverter.class)
    @Column(name="ytelse_type", nullable = false)
    private YtelseTyperKalkulusStøtter ytelseTyperKalkulusStøtter;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "bruker_aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public KoblingEntitet() {
    }

    public KoblingEntitet(KoblingReferanse koblingReferanse, YtelseTyperKalkulusStøtter ytelseTyperKalkulusStøtter, Saksnummer saksnummer, AktørId aktørId) {
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(ytelseTyperKalkulusStøtter, "ytelseTyperKalkulusStøtter");
        this.ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(ytelseTyperKalkulusStøtter.getKode());
        this.saksnummer = saksnummer;
        this.koblingReferanse = koblingReferanse;
        this.aktørId = aktørId;
    }

    @Override
    public String getIndexKey() {
        return String.valueOf(koblingReferanse);
    }

    public Long getId() {
        return id;
    }

    public KoblingReferanse getKoblingReferanse() {
        return koblingReferanse;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public YtelseTyperKalkulusStøtter getYtelseTyperKalkulusStøtter() {
        return ytelseTyperKalkulusStøtter;
    }

    public void setYtelseTyperKalkulusStøtter(YtelseTyperKalkulusStøtter ytelseTyperKalkulusStøtter) {
        this.ytelseTyperKalkulusStøtter = ytelseTyperKalkulusStøtter;
    }

    @Override
    public String toString() {
        return "Kobling{" +
                "KoblingReferanse=" + koblingReferanse +
                ", aktørId=" + aktørId +
                ", saksnummer = " + saksnummer +
                '}';
    }
}
