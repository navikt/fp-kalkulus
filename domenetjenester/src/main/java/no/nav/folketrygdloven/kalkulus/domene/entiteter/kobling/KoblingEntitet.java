package no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.NaturalId;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.YtelseTyperKalkulusStøtterKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

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
     * Ekstern Referanse - caller's angitte identitet for dette behandlingsgrunnlaget. Kan f.eks. være knyttet til en behandling
     * (behandlingUuid), men også mer finkornet dersom det fins flere grunnlag per behandling (eks. Omsorgspenger i K9)
     */
    @NaturalId
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "referanse", column = @Column(name = "kobling_referanse", updatable = false, unique = true))
    })
    private KoblingReferanse koblingReferanse;

    @Convert(converter = YtelseTyperKalkulusStøtterKodeverdiConverter.class)
    @Column(name = "ytelse_type", nullable = false)
    private YtelseTyperKalkulusStøtterKontrakt ytelseTyperKalkulusStøtter;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "bruker_aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public KoblingEntitet() {
    }

    public KoblingEntitet(KoblingReferanse koblingReferanse, YtelseTyperKalkulusStøtterKontrakt ytelseTyperKalkulusStøtter, Saksnummer saksnummer, AktørId aktørId, boolean erForlengelse) {
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(ytelseTyperKalkulusStøtter, "ytelseTyperKalkulusStøtter");
        this.ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtterKontrakt.fraKode(ytelseTyperKalkulusStøtter.getKode());
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

    public YtelseTyperKalkulusStøtterKontrakt getYtelseTyperKalkulusStøtter() {
        return ytelseTyperKalkulusStøtter;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public void setYtelseTyperKalkulusStøtter(YtelseTyperKalkulusStøtterKontrakt ytelseTyperKalkulusStøtter) {
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
