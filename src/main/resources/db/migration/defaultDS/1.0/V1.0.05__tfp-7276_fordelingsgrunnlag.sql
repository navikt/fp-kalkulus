create table gr_fordeling
(
    id            bigint constraint pk_gr_fordeling primary key,
    kobling_id    bigint not null constraint fk_gr_fordeling_1 references kobling,
    aktiv         boolean default true not null,
    versjon       int default 0 not null,
    opprettet_av  varchar(20) default 'VL' not null,
    opprettet_tid timestamp(3) default CURRENT_TIMESTAMP not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table gr_fordeling is 'Fordelingsgrunnlag for en kobling';

comment on column gr_fordeling.kobling_id is 'FK: KOBLING. Koblingen fordelingsgrunnlaget tilhører';
comment on column gr_fordeling.aktiv is 'Angir om dette er det aktive fordelingsgrunnlaget for koblingen';

create index idx_gr_fordeling_kobling on gr_fordeling (kobling_id);
create unique index uidx_gr_fordeling_aktiv on gr_fordeling (kobling_id) where aktiv;

create table refusjon_andel
(
    id                       bigint constraint pk_refusjon_andel primary key,
    fordeling_grunnlag_id     bigint not null constraint fk_refusjon_andel_1 references gr_fordeling,
    arbeidsgiver_orgnr       varchar(9),
    arbeidsgiver_aktoer_id   varchar(13),
    arbeidsforhold_intern_id uuid,
    kilde                    varchar(50) not null,
    journalpost_id           varchar(50),
    versjon                  int default 0 not null,
    opprettet_av             varchar(20) default 'VL' not null,
    opprettet_tid            timestamp(3) default CURRENT_TIMESTAMP not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3)
);

comment on table refusjon_andel is 'Refusjonskrav per arbeidsgiver, arbeidsforhold og kilde';

comment on column refusjon_andel.fordeling_grunnlag_id is 'FK: GR_FORDELING. Fordelingsgrunnlaget refusjonsandelen tilhører';
comment on column refusjon_andel.arbeidsgiver_orgnr is 'Organisasjonsnummer når arbeidsgiver er en virksomhet';
comment on column refusjon_andel.arbeidsgiver_aktoer_id is 'Aktør-ID når arbeidsgiver er en privatperson';
comment on column refusjon_andel.arbeidsforhold_intern_id is 'Intern arbeidsforholdsreferanse.';
comment on column refusjon_andel.kilde is 'Kilden til refusjonskravet';
comment on column refusjon_andel.journalpost_id is 'Journalpost for inntektsmeldingen refusjonskravet kommer fra';

create index idx_refusjon_andel_grunnlag on refusjon_andel (fordeling_grunnlag_id);

create table refusjon_andel_periode
(
    id                 bigint constraint pk_refusjon_andel_periode primary key,
    refusjon_andel_id   bigint not null constraint fk_refusjon_andel_periode_1 references refusjon_andel,
    fom                date not null,
    tom                date not null,
    refusjon_pr_mnd     numeric(19, 2) not null,
    utfall             varchar(50) not null,
    utfall_aarsak       varchar(50),
    versjon            int default 0 not null,
    opprettet_av       varchar(20) default 'VL' not null,
    opprettet_tid      timestamp(3) default CURRENT_TIMESTAMP not null,
    endret_av          varchar(20),
    endret_tid         timestamp(3)
);

comment on table refusjon_andel_periode is 'Refusjonsbelop og vurdering for en periode';

comment on column refusjon_andel_periode.refusjon_andel_id is 'FK: REFUSJON_ANDEL. Refusjonsandelen perioden tilhører';
comment on column refusjon_andel_periode.fom is 'Første dato i refusjonsperioden, inkludert';
comment on column refusjon_andel_periode.tom is 'Siste dato i refusjonsperioden, inkludert';
comment on column refusjon_andel_periode.refusjon_pr_mnd is 'Refusjonskrav per måned i kroner';
comment on column refusjon_andel_periode.utfall is 'Utfallet for refusjonskravet i perioden';
comment on column refusjon_andel_periode.utfall_aarsak is 'Årsak til utfallet for refusjonskravet';

create index idx_refusjon_andel_periode_andel on refusjon_andel_periode (refusjon_andel_id);
