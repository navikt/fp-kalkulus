package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaOmBeregningDto {

    @JsonProperty(value = "kortvarigeArbeidsforhold")
    private List<KortvarigeArbeidsforholdDto> kortvarigeArbeidsforhold;

    @JsonProperty(value = "frilansAndel")
    private FaktaOmBeregningAndelDto frilansAndel;

    @JsonProperty(value = "kunYtelse")
    private KunYtelseDto kunYtelse;

    @JsonProperty(value = "faktaOmBeregningTilfeller")
    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;

    @JsonProperty(value = "arbeidstakerOgFrilanserISammeOrganisasjonListe")
    private List<ATogFLISammeOrganisasjonDto> arbeidstakerOgFrilanserISammeOrganisasjonListe;

    @JsonProperty(value = "arbeidsforholdMedLønnsendringUtenIM")
    private List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIM;

    @JsonProperty(value = "vurderMottarYtelse")
    private VurderMottarYtelseDto vurderMottarYtelse;

    @JsonProperty(value = "avklarAktiviteter")
    private AvklarAktiviteterDto avklarAktiviteter;

    @JsonProperty(value = "vurderBesteberegning")
    private VurderBesteberegningDto vurderBesteberegning;

    @JsonProperty(value = "andelerForFaktaOmBeregning")
    private List<AndelForFaktaOmBeregningDto> andelerForFaktaOmBeregning;

    @JsonProperty(value = "vurderMilitaer")
    private VurderMilitærDto vurderMilitaer;

    @JsonProperty(value = "refusjonskravSomKommerForSentListe")
    private List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSentListe;

    public FaktaOmBeregningDto() {
        // Hibernate
    }

    public List<RefusjonskravSomKommerForSentDto> getRefusjonskravSomKommerForSentListe() {
        return refusjonskravSomKommerForSentListe;
    }

    public void setRefusjonskravSomKommerForSentListe(List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSentListe) {
        this.refusjonskravSomKommerForSentListe = refusjonskravSomKommerForSentListe;
    }


    public List<AndelForFaktaOmBeregningDto> getAndelerForFaktaOmBeregning() {
        return andelerForFaktaOmBeregning;
    }

    public void setAndelerForFaktaOmBeregning(List<AndelForFaktaOmBeregningDto> andelerForFaktaOmBeregning) {
        this.andelerForFaktaOmBeregning = andelerForFaktaOmBeregning;
    }

    public VurderBesteberegningDto getVurderBesteberegning() {
        return vurderBesteberegning;
    }

    public void setVurderBesteberegning(VurderBesteberegningDto vurderBesteberegning) {
        this.vurderBesteberegning = vurderBesteberegning;
    }

    public KunYtelseDto getKunYtelse() {
        return kunYtelse;
    }

    public void setKunYtelse(KunYtelseDto kunYtelse) {
        this.kunYtelse = kunYtelse;
    }

    public List<KortvarigeArbeidsforholdDto> getKortvarigeArbeidsforhold() {
        return kortvarigeArbeidsforhold;
    }

    public void setKortvarigeArbeidsforhold(List<KortvarigeArbeidsforholdDto> kortvarigeArbeidsforhold) {
        this.kortvarigeArbeidsforhold = kortvarigeArbeidsforhold;
    }

    public FaktaOmBeregningAndelDto getFrilansAndel() {
        return frilansAndel;
    }

    public void setFrilansAndel(FaktaOmBeregningAndelDto frilansAndel) {
        this.frilansAndel = frilansAndel;
    }

    public List<FaktaOmBeregningAndelDto> getArbeidsforholdMedLønnsendringUtenIM() {
        return arbeidsforholdMedLønnsendringUtenIM;
    }

    public void setArbeidsforholdMedLønnsendringUtenIM(List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIM) {
        this.arbeidsforholdMedLønnsendringUtenIM = arbeidsforholdMedLønnsendringUtenIM;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public void setFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }

    public List<ATogFLISammeOrganisasjonDto> getArbeidstakerOgFrilanserISammeOrganisasjonListe() {
        return arbeidstakerOgFrilanserISammeOrganisasjonListe;
    }

    public void setArbeidstakerOgFrilanserISammeOrganisasjonListe(List<ATogFLISammeOrganisasjonDto> aTogFLISammeOrganisasjonListe) {
        this.arbeidstakerOgFrilanserISammeOrganisasjonListe = aTogFLISammeOrganisasjonListe;
    }

    public VurderMottarYtelseDto getVurderMottarYtelse() {
        return vurderMottarYtelse;
    }

    public void setVurderMottarYtelse(VurderMottarYtelseDto vurderMottarYtelse) {
        this.vurderMottarYtelse = vurderMottarYtelse;
    }

    public AvklarAktiviteterDto getAvklarAktiviteter() {
        return avklarAktiviteter;
    }

    public void setAvklarAktiviteter(AvklarAktiviteterDto avklarAktiviteter) {
        this.avklarAktiviteter = avklarAktiviteter;
    }

    public VurderMilitærDto getVurderMilitaer() {
        return vurderMilitaer;
    }

    public void setVurderMilitaer(VurderMilitærDto vurderMilitaer) {
        this.vurderMilitaer = vurderMilitaer;
    }

}
