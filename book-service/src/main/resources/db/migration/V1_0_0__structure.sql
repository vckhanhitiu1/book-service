create table book(
                     id bigint primary key identity(1,1),
                     name nvarchar(255) not null,
                     publication_year smallint not null,
                     status nvarchar(50) not null default 'Active',
                     created_at datetime2 not null default sysutcdatetime(),
                     updated_at datetime2 not null default sysutcdatetime()
);

create table author(
                       id bigint primary key identity(1,1),
                       last_name nvarchar(255) not null,
                       first_name nvarchar(255) not null,
                       middle_name nvarchar(255),
                       country nvarchar(255) not null,
                       date_of_birth date not null,
                       created_at datetime2 not null default sysutcdatetime(),
                       updated_at datetime2 not null default sysutcdatetime()
);

create table book_author(
                            id bigint primary key identity(1,1),
                            book_id bigint references book(id) not null,
                            author_id bigint references author(id) not null,
                            created_at datetime2 not null default sysutcdatetime(),
                            updated_at datetime2 not null default sysutcdatetime()
);

create table book_loan(
                          id bigint primary key identity(1,1),
                          book_id bigint references book(id) not null,
                          user_id bigint not null,
                          status nvarchar(50) not null default 'Active',
                          created_at datetime2 not null default sysutcdatetime(),
                          updated_at datetime2 not null default sysutcdatetime()
);

create table user_replica(
                             id bigint primary key,
                             status nvarchar(50) not null
);

create table inbox(
                      id uniqueidentifier primary key,
                      source nvarchar(255) not null,
                      type nvarchar(255) not null,
                      payload nvarchar(max) not null,
                      status nvarchar(50) not null default 'New',
                      error nvarchar(255),
                      processed_by nvarchar(255),
                      version smallint not null default 0,
                      created_at datetime2 not null default sysutcdatetime(),
                      updated_at datetime2 not null default sysutcdatetime()
);

create table outbox(
                       id uniqueidentifier primary key default NEWID(),
                       aggregate_type nvarchar(255) not null,
                       aggregate_id nvarchar(255) not null,
                       type nvarchar(255) not null,
                       payload nvarchar(max) not null,
                       created_at datetime2 not null default sysutcdatetime(),
                       updated_at datetime2 not null default sysutcdatetime()
);