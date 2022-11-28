alter table if exists TILKOMMET_INNTEKT
DROP COLUMN er_tilkommet;

alter table if exists TILKOMMET_INNTEKT
    ADD skal_redusere_utbetaling BOOLEAN NOT NULL;

comment on column TILKOMMET_INNTEKT.skal_redusere_utbetaling is 'Vurdering om inntektsforholdet skal nedjustere utbetaling';
