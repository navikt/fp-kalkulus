alter table avklaringsbehov add column vurdert_av varchar(20);
alter table avklaringsbehov add column vurdert_tid timestamp(3);

update avklaringsbehov set vurdert_av = endret_av, vurdert_tid = endret_tid where avklaringsbehov_status = 'UTFO';
