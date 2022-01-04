create index IDX_GR_BEREGNINGSGRUNNLAG_09 on GR_BEREGNINGSGRUNNLAG (steg_opprettet);
create index IDX_GR_BEREGNINGSGRUNNLAG_10 on GR_BEREGNINGSGRUNNLAG (opprettet_tid);
create index IDX_GR_BEREGNINGSGRUNNLAG_11 on GR_BEREGNINGSGRUNNLAG (kobling_id, steg_opprettet, opprettet_tid);
