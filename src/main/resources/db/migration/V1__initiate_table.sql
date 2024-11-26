
CREATE TABLE concert_entity (
    id varchar(100) not null,
    title varchar(255),
    date date,
    genre clob,
    link varchar(255),
    location varchar(255),
    notified boolean not null,
    price varchar(255),
    primary key (id)
);

