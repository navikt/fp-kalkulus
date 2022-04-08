create table if not exists DIAGNOSTIKK_SAK_LOGG
(
    ID            BIGINT                                 NOT NULL PRIMARY KEY,
    SAKSNUMMER    varchar(19)                            NOT NULL,
    BEGRUNNELSE   TEXT                                   NOT NULL,
    TJENESTE      varchar(200)                           NOT NULL,
    OPPRETTET_AV  VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV     VARCHAR(20),
    ENDRET_TID    TIMESTAMP(3)
);

create sequence if not exists DIAGNOSTIKK_SAK_LOGG increment by 50 minvalue 1000000;
create index on DIAGNOSTIKK_SAK_LOGG (SAKSNUMMER);
