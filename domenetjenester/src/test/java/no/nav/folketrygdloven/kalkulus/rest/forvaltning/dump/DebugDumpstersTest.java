package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.FastsattInntektskategori;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Refusjon;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.JpaExtension;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.k9.felles.testutilities.cdi.UnitTestLookupInstanceImpl;
import no.nav.k9.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class DebugDumpstersTest extends EntityManagerAwareTest {

    private BeregningsgrunnlagDump beregningsgrunnlagDump;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private DebugDumpsters debugDumpsters;

    @BeforeEach
    void setUp() {
        this.beregningsgrunnlagRepository = new BeregningsgrunnlagRepository(getEntityManager());
        this.beregningsgrunnlagDump = new BeregningsgrunnlagDump(getEntityManager());
        this.debugDumpsters = new DebugDumpsters(new UnitTestLookupInstanceImpl<>(beregningsgrunnlagDump));
    }

    @Test
    void skal_kjøre_dump_og_få_200() {
        KoblingEntitet koblingEntitet = lagKobling();
        lagGrunnlag(koblingEntitet);

        var dump = debugDumpsters.dumper(koblingEntitet.getSaksnummer());

        var response = Response.ok(dump)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment; filename=\"%s.zip\"", koblingEntitet.getSaksnummer().getVerdi()))
                .build();


        assertThat(response.getStatus()).isEqualTo(200);
    }

    private KoblingEntitet lagKobling() {
        var koblingEntitet = new KoblingEntitet(new KoblingReferanse(UUID.randomUUID()), FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
                new Saksnummer("sak"), new AktørId("123456789"));
        getEntityManager().persist(koblingEntitet);
        return koblingEntitet;
    }

    private void lagGrunnlag(KoblingEntitet koblingEntitet) {
        var bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .medGrunnbeløp(BigDecimal.TEN)
                .build();

        var periode = BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(LocalDate.now(), TIDENES_ENDE)
                .build(bg);

        var virksomhet = Arbeidsgiver.virksomhet("112345689");
        var andel = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAndelsnr(1L)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder()
                        .medArbeidsgiver(virksomhet)
                        .medArbeidsforholdRef(InternArbeidsforholdRef.nullRef())
                        .medRefusjon(new Refusjon(new Beløp(10), null, null, null, Hjemmel.F_22_13_6, Utfall.GODKJENT)))
                .medGrunnlagPrÅr(new Årsgrunnlag(new Beløp(10), null, null, null, null, null))
                .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                .medFastsattInntektskategori(new FastsattInntektskategori(Inntektskategori.ARBEIDSTAKER, null, null))
                .build(periode);

        var grunnlagEntitet = BeregningsgrunnlagGrunnlagBuilder.nytt()
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .leggTilAktivitet(BeregningAktivitetEntitet.builder()
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .medArbeidsgiver(virksomhet)
                                .medArbeidsforholdRef(InternArbeidsforholdRef.nullRef())
                                .medPeriode(IntervallEntitet.fraOgMed(LocalDate.now().minusMonths(10))).build()).build());
        beregningsgrunnlagRepository.lagre(koblingEntitet.getId(), grunnlagEntitet, BeregningsgrunnlagTilstand.FASTSATT);
    }
}
