begin;

create table account
(
  id          serial8 primary key,
  login       varchar(64) unique,
  verifier    numeric(100, 0),
  salt        numeric(100, 0),
  session_key numeric(100, 0)
);

commit;
