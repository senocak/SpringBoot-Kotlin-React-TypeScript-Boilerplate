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

create table if not exists user_roles(
    user_id uuid not null constraint fk_user_roles_user_user_id references users,
    role_id uuid not null constraint fk_user_roles_user_role_id references roles,
    primary key (user_id, role_id)
);

create table if not exists email_activation_tokens (
    id uuid NOT NULL primary key ,
    created_at timestamp default now(),
    updated_at timestamp default now(),
    expiration_date timestamp not null,
    token character varying(64) NOT NULL constraint uk_email_activation_tokens_token unique ,
    user_id uuid NOT NULL constraint fk_email_activation_tokens_user_user_id references users(id) on delete cascade
);
