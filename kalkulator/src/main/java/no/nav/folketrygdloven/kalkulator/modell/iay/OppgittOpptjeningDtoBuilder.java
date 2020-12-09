package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.VirksomhetType;

public class OppgittOpptjeningDtoBuilder {

    private final OppgittOpptjeningDto kladd;

    private OppgittOpptjeningDtoBuilder(OppgittOpptjeningDto kladd) {
        this.kladd = kladd;
    }

    public static OppgittOpptjeningDtoBuilder ny() {
        return new OppgittOpptjeningDtoBuilder(new OppgittOpptjeningDto(UUID.randomUUID()));
    }

    public static OppgittOpptjeningDtoBuilder ny(UUID eksternReferanse, LocalDateTime opprettetTidspunktOriginalt) {
        return new OppgittOpptjeningDtoBuilder(new OppgittOpptjeningDto(eksternReferanse, opprettetTidspunktOriginalt));
    }

    public static OppgittOpptjeningDtoBuilder ny(UUID eksternReferanse, OffsetDateTime opprettetTidspunktOriginalt) {
        return new OppgittOpptjeningDtoBuilder(new OppgittOpptjeningDto(eksternReferanse, opprettetTidspunktOriginalt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));
    }


    public OppgittOpptjeningDtoBuilder leggTilAnnenAktivitet(OppgittAnnenAktivitetDto annenAktivitet){
        this.kladd.leggTilAnnenAktivitet(annenAktivitet);
        return this;
    }

    public OppgittOpptjeningDtoBuilder leggTilFrilansOpplysninger(OppgittFrilansDto frilans) {
        this.kladd.leggTilFrilans(frilans);
        return this;
    }

    public OppgittOpptjeningDtoBuilder leggTilEgneNæringer(List<EgenNæringBuilder> builders) {
        builders.forEach(builder -> this.kladd.leggTilEgenNæring(builder.build()));
        return this;
    }

    public OppgittOpptjeningDtoBuilder leggTilEgneNæring(EgenNæringBuilder builder) {
        this.kladd.leggTilEgenNæring(builder.build());
        return this;
    }

    public OppgittOpptjeningDtoBuilder leggTilOppgittArbeidsforhold(OppgittArbeidsforholdBuilder builder) {
        this.kladd.leggTilOppgittArbeidsforhold(builder.build());
        return this;
    }


    public OppgittOpptjeningDto build() {
        return kladd;
    }

    public static class EgenNæringBuilder {
        private final OppgittEgenNæringDto entitet;

        private EgenNæringBuilder(OppgittEgenNæringDto entitet) {
            this.entitet = entitet;
        }

        public static EgenNæringBuilder ny() {
            return new EgenNæringBuilder(new OppgittEgenNæringDto());
        }

        public EgenNæringBuilder medPeriode(Intervall periode) {
            this.entitet.setPeriode(periode);
            return this;
        }

        public EgenNæringBuilder medVirksomhet(String orgNr) {
            return medVirksomhet(new OrgNummer(orgNr));
        }

        public EgenNæringBuilder medVirksomhetType(VirksomhetType type) {
            this.entitet.setVirksomhetType(type);
            return this;
        }

        public EgenNæringBuilder medEndringDato(LocalDate dato) {
            this.entitet.setEndringDato(dato);
            return this;
        }

        public EgenNæringBuilder medBegrunnelse(String begrunnelse) {
            this.entitet.setBegrunnelse(begrunnelse);
            return this;
        }

        public EgenNæringBuilder medNyoppstartet(boolean nyoppstartet) {
            this.entitet.setNyoppstartet(nyoppstartet);
            return this;
        }

        public EgenNæringBuilder medVarigEndring(boolean varigEndring) {
            this.entitet.setVarigEndring(varigEndring);
            return this;
        }

        public EgenNæringBuilder medBruttoInntekt(BigDecimal bruttoInntekt) {
            this.entitet.setBruttoInntekt(bruttoInntekt);
            return this;
        }

        public OppgittEgenNæringDto build() {
            return entitet;
        }

        public EgenNæringBuilder medNyIArbeidslivet(boolean nyIArbeidslivet) {
            this.entitet.setNyIArbeidslivet(nyIArbeidslivet);
            return this;
        }

        public EgenNæringBuilder medVirksomhet(OrgNummer orgNr) {
            this.entitet.setVirksomhetOrgnr(orgNr);
            return this;
        }
    }

    public static class OppgittArbeidsforholdBuilder {
        private OppgittArbeidsforholdDto entitet;

        private OppgittArbeidsforholdBuilder(OppgittArbeidsforholdDto entitet) {
            this.entitet = entitet;
        }

        public static OppgittArbeidsforholdBuilder ny() {
            return new OppgittArbeidsforholdBuilder(new OppgittArbeidsforholdDto());
        }

        public OppgittArbeidsforholdBuilder medPeriode(Intervall periode) {
            this.entitet.setPeriode(periode);
            return this;
        }

        public OppgittArbeidsforholdDto build() {
            return entitet;
        }

        public OppgittArbeidsforholdBuilder medInntekt(BigDecimal inntekt) {
            this.entitet.setInntekt(inntekt);
            return this;
        }
    }
}
