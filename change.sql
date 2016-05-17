CREATE DATABASE "Handyman"
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;

CREATE SCHEMA IF NOT EXISTS handyweb
  AUTHORIZATION postgres;
  
CREATE TABLE IF NOT EXISTS handyweb.Users
(
	username VARCHAR(254) NOT NULL
	, password CHAR(60) NOT NULL
	, PRIMARY KEY (username)
);

ALTER TABLE handyweb.Users
	ALTER COLUMN password TYPE VARCHAR(60);

CREATE TABLE IF NOT EXISTS handyweb.Clerks
(
	username VARCHAR(254) NOT NULL
		REFERENCES handyweb.Users(username)
		CHECK (length(username) >= 7)
	, name VARCHAR(20) NOT NULL
	, PRIMARY KEY (username)
);
CREATE TABLE IF NOT EXISTS handyweb.Customers
(
	username VARCHAR(254) NOT NULL
		REFERENCES handyweb.Users(username)
	, first_name VARCHAR(35) NOT NULL
	, last_name VARCHAR(35) NOT NULL
	, home_phone VARCHAR(14) --(xxx) xxx-xxxx
	, work_phone VARCHAR(14) --(xxx) xxx-xxxx
	, address VARCHAR(512) NOT NULL
	, PRIMARY KEY (username)
	, CHECK (home_phone IS NOT NULL OR work_phone IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS handyweb.Tools
(
	tool_number INT NOT NULL PRIMARY KEY
	, abbreviated_description VARCHAR(32) NOT NULL 
		CHECK (length(abbreviated_description) >= 1)
	, full_description VARCHAR(512) NOT NULL
		CHECK (length(full_description) >= 1)
	--the next 3 price values are in pennies
	, rental_price INT NOT NULL CHECK (rental_price >0 )
	, purchase_price INT NOT NULL CHECK (purchase_price >0 )
	, deposit_price INT NOT NULL CHECK (deposit_price >0 )
	--computed column not supported in postgresql. This will be handled by a SELECT
	--, sale_price AS purchase_price / 2
	--1 = handtool, 2 = construction, 3 = power tool
	, tool_type INT NOT NULL CHECK (tool_type IN (1, 2, 3))
);
CREATE TABLE IF NOT EXISTS handyweb.Tags
(
	tool_number INT NOT NULL
		REFERENCES handyweb.Tools(tool_number)
	, listed_dt DATE NOT NULL
	, sold_dt DATE NULL
		CHECK (listed_dt <= sold_dt)
	, PRIMARY KEY (tool_number, listed_dt)
);
CREATE TABLE IF NOT EXISTS handyweb.Accessories --for power tools only
(
	tool_number INT NOT NULL
		REFERENCES handyweb.Tools(tool_number)
	, accessory VARCHAR(32) NOT NULL
	, PRIMARY KEY (tool_number, accessory)
);

CREATE TABLE IF NOT EXISTS handyweb.Reservations
(
	reservation_number SERIAL PRIMARY KEY
	, username VARCHAR(254) NOT NULL 
		REFERENCES handyweb.Customers(username)
	, start_dt DATE NOT NULL
	, end_dt DATE NOT NULL
		CHECK (start_dt <= end_dt)
	--pickup_clerk is NULLable because the reservation is created before check out activity
	, pickup_clerk VARCHAR(254) NULL 
		REFERENCES handyweb.Clerks(username)
	, dropoff_clerk VARCHAR(254) NULL 
		REFERENCES handyweb.Clerks(username)
	--null here but only because of the order of events.  the cc info isn't gathered until the end
	, credit_card VARCHAR(16) NULL 
	, exp_dt CHAR(4) NULL
	-- NULL is complete, not null is when the timer expires
	, in_process_timer TIMESTAMP NULL 
);
CREATE TABLE IF NOT EXISTS handyweb.Reservation_Tools
(
	reservation_number INT NOT NULL 
		REFERENCES handyweb.Reservations(reservation_number)
		ON DELETE CASCADE
	, tool_number INT NOT NULL 
		REFERENCES handyweb.Tools(tool_number)
	, at_rental_price INT NOT NULL
	, at_deposit_price INT NOT NULL
	, PRIMARY KEY (reservation_number, tool_number)
); 

CREATE TABLE IF NOT EXISTS handyweb.Service_Order
(
	tool_number INT NOT NULL
		REFERENCES handyweb.Tools(tool_number)
	, start_dt DATE NOT NULL
	, end_dt DATE NOT NULL
		CHECK (start_dt <= end_dt)
	, est_repair_price INT NOT NULL --denominated in pennies
		CHECK (est_repair_price >= 0)
	, PRIMARY KEY (tool_number, start_dt)
);

DO 
$$
BEGIN
CREATE SEQUENCE handyweb.reservation_numbers START 1;
EXCEPTION WHEN duplicate_table THEN
  -- Do nothing, seq is there.
END
$$ LANGUAGE plpgsql;

DO 
$$
BEGIN
CREATE SEQUENCE handyweb.tool_numbers START 1;
EXCEPTION WHEN duplicate_table THEN
  -- Do nothing, seq is there.
END
$$ LANGUAGE plpgsql;

INSERT into handyweb.users 
	SELECT 'jdavenpo@testing.com', 'testing' 
	WHERE NOT EXISTS (SELECT username FROM handyweb.users u WHERE u.username = 'jdavenpo@testing.com');
INSERT INTO handyweb.customers 
	SELECT 'jdavenpo@testing.com', 'Patrick', 'Davenport', '(317)-473-8862', '(386)-385-4956', '415 Olive St, Palatka Fl, 32177'
	WHERE NOT EXISTS (SELECT username FROM handyweb.customers u WHERE u.username = 'jdavenpo@testing.com');

INSERT into handyweb.users 
	SELECT '1234567', '1234567' 
	WHERE NOT EXISTS (SELECT username FROM handyweb.users u WHERE u.username = '1234567');
INSERT INTO handyweb.clerks 
	SELECT '1234567', 'Sarah'
	WHERE NOT EXISTS (SELECT username FROM handyweb.clerks u WHERE u.username = '1234567');

INSERT into handyweb.users 
	SELECT '1234566', '1234566' 
	WHERE NOT EXISTS (SELECT username FROM handyweb.users u WHERE u.username = '1234566');
INSERT INTO handyweb.clerks 
	SELECT '1234566', 'Martha'
	WHERE NOT EXISTS (SELECT username FROM handyweb.clerks u WHERE u.username = '1234566');

INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 1', 			'Hand tool 1 is an ok tool',	111, 		  	1000, 		  		10,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 2', 			'Hand tool 2 is an ok tool',	222, 		  	2000, 		 		20,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 3', 			'Hand tool 3 is an ok tool', 	333, 		  	3000, 		 		30,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 4', 			'Hand tool 4 is an ok tool',	444, 		  	4000, 		  		40,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 5', 			'Hand tool 5 is an ok tool',	555, 		  	5000, 		 		50,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 6', 			'Hand tool 6 is an ok tool', 	666, 		  	6000, 			 	60,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 7', 			'Hand tool 7 is an ok tool',	777, 		  	7000, 		  		70,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 8', 			'Hand tool 8 is an ok tool',	888, 		  	8000, 		 		80,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 9', 			'Hand tool 9 is an ok tool', 	999, 		  	9000, 		 		90,           1);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 10', 		'Hand tool 10 is an ok tool',	1111, 		  	10000, 		  		100,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 11', 		 'Hand tool 11 is an ok tool',	2222, 		  	11000, 		 		110,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 12', 		 'Hand tool 12 is an ok tool', 	3333, 		  	12000, 		 		120,           1);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 13', 		 'Hand tool 13 is an ok tool',	4444, 		  	13000, 		  		130,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 14', 		 'Hand tool 14 is an ok tool',	5555, 		  	14000, 		 		140,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 15', 		 'Hand tool 15 is an ok tool', 	6666, 		  	15000, 		 		150,           1);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 16', 		 'Hand tool 16 is an ok tool', 	7777, 		  	16000, 		 		160,           1);

INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 17', 		 'Hand tool 17 is an ok tool',	8888, 		  	17000, 		 		170,           1);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Hand tool 18', 		 'Hand tool 18 is an ok tool', 	9999, 		  	18000, 		 		180,           1);    	
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 1', 			'Construction tool 1 is an ok tool',	111, 		  	1000, 		  		10,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 2', 			'Construction tool 2 is an ok tool',	222, 		  	2000, 		 		20,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 3', 			'Construction tool 3 is an ok tool', 	333, 		  	3000, 		 		30,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 4', 			'Construction tool 4 is an ok tool',	444, 		  	4000, 		  		40,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 5', 			'Construction tool 5 is an ok tool',	555, 		  	5000, 		 		50,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 6', 			'Construction tool 6 is an ok tool', 	666, 		  	6000, 			 	60,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 7', 			'Construction tool 7 is an ok tool',	777, 		  	7000, 		  		70,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 8', 			'Construction tool 8 is an ok tool',	888, 		  	8000, 		 		80,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 9', 			'Construction tool 9 is an ok tool', 	999, 		  	9000, 		 		90,           2);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 10', 		'Construction tool 10 is an ok tool',	1111, 		  	10000, 		  		100,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 11', 		 'Construction tool 11 is an ok tool',	2222, 		  	11000, 		 		110,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 12', 		 'Construction tool 12 is an ok tool', 	3333, 		  	12000, 		 		120,           2);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 13', 		 'Construction tool 13 is an ok tool',	4444, 		  	13000, 		  		130,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 14', 		 'Construction tool 14 is an ok tool',	5555, 		  	14000, 		 		140,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 15', 		 'Construction tool 15 is an ok tool', 	6666, 		  	15000, 		 		150,           2);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 16', 		 'Construction tool 16 is an ok tool', 	7777, 		  	16000, 		 		160,           2);

INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 17', 		 'Construction tool 17 is an ok tool',	8888, 		  	17000, 		 		170,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Construction tool 18', 		 'Construction tool 18 is an ok tool', 	9999, 		  	18000, 		 		180,           2);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 1', 			'Power tool 1 is an ok tool',	111, 		  	1000, 		  		10,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 2', 			'Power tool 2 is an ok tool',	222, 		  	2000, 		 		20,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 3', 			'Power tool 3 is an ok tool', 	333, 		  	3000, 		 		30,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 4', 			'Power tool 4 is an ok tool',	444, 		  	4000, 		  		40,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 5', 			'Power tool 5 is an ok tool',	555, 		  	5000, 		 		50,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 6', 			'Power tool 6 is an ok tool', 	666, 		  	6000, 			 	60,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 7', 			'Power tool 7 is an ok tool',	777, 		  	7000, 		  		70,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 8', 			'Power tool 8 is an ok tool',	888, 		  	8000, 		 		80,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 9', 			'Power tool 9 is an ok tool', 	999, 		  	9000, 		 		90,           3);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 10', 		'Power tool 10 is an ok tool',	1111, 		  	10000, 		  		100,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 11', 		 'Power tool 11 is an ok tool',	2222, 		  	11000, 		 		110,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 12', 		 'Power tool 12 is an ok tool', 	3333, 		  	12000, 		 		120,           3);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 13', 		 'Power tool 13 is an ok tool',	4444, 		  	13000, 		  		130,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 14', 		 'Power tool 14 is an ok tool',	5555, 		  	14000, 		 		140,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 15', 		 'Power tool 15 is an ok tool', 	6666, 		  	15000, 		 		150,           3);    		
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 16', 		 'Power tool 16 is an ok tool', 	7777, 		  	16000, 		 		160,           3);

INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description, 				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 17', 		 'Power tool 17 is an ok tool',	8888, 		  	17000, 		 		170,           3);
    		
INSERT INTO handyweb.tools(	tool_number, 						abbreviated_description, full_description,				rental_price, 	purchase_price, 	deposit_price, tool_type)
    		VALUES ( 		nextval('handyweb.tool_numbers'), 	'Power tool 18', 		 'Power tool 18 is an ok tool', 	9999, 		  	18000, 		 		180,           3);    	

INSERT INTO handyweb.accessories (tool_number, accessory)
SELECT tool_number, 'Accessory ' || nextval('handyweb.tool_numbers')
FROM handyweb.tools
--create two accessories per tool
CROSS JOIN (SELECT 1 UNION SELECT 2) u(nbr)
WHERE tool_type = 3;