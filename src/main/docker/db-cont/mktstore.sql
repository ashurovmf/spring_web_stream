CREATE USER clnnode WITH PASSWORD 'hotCave13'; 
CREATE DATABASE mktstore; 
GRANT ALL PRIVILEGES ON DATABASE mktstore to clnnode;
-- SIMPLE TABLE FOR USERS
CREATE TABLE IF NOT EXISTS users
(
  username character varying(45) NOT NULL,
  password character varying(45) NOT NULL,
  enabled smallint NOT NULL DEFAULT 1,
  id integer NOT NULL,
  CONSTRAINT users_pk PRIMARY KEY (id),
  CONSTRAINT uniq_credent UNIQUE (username, password)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE users
  OWNER TO clnnode;
GRANT ALL ON TABLE users TO clnnode;
GRANT ALL ON TABLE users TO public;

-- SIMPLE TABLE FOR USER ROLES
CREATE TABLE IF NOT EXISTS user_roles
(
  id integer NOT NULL,
  user_id integer NOT NULL,
  role character varying(45) NOT NULL,
  CONSTRAINT user_roles_pk PRIMARY KEY (id),
  CONSTRAINT user_id_fk FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uniq_user UNIQUE (role, user_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE user_roles
  OWNER TO clnnode;
GRANT ALL ON TABLE user_roles TO clnnode;
GRANT ALL ON TABLE user_roles TO public;

-- Index: inx_user_roles

-- DROP INDEX inx_user_roles;

CREATE INDEX inx_user_roles
  ON user_roles
  USING btree
  (user_id);


-- VIEW TO USER AND ROLE MAPPINGS
CREATE OR REPLACE VIEW userroles_v AS 
 SELECT u.username,
    r.role
   FROM users u
     JOIN user_roles r ON r.user_id = u.id;

ALTER TABLE userroles_v
  OWNER TO clnnode;
GRANT ALL ON TABLE userroles_v TO clnnode;
GRANT ALL ON TABLE userroles_v TO public;

INSERT INTO users(id,username,password,enabled) 
	VALUES (1,'miav','123456',1),
			(2,'alex','123456',1);

INSERT INTO user_roles(id,user_id,role) 
	VALUES (1,1,'ROLE_USER'),
			(2,1,'ROLE_ADMIN'),
			(3,2,'ROLE_USER');
