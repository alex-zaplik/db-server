drop procedure if exists student_list;

delimiter $$
create procedure student_list(in get_id int, in get_lecturer char(20))
begin
	if (get_lecturer in (select lecturer from Groups where Groups.ID = get_id)) then
		select us.login, us.name_str, us.last_name from Students st join GroupStudents gs on st.login = gs.student and gs.group_id = get_id join Users us on st.login = us.login where gs.active = true;
	else
		select "Access denied";
	end if;
end $$
delimiter ;

drop procedure if exists result_list;

delimiter $$
create procedure result_list(in get_id int, in get_lecturer char(20))
begin
	if (get_lecturer in (select lecturer from Groups where Groups.ID = get_id)) then
		select res.ID, us.login, us.name_str, us.last_name, res.when_given, res.res_value, res.res_type from Students st join GroupStudents gs on st.login = gs.student and gs.group_id = get_id join Users us on st.login = us.login join Results res on res.student = st.login and res.group_id = get_id where gs.active = true;
	else
		select "Access denied";
	end if;
end $$
delimiter ;

delimiter ;

drop procedure if exists activity_update;

delimiter $$
create procedure activity_update(in student_id char(20), gr_id int unsigned, in _given date, in result_value double, adding boolean)
begin
	declare new_res_val double;
    declare res_key int;
    
    set new_res_val = result_value;
    
    if (exists (select ID from Results where student = student_id and res_type = 'activity' and group_id = gr_id)) then
		set new_res_val = new_res_val + (select res_value from Results where student = student_id and res_type = 'activity' and group_id = gr_id);
        set res_key = (select ID from Results where student = student_id and res_type = 'activity' and group_id = gr_id);
        delete from Results where ID = res_key;
	end if;
    
	insert into Results values(res_key, student_id, gr_id, 'activity', _given, new_res_val);
end $$
delimiter ;

call activity_update('100002', 3, '1900-01-01', 5, true);

drop procedure if exists result_list_param;

delimiter $$
create procedure result_list_param(in get_id int, in get_lecturer char(20), _login varchar(20), _last_name varchar(20))
begin
	if (get_lecturer in (select lecturer from Groups where Groups.ID = get_id)) then
		if length(_login) > 0 then
			select res.ID, us.login, us.name_str, us.last_name, res.when_given, res.res_value, res.res_type from Students st join GroupStudents gs on st.login = gs.student and gs.group_id = get_id join Users us on st.login = us.login join Results res on res.student = st.login and res.group_id = get_id where gs.active = true and us.login = _login;
		else
			select res.ID, us.login, us.name_str, us.last_name, res.when_given, res.res_value, res.res_type from Students st join GroupStudents gs on st.login = gs.student and gs.group_id = get_id join Users us on st.login = us.login join Results res on res.student = st.login and res.group_id = get_id where gs.active = true and us.last_name = _last_name;
		end if;
    else
		select "Access denied";
	end if;
end $$
delimiter ;

drop procedure if exists student_result_list;

delimiter $$
create procedure student_result_list(in get_id int, in get_student char(20))
begin
	select res.when_given, res.res_value, res.res_type from Results res where res.group_id = get_id and res.student = get_student;
end $$
delimiter ;

drop procedure if exists group_list;

delimiter $$
create procedure group_list(in get_user char(20))
begin
	if (get_user in (select login from Lecturers lec where lec.login = get_user)) then
		select gr.ID, gr.group_name, gr.start_time, gr.week_patt, gr.week_day from Groups gr where gr.lecturer = get_user;
	else
		if (get_user in (select login from Students st where st.login = get_user)) then
			select gr.ID, gr.group_name, us.last_name, le.degree, gr.start_time, gr.week_patt, gr.week_day from Groups gr join GroupStudents gs on gr.ID = gs.group_id join Users us on gr.lecturer = us.login join Lecturers le on gr.lecturer = le.login where gs.student = get_user;
		else
			select "Access denied";
		end if;
	end if;
end $$
delimiter ;

drop procedure if exists hash_pass;

delimiter $$
create procedure hash_pass(inout _pass char(64), in _salt char(64))
begin
	declare _pass_sha char(64);
    
    set _pass_sha = sha2(_pass, 256);
    set _pass = sha2(concat(_pass_sha, _salt), 256);
end $$
delimiter ;
