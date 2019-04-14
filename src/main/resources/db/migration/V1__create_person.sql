CREATE TABLE PERSON (
  id SERIAL PRIMARY KEY,
  first_name TEXT,
  last_name TEXT,
  gender TEXT
);

CREATE TABLE PERSON_TWITTER_INFO (
  id SERIAL PRIMARY KEY,
  screen_name TEXT,
  user_id BIGINT,
  foreign key (user_id) references PERSON(id)
);