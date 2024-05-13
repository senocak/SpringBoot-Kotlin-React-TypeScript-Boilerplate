create table if not exists roles(
    id         uuid not null  primary key,
    created_at timestamp default now(),
    updated_at timestamp default now(),
    name       varchar(255) constraint roles_name_check check ((name)::text = ANY ((ARRAY ['ROLE_USER'::character varying, 'ROLE_ADMIN'::character varying])::text[]))
);

create table if not exists users(
    id         uuid not null primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    email      varchar(255) constraint uk_email_user unique,
    name       varchar(255),
    password   varchar(255),
    email_activated_at timestamp
);

create table audit_revision_entity(
    id         integer not null primary key,
    timestamp  bigint  not null,
    updated_at timestamp(6)
);

create table user_audit(
    rev                integer not null constraint fk_user_audit_rev references audit_revision_entity,
    revtype            smallint,
    email_activated_at timestamp(6),
    id                 uuid    not null,
    email              varchar(255),
    name               varchar(255),
    password           varchar(255),
    primary key (rev, id)
);

create table user_roles_aud (
    rev     integer not null constraint fk_user_roles_audit_rev references audit_revision_entity,
    revtype smallint,
    role_id uuid    not null,
    user_id uuid    not null,
    primary key (rev, role_id, user_id)
);

create table email_activation_tokens_aud(
    rev             integer not null constraint fk_email_activation_tokens_audit_rev references audit_revision_entity,
    revtype         smallint,
    expiration_date timestamp(6),
    id              uuid    not null,
    user_id         uuid,
    token           varchar(64),
    primary key (rev, id)
);

create table if not exists user_roles(
    user_id uuid not null constraint fk_user_roles_user_user_id references users,
    role_id uuid not null constraint fk_user_roles_user_role_id references roles,
    primary key (user_id, role_id)
);

create sequence audit_revision_entity_seq increment by 50;

create table if not exists email_activation_tokens (
    id uuid NOT NULL primary key ,
    created_at timestamp default now(),
    updated_at timestamp default now(),
    expiration_date timestamp not null,
    token character varying(64) NOT NULL constraint uk_email_activation_tokens_token unique ,
    user_id uuid NOT NULL constraint fk_email_activation_tokens_user_user_id references users(id) on delete cascade
);
