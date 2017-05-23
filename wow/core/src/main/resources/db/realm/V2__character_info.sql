begin;

create type races as enum ('Human', 'Orc', 'Dwarf', 'Nightelf', 'UndeadPlayer', 'Tauren', 'Gnome', 'Troll', 'Bloodelf', 'Draenei');

create type genders as enum ('Male', 'Female');

create type classes as enum ('Warrior', 'Paladin', 'Hunter', 'Rogue', 'Priest', 'DeathKnight', 'Shaman', 'Mage', 'Warlock', 'Druid');

create table character_info
(
  guid        serial primary key,
  account_id  int references ${authSchemaName}.account (id),
  name        varchar(12) not null unique,
  race        races       not null,
  clazz       classes     not null,
  gender      genders     not null,
  skin        int         not null,
  face        int         not null,
  hair_style  int         not null,
  hair_color  int         not null,
  facial_hair int         not null,
  map_id      int         not null,
  x           float       not null,
  y           float       not null,
  z           float       not null,
  orientation float       not null,
  deleted_at  timestamp
);

commit;
