
CREATE TABLE concert_entity (
    id varchar(100) not null,
    title varchar(255),
    date date,
    genre clob,
    link varchar(255),
    location varchar(255),
    notified boolean not null,
    support_bands varchar(1000),
    primary key (id)
);

CREATE UNIQUE INDEX unique_title_date ON concert_entity(title, date);
