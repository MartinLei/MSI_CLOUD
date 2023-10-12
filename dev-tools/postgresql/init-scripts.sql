CREATE TABLE IF NOT EXISTS fileitem (
    id int not null,
    name varchar(255),
    data varchar(255),
    PRIMARY KEY (id)
);

--INSERT DUMMY DATEN
INSERT INTO fileitem VALUES ('1','Test1 Name','data1'), ('2','Test2 Name','data2');