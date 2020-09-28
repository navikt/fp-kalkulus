ALTER TABLE REGEL_SPORING_GRUNNLAG ALTER COLUMN REGEL_EVALUERING DROP NOT NULL;

insert into REGEL_SPORING_GRUNNLAG (ID, KOBLING_ID, REGEL_EVALUERING, REGEL_INPUT, REGEL_TYPE, AKTIV)
(select nextval('SEQ_REGEL_SPORING_GRUNNLAG'), gr.kobling_id,
lo_from_bytea(0, sporing.REGEL_EVALUERING::text::bytea),
lo_from_bytea(0, sporing.REGEL_INPUT::text::bytea), sporing.REGEL_TYPE, true
from GR_BEREGNINGSGRUNNLAG gr
inner join BEREGNINGSGRUNNLAG bg on gr.beregningsgrunnlag_id = bg.id
inner join BG_REGEL_SPORING sporing on sporing.bg_id = bg.id
where gr.aktiv = true
and not exists (select 1 from REGEL_SPORING_GRUNNLAG rs where rs.kobling_id = gr.kobling_id and rs.regel_type = sporing.regel_type));

insert into REGEL_SPORING_PERIODE (ID, KOBLING_ID, FOM, TOM, REGEL_EVALUERING, REGEL_INPUT, REGEL_TYPE, AKTIV)
(
select nextval('SEQ_BG_PERIODE_REGEL_SPORING'), gr.kobling_id,
periode.bg_periode_fom, periode.bg_periode_tom,
lo_from_bytea(0, sporing.REGEL_EVALUERING::text::bytea),
lo_from_bytea(0, sporing.REGEL_INPUT::text::bytea), sporing.REGEL_TYPE, true
from GR_BEREGNINGSGRUNNLAG gr
inner join BEREGNINGSGRUNNLAG bg on gr.beregningsgrunnlag_id = bg.id
inner join BEREGNINGSGRUNNLAG_PERIODE periode on periode.beregningsgrunnlag_id = bg.id
inner join BG_PERIODE_REGEL_SPORING sporing on sporing.bg_periode_id = bg.id
where gr.aktiv = true
and not exists (select 1 from REGEL_SPORING_PERIODE rs
where rs.kobling_id = gr.kobling_id
and rs.regel_type = sporing.regel_type
and periode.bg_periode_fom = rs.fom)
);
