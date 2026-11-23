GRANT SELECT, INSERT (id, email, pen_name, pen_name_key, password_hash)
    ON TABLE public.accounts TO writesphere_api;
GRANT SELECT, INSERT, UPDATE ON TABLE public.works TO writesphere_api;
GRANT SELECT ON TABLE public.published_works TO writesphere_api;
GRANT SELECT, INSERT (account_id, work_id), DELETE ON TABLE public.bookmarks TO writesphere_api;
GRANT SELECT, INSERT (id, work_id, reporter_id, reason, created_at), UPDATE (decision, resolved_at, moderator_id)
    ON TABLE public.reports TO writesphere_api;
GRANT SELECT, INSERT, UPDATE (name, display_name) ON TABLE public.user_entities TO writesphere_api;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.user_credentials TO writesphere_api;
