package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class  SvpGrunnlagDto {

    private Long behandlingId;

    private SvpTilretteleggingerDto opprinneligeTilrettelegginger;

    private SvpTilretteleggingerDto overstyrteTilrettelegginger;

    private boolean aktiv = true;

    public SvpTilretteleggingerDto getOpprinneligeTilrettelegginger() {
        return opprinneligeTilrettelegginger;
    }

    public SvpTilretteleggingerDto getOverstyrteTilrettelegginger() {
        return overstyrteTilrettelegginger;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public static class Builder {

        private List<SvpTilretteleggingDto> opprinneligeTilretteleggingListe = new ArrayList<>();
        private List<SvpTilretteleggingDto> overstyrteTilretteleggingerListe = new ArrayList<>();

        private SvpTilretteleggingerDto opprinneligeTilrettelegginger;
        private SvpTilretteleggingerDto overstyrteTilrettelegginger;

        private Long behandlingId;

        public Builder() {
        }

        public Builder(SvpGrunnlagDto eksisterendeGrunnlag) {
            this.behandlingId = eksisterendeGrunnlag.behandlingId;
            this.opprinneligeTilrettelegginger = eksisterendeGrunnlag.opprinneligeTilrettelegginger;
            this.overstyrteTilrettelegginger = eksisterendeGrunnlag.overstyrteTilrettelegginger;

        }

        public Builder medBehandlingId(Long behandlingId) {
            this.behandlingId = behandlingId;
            return this;
        }

        public Builder medOpprinneligeTilrettelegginger(List<SvpTilretteleggingDto> tilrettelegginger) {
            this.opprinneligeTilretteleggingListe = tilrettelegginger;
            return this;
        }

        public Builder medOverstyrteTilrettelegginger(List<SvpTilretteleggingDto> tilrettelegginger) {
            this.overstyrteTilretteleggingerListe = tilrettelegginger;
            return this;
        }

        public SvpGrunnlagDto build() {
            Objects.requireNonNull(behandlingId, "Behandling er påkrevet");

            SvpGrunnlagDto entitet = new SvpGrunnlagDto();
            entitet.behandlingId = this.behandlingId;
            entitet.aktiv = true;

            if (this.opprinneligeTilrettelegginger != null) {
                entitet.opprinneligeTilrettelegginger = opprinneligeTilrettelegginger;
            } else if (!opprinneligeTilretteleggingListe.isEmpty()) {
                SvpTilretteleggingerDto.Builder opprinneligeTrlgBuilder = new SvpTilretteleggingerDto.Builder();
                opprinneligeTrlgBuilder.medTilretteleggingListe(this.opprinneligeTilretteleggingListe);
                entitet.opprinneligeTilrettelegginger = opprinneligeTrlgBuilder.build();
            }

            if  (!overstyrteTilretteleggingerListe.isEmpty()) {
                SvpTilretteleggingerDto.Builder overstyrteTrlgBuilder = new SvpTilretteleggingerDto.Builder();
                overstyrteTrlgBuilder.medTilretteleggingListe(this.overstyrteTilretteleggingerListe);
                entitet.overstyrteTilrettelegginger = overstyrteTrlgBuilder.build();
            } else if (this.overstyrteTilrettelegginger != null) {
                entitet.overstyrteTilrettelegginger = overstyrteTilrettelegginger;
            }


            return entitet;
        }
    }
}
