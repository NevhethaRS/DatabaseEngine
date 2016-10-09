The file to be compiled is DavisBase.java using the following command in the console
	javac DavisBase.java

This creates DavisBase.class file.
To run the program in the console
	java DavisBase

The application is highly case-sensitive and space sensitive.
It supports following functionalities and the format for specifying the functionalities are to be followed strictly.

--To show the existing schemas
	show schemas;

--To show the existing tables in the selected database
	show tables;

--To switch to use a particular schema
	use schema <schema_name>;
	(e.g.) use schema university;

--To create a new schema
	create schema <schema_name>;
	(e.g.) create schema university;

--To create a new table
	create table <table_name>(<attribute_name> <attribute_type> <primary_key_		constraint> <not_null_constraint>);
	(e.g.) create table students(id int primarykey,name varchar(25),bdate date,credits short);

primary key constraint is to be represented as’primarykey’
not null constraint is to be represented as’notnull’

--To insert a record into the table
	insert into <table_name> values(<value1>,<value2>);
	(e.g.) insert into students values(1,’Jason Day’,’1995-08-21’,32);

Unlike in mysql,this application requires null values to be specified explicitly.

String, datetime and date values are to be enclosed in ‘’;

--To select all records from a table
	select * from <table_name>
	(e.g.) select * from university;

--To select a particular record from the table
	select * from <table_name> where <attribute_name><operator><value>;
	(e.g.) select * from university where id=1;

Supported operators are ==,!=,>=,<=,>,<.
