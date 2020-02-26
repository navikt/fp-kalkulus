ALTER TABLE KALKULATOR_INPUT
    ADD COLUMN AKTIV BOOLEAN DEFAULT TRUE NOT NULL;


CREATE UNIQUE INDEX UIDX_KALKULATOR_INPUT_01
    ON KALKULATOR_INPUT (
                          (CASE
                               WHEN AKTIV is true
                                   THEN kobling_id
                               ELSE NULL END),
                          (CASE
                               WHEN AKTIV is true
                                   THEN AKTIV
                               ELSE NULL END)
        );

comment on column KALKULATOR_INPUT.AKTIV is 'Ture hvis det er denne inputten som brukes';
