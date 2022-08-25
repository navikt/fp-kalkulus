ALTER TABLE IF EXISTS AVKLARINGSBEHOV
ADD ER_TRUKKET BOOLEAN;
comment on column AVKLARINGSBEHOV.ER_TRUKKET is 'Kun relevant for overstyring. Spesifiserer om handling for overstyring er trukket/avbrutt.';
