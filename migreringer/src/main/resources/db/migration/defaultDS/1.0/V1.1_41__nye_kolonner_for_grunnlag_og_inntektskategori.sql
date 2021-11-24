ALTER TABLE IF EXISTS BG_PR_STATUS_OG_ANDEL
    ADD COLUMN IF NOT EXISTS manuelt_fordelt_pr_aar NUMERIC(19, 2);

comment on column BG_PR_STATUS_OG_ANDEL.manuelt_fordelt_pr_aar is 'Manuelt fordelt beregningsgrunnlag.';


ALTER TABLE IF EXISTS BG_PR_STATUS_OG_ANDEL
    ADD COLUMN IF NOT EXISTS inntektskategori_manuell_fordeling VARCHAR(100);

comment on column BG_PR_STATUS_OG_ANDEL.inntektskategori_manuell_fordeling is 'Inntektskategori satt ved manuell fordeling.';

ALTER TABLE IF EXISTS BG_PR_STATUS_OG_ANDEL
    ADD COLUMN IF NOT EXISTS inntektskategori_fordeling VARCHAR(100);

comment on column BG_PR_STATUS_OG_ANDEL.inntektskategori_fordeling is 'Inntektskategori satt ved automatisk fordeling.';

ALTER TABLE IF EXISTS BG_ANDEL_ARBEIDSFORHOLD
    ADD COLUMN IF NOT EXISTS manuelt_fordelt_refusjon_pr_aar NUMERIC(19, 2);
comment on column BG_ANDEL_ARBEIDSFORHOLD.manuelt_fordelt_refusjon_pr_aar is 'Refusjonsbeløp satt av saksbehandler i fordeling.';
