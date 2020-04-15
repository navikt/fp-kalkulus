package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn;

import java.util.Collection;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class MapBeregningAktiviteterFraVLTilRegelFRISINN extends MapBeregningAktiviteterFraVLTilRegel {

    public MapBeregningAktiviteterFraVLTilRegelFRISINN() {
        super();
    }

    @Override
    protected AktivPeriode lagAktivPeriode(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(opptjeningsperiode.getOpptjeningAktivitetType());
        Periode gjeldendePeriode = opptjeningsperiode.getPeriode();

        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forFrilanser(gjeldendePeriode);
        } else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            var opptjeningArbeidsgiverAktørId = opptjeningsperiode.getArbeidsgiverAktørId();
            var opptjeningArbeidsgiverOrgnummer = opptjeningsperiode.getArbeidsgiverOrgNummer();
            var opptjeningArbeidsforhold = Optional.ofNullable(opptjeningsperiode.getArbeidsforholdId()).orElse(InternArbeidsforholdRefDto.nullRef());
            return lagAktivPeriodeForArbeidstaker(inntektsmeldinger, gjeldendePeriode, opptjeningArbeidsgiverAktørId,
                opptjeningArbeidsgiverOrgnummer, opptjeningArbeidsforhold);
        } else {
            return AktivPeriode.forAndre(aktivitetType, gjeldendePeriode);
        }
    }

}
