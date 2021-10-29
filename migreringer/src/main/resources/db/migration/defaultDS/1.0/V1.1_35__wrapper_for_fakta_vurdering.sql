create table if not exists FAKTA_VURDERING
(
    ID                              BIGINT                                  NOT NULL,
    VURDERING                       BOOLEAN                                 NOT NULL,
    KILDE                           VARCHAR(100)                            NOT NULL,
    VERSJON                         BIGINT       DEFAULT 0                  NOT NULL,
    OPPRETTET_AV                    VARCHAR(20)  DEFAULT 'VL'               NOT NULL,
    OPPRETTET_TID                   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    ENDRET_AV                       VARCHAR(20),
    ENDRET_TID                      TIMESTAMP(3)
    );
create UNIQUE index PK_FAKTA_VURDERING on FAKTA_VURDERING (ID);
alter table FAKTA_VURDERING add constraint PK_FAKTA_VURDERING primary key using index PK_FAKTA_VURDERING;
create sequence if not exists SEQ_FAKTA_VURDERING increment by 50 minvalue 1000000;

comment on table FAKTA_VURDERING is 'Tabell som lagrer faktaavklaringer for arbeidsforhold';
comment on column FAKTA_VURDERING.ID is 'Primary Key';
comment on column FAKTA_VURDERING.VURDERING is 'Vurderingen av fakta';
comment on column FAKTA_VURDERING.KILDE is 'Kilde til vurderingen, enten saksbehandler eller kalkulator';

ALTER TABLE FAKTA_ARBEIDSFORHOLD ADD FK_ER_TIDSBEGRENSET BIGINT;
alter table FAKTA_ARBEIDSFORHOLD add constraint FK_FAKTA_ARBEIDSFORHOLD_02 foreign key (FK_ER_TIDSBEGRENSET) references FAKTA_VURDERING (ID);
comment on column FAKTA_ARBEIDSFORHOLD.FK_ER_TIDSBEGRENSET is 'FK: Referanse til vurdering av om arbeidsforhold er tidsbegrenset';
ALTER TABLE FAKTA_ARBEIDSFORHOLD ADD FK_HAR_MOTTATT_YTELSE BIGINT;
alter table FAKTA_ARBEIDSFORHOLD add constraint FK_FAKTA_ARBEIDSFORHOLD_03 foreign key (FK_HAR_MOTTATT_YTELSE) references FAKTA_VURDERING (ID);
comment on column FAKTA_ARBEIDSFORHOLD.FK_HAR_MOTTATT_YTELSE is 'FK: Referanse til vurdering av om det er mottatt ytelse for arbeidsforhold';
ALTER TABLE FAKTA_ARBEIDSFORHOLD ADD FK_HAR_LONNSENDRING_I_BEREGNINGSPERIODEN BIGINT;
alter table FAKTA_ARBEIDSFORHOLD add constraint FK_FAKTA_ARBEIDSFORHOLD_04 foreign key (FK_HAR_LONNSENDRING_I_BEREGNINGSPERIODEN) references FAKTA_VURDERING (ID);
comment on column FAKTA_ARBEIDSFORHOLD.FK_HAR_LONNSENDRING_I_BEREGNINGSPERIODEN is 'FK: Referanse til vurdering av om arbeidsforhold har lønnsendring i beregningsperioden';

ALTER TABLE FAKTA_AKTOER ADD FK_ER_NY_I_ARBEIDSLIVET_SN BIGINT;
alter table FAKTA_AKTOER add constraint FK_FAKTA_AKTOER_02 foreign key (FK_ER_NY_I_ARBEIDSLIVET_SN) references FAKTA_VURDERING (ID);
comment on column FAKTA_AKTOER.FK_ER_NY_I_ARBEIDSLIVET_SN is 'FK: Referanse til vurdering av om bruker er ny i arbeidslivet.';
ALTER TABLE FAKTA_AKTOER ADD FK_ER_NYOPPSTARTET_FL BIGINT;
alter table FAKTA_AKTOER add constraint FK_FAKTA_AKTOER_03 foreign key (FK_ER_NYOPPSTARTET_FL) references FAKTA_VURDERING (ID);
comment on column FAKTA_AKTOER.FK_ER_NYOPPSTARTET_FL is 'FK: Referanse til vurdering av om bruker er nyoppstartet frilans.';
ALTER TABLE FAKTA_AKTOER ADD FK_HAR_FL_MOTTATT_YTELSE BIGINT;
alter table FAKTA_AKTOER add constraint FK_FAKTA_AKTOER_04 foreign key (FK_HAR_FL_MOTTATT_YTELSE) references FAKTA_VURDERING (ID);
comment on column FAKTA_AKTOER.FK_HAR_FL_MOTTATT_YTELSE is 'FK: Referanse til vurdering av om det er mottatt ytelse for frilans.';
ALTER TABLE FAKTA_AKTOER ADD FK_MOTTAR_ETTERLONN_SLUTTPAKKE BIGINT;
alter table FAKTA_AKTOER add constraint FK_FAKTA_AKTOER_05 foreign key (FK_MOTTAR_ETTERLONN_SLUTTPAKKE) references FAKTA_VURDERING (ID);
comment on column FAKTA_AKTOER.FK_MOTTAR_ETTERLONN_SLUTTPAKKE is 'FK: Referanse til vurdering av om bruker mottar etterlønn/sluttpakke.';
ALTER TABLE FAKTA_AKTOER ADD FK_SKAL_BEREGNES_SOM_MILITAER BIGINT;
alter table FAKTA_AKTOER add constraint FK_FAKTA_AKTOER_06 foreign key (FK_SKAL_BEREGNES_SOM_MILITAER) references FAKTA_VURDERING (ID);
comment on column FAKTA_AKTOER.FK_SKAL_BEREGNES_SOM_MILITAER is 'FK: Referanse til vurdering av om bruker skal beregnes som militær.';
