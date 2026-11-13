package no.nav.folketrygdloven.kalkulus.domene.mapTilEntitet;

import static no.nav.folketrygdloven.kalkulus.domene.mappers.VerdityperMapper.beløpTilDao;

import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.FordelingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.RefusjonsandelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.RefusjonsperiodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.IntervallEntitet;

public final class FordelingsgrunnlagMapper {

    private FordelingsgrunnlagMapper() {
    }

    public static FordelingGrunnlagEntitet.Builder map(FordelingGrunnlag grunnlag) {
        var builder = FordelingGrunnlagEntitet.builder();
        grunnlag.refusjonsandeler().stream().map(FordelingsgrunnlagMapper::mapAndel).forEach(builder::leggTilRefusjonsandel);
        return builder;
    }

    private static RefusjonsandelEntitet mapAndel(Refusjonsandel andel) {
        var builder = RefusjonsandelEntitet.builder()
            .medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(andel.arbeidsgiver()))
            .medArbeidsforholdRef(KalkulatorTilIAYMapper.mapArbeidsforholdRef(andel.arbeidsforholdRef()))
            .medKilde(andel.kilde())
            .medJournalpostId(andel.journalpostId() == null ? null : andel.journalpostId().getId());
        andel.refusjonsperioder().stream().map(FordelingsgrunnlagMapper::mapPeriode).forEach(builder::leggTilRefusjonsperiode);
        return builder.build();
    }

    private static RefusjonsperiodeEntitet mapPeriode(Refusjonsperiode periode) {
        return RefusjonsperiodeEntitet.builder()
            .medPeriode(IntervallEntitet.fraOgMedTilOgMed(periode.periode().getFomDato(), periode.periode().getTomDato()))
            .medRefusjonPrMnd(beløpTilDao(periode.refusjonPrMnd()))
            .medUtfall(periode.utfall())
            .medUtfallÅrsak(periode.utfallÅrsak())
            .build();
    }
}
