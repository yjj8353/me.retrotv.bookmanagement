CREATE SEQUENCE IF NOT EXISTS public.hibernate_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE IF NOT EXISTS public.author (
	author_id int8 NOT NULL,
	author_name varchar(30) NOT NULL,
	author_regist_date timestamp NOT NULL,
	author_update_date timestamp NOT NULL,
	CONSTRAINT author_pkey PRIMARY KEY (author_id),
	CONSTRAINT uk_ccl4rp0t1cxnkcud4m5r9e9ls UNIQUE (author_name)
);

CREATE TABLE IF NOT EXISTS public.image (
	image_id int8 NOT NULL,
	image_format varchar(10) NULL,
	image_name varchar(255) NOT NULL,
	image_path varchar(255) NOT NULL,
	image_proxy_name varchar(255) NOT NULL,
	image_regist_date timestamp NOT NULL,
	image_update_date timestamp NOT NULL,
	CONSTRAINT image_pkey PRIMARY KEY (image_id),
	CONSTRAINT uk_t82w53ycwln1mtrup6awau8i5 UNIQUE (image_proxy_name)
);

CREATE TABLE IF NOT EXISTS public."member" (
	member_id int8 NOT NULL,
	member_email varchar(255) NOT NULL,
	member_certified bool NULL,
	member_nick_name varchar(30) NULL,
	member_passcode varchar(255) NULL,
	member_password varchar(255) NOT NULL,
	member_real_name varchar(30) NULL,
	member_refresh_token varchar(255) NULL,
	member_regist_date timestamp NOT NULL,
	member_role varchar(255) NULL,
	member_update_date timestamp NOT NULL,
	member_username varchar(20) NOT NULL,
	CONSTRAINT member_pkey PRIMARY KEY (member_id),
	CONSTRAINT uk_3orqjaukiw2b73e2gw8rer4rq UNIQUE (member_email),
	CONSTRAINT uk_je04nuwy8t3on74g3cahhutj5 UNIQUE (member_username)
);

CREATE TABLE IF NOT EXISTS public.publisher (
	publisher_id int8 NOT NULL,
	publisher_name varchar(30) NOT NULL,
	publisher_regist_date timestamp NOT NULL,
	publisher_update_date timestamp NOT NULL,
	CONSTRAINT publisher_pkey PRIMARY KEY (publisher_id),
	CONSTRAINT uk_r71ni5g7t7grhu0aj3auc2ine UNIQUE (publisher_name)
);

CREATE TABLE IF NOT EXISTS public.refresh_token (
	refresh_token_id int8 NOT NULL,
	"token" varchar(255) NULL,
	user_id varchar(255) NULL,
	CONSTRAINT refresh_token_pkey PRIMARY KEY (refresh_token_id)
);

CREATE TABLE IF NOT EXISTS public.users (
	user_id int8 NOT NULL,
	user_email varchar(255) NULL,
	user_name varchar(255) NULL,
	user_nick_name varchar(255) NULL,
	user_password varchar(255) NULL,
	user_real_name varchar(255) NULL,
	user_refresh_token varchar(255) NULL,
	user_regist_date timestamp NULL,
	"role" varchar(255) NULL,
	user_update_date timestamp NULL,
	CONSTRAINT uk_k8d0f2n7n88w1a16yhua64onx UNIQUE (user_name),
	CONSTRAINT users_pkey PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS public.book (
	book_id int8 NOT NULL,
	book_isbn varchar(13) NULL,
	book_regist_date timestamp NOT NULL,
	book_title varchar(255) NOT NULL,
	book_update_date timestamp NOT NULL,
	image_id int8 NULL,
	member_id int8 NULL,
	publisher_id int8 NULL,
	CONSTRAINT book_pkey PRIMARY KEY (book_id),
	CONSTRAINT fkgtvt7p649s4x80y6f4842pnfq FOREIGN KEY (publisher_id) REFERENCES public.publisher(publisher_id),
	CONSTRAINT fkpdmglejuicm0m2wwgvosuv0nq FOREIGN KEY (member_id) REFERENCES public."member"(member_id),
	CONSTRAINT fksb67nrrvdpm7qkhtmekewbwco FOREIGN KEY (image_id) REFERENCES public.image(image_id)
);

CREATE TABLE IF NOT EXISTS public.book_author (
	author_id int8 NOT NULL,
	book_id int8 NOT NULL,
	CONSTRAINT book_author_pkey PRIMARY KEY (author_id, book_id),
	CONSTRAINT fkbjqhp85wjv8vpr0beygh6jsgo FOREIGN KEY (author_id) REFERENCES public.author(author_id),
	CONSTRAINT fkhwgu59n9o80xv75plf9ggj7xn FOREIGN KEY (book_id) REFERENCES public.book(book_id)
);
