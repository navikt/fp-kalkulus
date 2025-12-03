package no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling;

import java.util.Objects;
import java.util.Optional;

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

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.FagsakYtelseTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@Entity(name = "Kobling")
@Table(name = "KOBLING")
public class KoblingEntitet extends BaseEntitet implements IndexKey {

    /**
     * Kalkulus intern kobling_id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
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
    @AttributeOverrides({@AttributeOverride(name = "referanse", column = @Column(name = "kobling_referanse", updatable = false, unique = true))})
    private KoblingReferanse koblingReferanse;

    @Convert(converter = FagsakYtelseTypeKodeverdiConverter.class)
    @Column(name = "ytelse_type", nullable = false)
    private FagsakYtelseType ytelseType;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "referanse", column = @Column(name = "original_kobling_referanse", updatable = false))})
    private KoblingReferanse originalKoblingReferanse;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "bruker_aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Column(name = "er_avsluttet", nullable = false)
    private boolean erAvsluttet = false;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    protected KoblingEntitet() {
        // CDI
    }

    public KoblingEntitet(KoblingReferanse koblingReferanse, FagsakYtelseType ytelseType, Saksnummer saksnummer, AktørId aktørId) {
        this(koblingReferanse, ytelseType, saksnummer, aktørId, Optional.empty());
    }

    public KoblingEntitet(KoblingReferanse koblingReferanse,
                          FagsakYtelseType ytelseType,
                          Saksnummer saksnummer,
                          AktørId aktørId,
                          Optional<KoblingReferanse> originalKoblingReferanse) {
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(ytelseType, "fagsakYtelseType");
        if (FagsakYtelseType.UDEFINERT.equals(ytelseType)) {
            throw new IllegalArgumentException("Udefinert ytelse");
        }
        this.ytelseType = ytelseType;
        this.saksnummer = saksnummer;
        this.koblingReferanse = koblingReferanse;
        this.aktørId = aktørId;
        originalKoblingReferanse.ifPresent(kr -> this.originalKoblingReferanse = kr);
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

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public Optional<KoblingReferanse> getOriginalKoblingReferanse() {
        return Optional.ofNullable(originalKoblingReferanse);
    }

    public boolean getErAvsluttet() {
        return erAvsluttet;
    }

    public void setErAvsluttet(boolean erAvsluttet) {
        this.erAvsluttet = erAvsluttet;
    }

    @Override
    public String toString() {
        return "Kobling{" + "KoblingReferanse=" + koblingReferanse + ", aktørId=" + aktørId + ", saksnummer = " + saksnummer + '}';
    }
}
