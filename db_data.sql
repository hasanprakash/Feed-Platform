-- 1. Insert seed data into users table (Normal & Celebrity Users)
INSERT INTO users (id, username, is_celebrity, follower_count, created_at) VALUES
(1, 'alice_wonder', false, 2, NOW() - INTERVAL '10 days'),
(2, 'bob_builder', false, 1, NOW() - INTERVAL '9 days'),
(3, 'charlie_brown', false, 1, NOW() - INTERVAL '8 days'),
(4, 'david_backham', false, 1, NOW() - INTERVAL '7 days'),
(5, 'elon_musketeer', true, 4, NOW() - INTERVAL '6 days'), -- Celebrity
(6, 'taylor_swiftly', true, 4, NOW() - INTERVAL '5 days');  -- Celebrity

-- Sync the ID sequence to prevent duplicate key violations on subsequent app-driven inserts
SELECT setval(pg_get_serial_sequence('users', 'id'), coalesce(max(id), 1)) FROM users;


-- 2. Insert follow relations into followers table
INSERT INTO followers (follower_id, following_id, created_at) VALUES
(1, 5, NOW() - INTERVAL '4 days'), -- alice follows elon
(1, 6, NOW() - INTERVAL '4 days'), -- alice follows taylor
(2, 5, NOW() - INTERVAL '4 days'), -- bob follows elon
(2, 6, NOW() - INTERVAL '4 days'), -- bob follows taylor
(3, 5, NOW() - INTERVAL '3 days'), -- charlie follows elon
(3, 6, NOW() - INTERVAL '3 days'), -- charlie follows taylor
(4, 5, NOW() - INTERVAL '2 days'), -- david follows elon
(4, 6, NOW() - INTERVAL '2 days'), -- david follows taylor
(1, 2, NOW() - INTERVAL '2 days'), -- alice follows bob
(2, 3, NOW() - INTERVAL '2 days'), -- bob follows charlie
(3, 4, NOW() - INTERVAL '1 day'),  -- charlie follows david
(4, 1, NOW() - INTERVAL '1 day');  -- david follows alice

SELECT setval(pg_get_serial_sequence('followers', 'id'), coalesce(max(id), 1)) FROM followers;


-- 3. Insert ~15 posts from different users (Normal & Celebrity)
INSERT INTO posts (author_id, content, created_at) VALUES
(1, 'Hello world! This is Alice first post.', NOW() - INTERVAL '9 days 2 hours'),
(2, 'Can we build it? Yes we can!', NOW() - INTERVAL '8 days 4 hours'),
(5, 'To the moon! 🚀', NOW() - INTERVAL '6 days 1 hour'), -- Elon post (Celebrity)
(6, 'Welcome to the Eras Tour!', NOW() - INTERVAL '5 days 3 hours'), -- Taylor post (Celebrity)
(3, 'Good grief, it is raining today.', NOW() - INTERVAL '5 days'),
(1, 'Enjoying a quiet afternoon with tea.', NOW() - INTERVAL '4 days 6 hours'),
(5, 'Working on some new electric vehicle designs.', NOW() - INTERVAL '4 days 2 hours'),
(2, 'Finished another project today!', NOW() - INTERVAL '3 days 10 hours'),
(6, 'Writing a new song late at night.', NOW() - INTERVAL '3 days 2 hours'),
(4, 'Just had a great training session.', NOW() - INTERVAL '2 days 12 hours'),
(3, 'Flying a kite this weekend!', NOW() - INTERVAL '2 days'),
(5, 'Space exploration is key to humanity future.', NOW() - INTERVAL '1 day 18 hours'),
(6, 'Shake it off!', NOW() - INTERVAL '1 day 12 hours'),
(1, 'Who is up for a call later today?', NOW() - INTERVAL '12 hours'),
(4, 'Preparing for the next match!', NOW() - INTERVAL '4 hours');

SELECT setval(pg_get_serial_sequence('posts', 'id'), coalesce(max(id), 1)) FROM posts;
