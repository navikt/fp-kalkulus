create UNIQUE index PK_BG_REFUSJON_OVERSTYRING on BG_REFUSJON_OVERSTYRING (ID);
alter table BG_REFUSJON_OVERSTYRING
    add constraint PK_BG_REFUSJON_OVERSTYRING primary key using index PK_BG_REFUSJON_OVERSTYRING;

create table if not exists BG_REFUSJON_PERIODE
(
                id BIGINT NOT NULL PRIMARY KEY,
                BG_REFUSJON_OVERSTYRING_ID BIGINT NOT NULL REFERENCES BG_REFUSJON_OVERSTYRING (id),
                arbeidsforhold_intern_id  UUID,
                fom TIMESTAMP(0) NOT NULL,
                VERSJON  BIGINT DEFAULT 0 NOT NULL,
                OPPRETTET_AV VARCHAR(20) DEFAULT 'VL' NOT NULL,
                OPPRETTET_TID TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
                ENDRET_AV VARCHAR(20),
                ENDRET_TID TIMESTAMP (3)

);
COMMENT ON TABLE BG_REFUSJON_PERIODE  IS 'Tabell som holder på hvilke refusjonskrav som skal gjelde fra hvilken dato gitt arbeidsgiver og arbeidsforhold';
COMMENT ON COLUMN BG_REFUSJON_PERIODE.arbeidsforhold_intern_id  IS 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';
COMMENT ON COLUMN BG_REFUSJON_PERIODE.fom IS 'Fra og med datoen refusjon skal tas med i beregningen';
COMMENT ON COLUMN BG_REFUSJON_PERIODE.BG_REFUSJON_OVERSTYRING_ID IS 'Foreign key til tabell BG_REFUSJON_OVERSTYRING';
create sequence if not exists SEQ_BG_REFUSJON_PERIODE increment by 50 minvalue 1000000;
create index IDX_BG_REFUSJON_PERIODE_1 on BG_REFUSJON_PERIODE (BG_REFUSJON_OVERSTYRING_ID);
alter table BG_REFUSJON_OVERSTYRING alter column fom drop not null;
