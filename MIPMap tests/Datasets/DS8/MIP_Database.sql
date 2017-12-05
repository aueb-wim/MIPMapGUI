-- Table: patient
-- DROP TABLE patient;

CREATE TABLE patient
(
  id text NOT NULL,
  name text,
  year_of_birth integer,
  gender text,
  extraction_method text,
  record_creation timestamp without time zone,
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
  patient_id text,
  measurement_id text,
  value text,
  exam_date date,
  extraction_method text,
  extracted_from text,
  CONSTRAINT exam_value_patient_id_fkey FOREIGN KEY (patient_id)
      REFERENCES patient (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE exam_value
  OWNER TO postgres;