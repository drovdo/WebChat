use chat;
select * from users where id in
(select user_id from messages group by user_id having count(user_id) > 3);