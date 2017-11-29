create database mipmap_test;

use mipmap_test;

CREATE TABLE `patients` (
  `idpatients` int(11) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `gender` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idpatients`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `exams` (
  `idexams` int(11) NOT NULL,
  `patient` int(11) DEFAULT NULL,
  `exam_measurement` varchar(45) DEFAULT NULL,
  `exam_value` varchar(45) DEFAULT NULL,
  `exam_date` date DEFAULT NULL,
  PRIMARY KEY (`idexams`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

insert into patients values (2561591,"Peter Quill", "Male"),
(2684672,"Vincent Nichols","Male"),
(260428,"Derrick Matthews","Male"),
(861093,"Matilda Webster","Female"),
(604704,"Effie Evans","Female"),
(163081,"Charlotte Bailey","Female"),
(559677,"George Harrington","Male"),
(2742705,"Esther Silva","Female"),
(629792,"Rebecca Barrett","Female"),
(418877,"Roger Bryan","Male"),
(2326096,"Mary Nunez","Female"),
(481145,"Susan Wagner","Female"),
(2877889,"John Mann","Male"),
(763227,"Rosetta Schultz","Female"),
(1040371,"Edward Webster","Male"),
(286768,"Leon Newman","Male"),
(1033854,"Bertha Yates","Female"),
(448702,"Trevor Sandoval","Male"),
(2746803,"Isabelle Pierce","Female"),
(960386,"Matthew Delgado","Male"),
(593656,"Emily Logan","Female"),
(2619235,"Amy Morgan","Female"),
(166595,"Florence Cook","Female"),
(919019,"Gordon Leonard","Male");

insert into exams values (2,629792,"CDRSB",3,"2010-03-25"),
(3,2746803,"MOCA",28,"2010-03-25"),
(4,559677,"CDRSB",3,"2010-03-26"),
(5,2561591,"APOE4",1,"2010-03-26"),
(6,1033854,"MOCA",28,"2010-03-28"),
(7,593656,"APOE4",0,"2010-03-29"),
(8,2561591,"CDRSB",3,"2010-04-02");
