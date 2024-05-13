/* TABLE */
create table if not exists users_history_trigger (
    id         varchar(255) not null,
    created_at timestamp    not null,
    updated_at timestamp    not null,
    email      varchar(255) constraint uk_email_user unique,
    name       varchar(255),
    password   varchar(255),
    email_activated_at  timestamp,
    operation           varchar(1)
);

/* INSERT TRIGGER SCHEMA */
create function trigger_fct_trg_user_insert() returns trigger
    security definer
    language plpgsql
as
$$
begin
    insert into users_history_trigger(id, email, name, password, created_at, updated_at, email_activated_at, operation)
        values(new.id, new.email, new.name, new.password, new.CREATED_AT, new.updated_at, new.email_activated_at, 'I');
return new;
end
$$;
/* INSERT TRIGGER ON TABLE */
create trigger trg_user_insert before insert on users_history_trigger for each row execute function trigger_fct_trg_user_insert();

/* UPDATE TRIGGER SCHEMA */
create function trigger_fct_trg_user_update() returns trigger
    security definer
    language plpgsql
as
$$
begin
    insert into users_history_trigger(id, email, name, password, created_at, updated_at, email_activated_at, operation)
        values(new.id, new.email, new.name, new.password, new.CREATED_AT, new.updated_at, new.email_activated_at, 'U');
    return new;
end
$$;
/* UPDATE TRIGGER ON TABLE */
create trigger trg_user_update before update on users_history_trigger for each row execute function trigger_fct_trg_user_update();

/* DELETE TRIGGER SCHEMA */
create function trigger_fct_trg_user_delete() returns trigger
    security definer
    language plpgsql
as
$$
begin
    insert into users_history_trigger(id, email, name, password, created_at, updated_at, email_activated_at, operation)
        values(new.id, new.email, new.name, new.password, new.CREATED_AT, new.updated_at, new.email_activated_at, 'D');
return old;
end
$$;
/* DELETE TRIGGER ON TABLE */
create trigger trg_user_delete before delete on users_history_trigger for each row execute function trigger_fct_trg_user_delete();


/* DROP TABLE */
DROP TABLE users_history_trigger;

/* DROP TRIGGERS */
DROP FUNCTION trigger_fct_trg_user_insert() CASCADE;
DROP FUNCTION trigger_fct_trg_user_delete() CASCADE;
DROP FUNCTION trigger_fct_trg_user_delete() CASCADE;
