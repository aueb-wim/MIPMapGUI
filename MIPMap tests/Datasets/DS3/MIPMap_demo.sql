CREATE DATABASE "MIPMap_demo"
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'Greek_Greece.1253'
       LC_CTYPE = 'Greek_Greece.1253'
       CONNECTION LIMIT = -1;
	   
	   -- Table: exam_measurement

-- DROP TABLE exam_measurement;

CREATE TABLE exam_measurement
(
  id character varying(16) NOT NULL,
  lab_id character varying(16),
  variable_name character varying(32),
  loinc_num character varying(10),
  description character varying(512),
  record_creation timestamp without time zone DEFAULT now(),
  CONSTRAINT exam_measurement_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE exam_measurement
  OWNER TO postgres;

  -- Table: patient

-- DROP TABLE patient;

CREATE TABLE patient
(
  id character varying(32) NOT NULL,
  year_of_birth integer,
  gender character varying(16),
  city character varying(32),
  extracted_from character varying(32),
  extraction_method character varying(32),
  description character varying(512),
  record_creation timestamp without time zone DEFAULT now(),
  CONSTRAINT patient_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE patient
  OWNER TO postgres;

  -- Table: exam_value

-- DROP TABLE exam_value;

CREATE TABLE exam_value
(
  patient_id character varying(32),
  exam_measurement_id character varying(16),
  value character varying(32),
  variable_name character varying(32),
  status character varying(32),
  exam_date date,
  extracted_from character varying(32),
  extraction_method character varying(32),
  description character varying(512),
  record_creation timestamp without time zone DEFAULT now(),
  CONSTRAINT exam_value_exam_measurement_id_fkey FOREIGN KEY (exam_measurement_id)
      REFERENCES exam_measurement (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT exam_value_patient_id_fkey FOREIGN KEY (patient_id)
      REFERENCES patient (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE exam_value
  OWNER TO postgres;
