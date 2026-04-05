create table script_executions (
    id uuid primary key,
    source_code text not null,
    status varchar(32) not null,
    stdout text,
    stderr text,
    stack_trace text,
    created_at timestamp with time zone not null,
    started_at timestamp with time zone,
    completed_at timestamp with time zone
);
