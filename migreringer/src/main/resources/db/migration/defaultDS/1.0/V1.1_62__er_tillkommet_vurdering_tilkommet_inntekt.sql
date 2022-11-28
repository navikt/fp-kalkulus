alter table if exists TILKOMMET_INNTEKT
ADD er_tilkommet BOOLEAN NOT NULL;

alter table if exists TILKOMMET_INNTEKT
    ALTER COLUMN BRUTTO_INNTEKT_PR_AAR DROP NOT NULL;

alter table if exists TILKOMMET_INNTEKT
ALTER COLUMN TILKOMMET_INNTEKT_PR_AAR DROP NOT NULL;

comment on column TILKOMMET_INNTEKT.er_tilkommet is 'Vurdering om inntektsforholdet skal regnes som tilkommet og om det skal nedjustere utbetaling';
