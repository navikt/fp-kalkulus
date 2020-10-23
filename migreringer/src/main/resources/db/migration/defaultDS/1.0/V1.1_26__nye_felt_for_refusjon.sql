ALTER TABLE IF EXISTS BG_ANDEL_ARBEIDSFORHOLD
ADD COLUMN IF NOT EXISTS saksbehandlet_refusjon_pr_aar NUMERIC(19, 2);
comment on column BG_ANDEL_ARBEIDSFORHOLD.saksbehandlet_refusjon_pr_aar is 'Refusjonsbeløp satt som følge av å ha vurdert refusjonskravet og refusjonsbeløpet';

ALTER TABLE IF EXISTS BG_ANDEL_ARBEIDSFORHOLD
ADD COLUMN IF NOT EXISTS fordelt_refusjon_pr_aar NUMERIC(19, 2);
comment on column BG_ANDEL_ARBEIDSFORHOLD.fordelt_refusjon_pr_aar is 'Refusjonsbeløp satt i henhold til fordelingsregler';
