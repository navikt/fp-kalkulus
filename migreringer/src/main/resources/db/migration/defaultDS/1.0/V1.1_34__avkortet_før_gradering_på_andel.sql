alter table BG_PR_STATUS_OG_ANDEL add AVKORTET_FOER_GRADERING_PR_AAR NUMERIC(19, 2);

comment on column BG_PR_STATUS_OG_ANDEL.AVKORTET_FOER_GRADERING_PR_AAR is 'Beløp etter avkorting før gradering mot utbetalingsgrad';
