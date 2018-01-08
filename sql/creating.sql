create table Users(login char(20) not null primary key, name_str char(50) not null, last_name char(20) not null, email char(20), pass char(64) not null, salt char(64));
create table Students(login char(20) not null unique, uni_start date not null, uni_year_num int unsigned not null, foreign key(login) references Users(login));
create table Lecturers(login char(20) not null unique, degree char(10), foreign key(login) references Users(login));
create table Groups(ID int unsigned not null auto_increment primary key, lecturer char(20) not null, group_name char(30) not null, num_students int not null, capacity int not null, week_patt enum('EVERY', 'TN', 'TP') not null, week_day int not null, foreign key(lecturer) references Lecturers(login));
create table GroupStudents(group_id int unsigned not null, student char(20) not null, active boolean, foreign key(group_id) references Groups(ID), foreign key(student) references Students(login), primary key(group_id, student));
create table Results(ID int unsigned auto_increment primary key, student char(20) not null, group_id int unsigned not null, res_type enum('egxam', 'mid_term', 'problem_set', 'activity'), when_given date, res_value double, foreign key(student) references Students(login), foreign key(group_id) references Groups(ID));

drop table results;
drop table groupstudents;
drop table groups;
drop table lecturers;
drop table students;
drop table users;

drop trigger if exists user_password;

delimiter $$
create trigger user_password before insert on Users
for each row begin
	declare _salt char(64);
    declare rand_char char(1);
    declare rand_count int;
    declare rand_upper int;
    
    set _salt = '';

	set rand_count = 0;
    while rand_count < 64 do
		set rand_upper = floor(rand() * 100);
        
        if rand_upper > 50 then
			set rand_char = (select lower(conv(floor(rand() * 36), 10, 36)));
		else
			set rand_char = (select upper(conv(floor(rand() * 36), 10, 36)));
		end if;
        
        set _salt = concat(_salt, rand_char);
        set rand_count = rand_count + 1;
    end while;
    
    call hash_pass(new.pass, _salt);
    set new.salt = _salt;
    end; $$
delimiter ;

drop trigger if exists groups_in_check;

delimiter $$
create trigger groups_in_check before insert on Groups
for each row begin
	if (new.num_students > new.capacity) or (new.week_day < 1) or (new.week_day > 7) then
		signal sqlstate '45000'
        set message_text = 'Invalid data given';
	end if;
    end; $$
delimiter ;

drop trigger if exists groups_up_check;

delimiter $$
create trigger groups_up_check before update on Groups
for each row begin
	if (new.num_students > new.capacity) or (new.week_day < 1) or (new.week_day > 7) then
		signal sqlstate '45000'
        set message_text = 'Invalid data given';
	end if;
    end; $$
delimiter ;

drop trigger if exists groupstudents_in_check;

delimiter $$
create trigger groupstudents_in_check before insert on GroupStudents
for each row begin
	declare ns int;
    declare cap int;
    
    select num_students into ns from Groups where Groups.ID = new.group_id;
    select capacity into cap from Groups where Groups.ID = new.group_id;
    
	if (ns + 1 > cap) then
		signal sqlstate '45000'
        set message_text = 'Invalid data given';
	end if;
    end; $$
delimiter ;

drop trigger if exists groupstudents_up_check;

delimiter $$
create trigger groupstudents_up_check before update on GroupStudents
for each row begin
	declare ns int;
    declare cap int;
    
    select num_students into ns from Groups where Groups.ID = new.group_id;
    select capacity into cap from Groups where Groups.ID = new.group_id;
    
	if (ns + 1 > cap) then
		signal sqlstate '45000'
        set message_text = 'Invalid data given';
	end if;
    end; $$
delimiter ;

drop trigger if exists groupstudents_in_after;

delimiter $$
create trigger groupstudents_in_after after insert on GroupStudents
for each row begin
    if (new.active = true) then
		update Groups set num_students = num_students + 1 where ID = new.group_id;
	end if;
    end; $$
delimiter ;

drop trigger if exists groupstudents_up_after;

delimiter $$
create trigger groupstudents_up_after after update on GroupStudents
for each row begin
	if (old.active = false and new.active = true) then
		update Groups set num_students = num_students + 1 where ID = new.group_id;
	end if;
    
    if (old.active = true and new.active = false) then
		update Groups set num_students = num_students - 1 where ID = new.group_id;
	end if;
    end; $$
delimiter ;

drop trigger if exists groupstudents_del_after;

delimiter $$
create trigger groupstudents_del_after after delete on GroupStudents
for each row begin
	update Groups set num_students = num_students - 1 where ID = old.group_id;
    end; $$
delimiter ;

drop trigger if exists results_in_check;

delimiter $$
create trigger results_in_check before insert on Results
for each row begin
	declare ac boolean;
    
    if (new.student in (select student from GroupStudents gs where new.student = gs.student and new.group_id = gs.group_id)) then
		select active into ac from GroupStudents where new.group_id = GroupStudents.group_id and new.student = GroupStudents.student;
		
		if (ac = false) then
			signal sqlstate '45000'
			set message_text = 'Invalid data given';
		end if;
	else
		signal sqlstate '45000'
		set message_text = 'Invalid data given';
	end if;
    end; $$
delimiter ;
