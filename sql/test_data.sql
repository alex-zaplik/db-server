insert into Users values("admin", "admin", "admin", null, "admin", null);

insert into Users values("ala.ala", "Ala", "Kowalska", null, "123", null);
insert into Users values("bob.bob", "Bob", "Kowalski", null, "abc", null);
insert into Users values("mat.mat", "Mat", "Nowak", null, "aaa", null);

update Users set pass = 'bbb' where login = 'bob.bob';

insert into Lecturers values("ala.ala", "mgr.");
insert into Lecturers values("bob.bob", "dr");
insert into Lecturers values("mat.mat", "dr hab.");

insert into Users values("100000", "100", "200", null, "111", null);
insert into Users values("100001", "101", "201", null, "222", null);
insert into Users values("100002", "102", "202", null, "333", null);
insert into Users values("100003", "103", "203", null, "444", null);
insert into Users values("100004", "104", "204", null, "555", null);
insert into Users values("100005", "105", "205", null, "666", null);
insert into Users values("100006", "106", "206", null, "777", null);
insert into Users values("100007", "107", "207", null, "888", null);
insert into Users values("100008", "108", "208", null, "999", null);
insert into Users values("100010", "222", "333", null, "aaa", null);
insert into Users values("100011", "222", "333", null, "aaa", null);

insert into Students values("100000", "2016-10-01", 2);
insert into Students values("100001", "2015-10-01", 3);
insert into Students values("100002", "2014-10-01", 1);
insert into Students values("100003", "2016-10-01", 2);
insert into Students values("100004", "2015-10-01", 2);
insert into Students values("100005", "2014-10-01", 4);
insert into Students values("100006", "2015-10-01", 1);
insert into Students values("100007", "2014-10-01", 1);
insert into Students values("100008", "2016-10-01", 3);

insert into Groups values(null, "ala.ala", "Algebra", 0, 5, 'EVERY', 4, '11:15');
insert into Groups values(null, "bob.bob", "Algebra", 0, 6, 'TN', 1, '9:15');
insert into Groups values(null, "bob.bob", "Logika", 0, 7, 'EVERY', 5, '13:15');
insert into Groups values(null, "mat.mat", "Algebra", 0, 8, 'TP', 6, '9:15');
insert into Groups values(null, "mat.mat", "Logika", 0, 9, 'EVERY', 2, '11:15');
insert into Groups values(null, "mat.mat", "Analiza", 0, 5, 'TN', 4, '11:15');

insert into GroupStudents values(1, "100000", false);
insert into GroupStudents values(2, "100000", true);
insert into GroupStudents values(1, "100001", true);
insert into GroupStudents values(3, "100001", true);
insert into GroupStudents values(4, "100001", true);
insert into GroupStudents values(2, "100002", true);
insert into GroupStudents values(3, "100002", true);
insert into GroupStudents values(6, "100003", true);
insert into GroupStudents values(2, "100004", true);
insert into GroupStudents values(4, "100005", true);
insert into GroupStudents values(6, "100005", true);
insert into GroupStudents values(1, "100005", false);
insert into GroupStudents values(1, "100006", true);
insert into GroupStudents values(2, "100006", true);
insert into GroupStudents values(3, "100007", true);
insert into GroupStudents values(4, "100007", true);
insert into GroupStudents values(5, "100007", true);
insert into GroupStudents values(6, "100008", false);

insert into GroupStudents values(6, "100000", true);
insert into GroupStudents values(6, "100001", true);
insert into GroupStudents values(6, "100004", true);
insert into GroupStudents values(6, "100006", true);

insert into Results values(null, "100005", 4, 'EXAM', '2019-01-06', 10.0);
insert into Results values(null, "100001", 1, 'EXAM', '2019-01-06', 5.5);
insert into Results values(null, "100001", 1, 'MID_TERM', '2019-01-06', 4.5);
insert into Results values(null, "100001", 3, 'EXAM', '2019-01-06', 4.5);
insert into Results values(null, "100002", 3, 'EXAM', '2019-01-06', 3.5);
insert into Results values(null, "100007", 3, 'EXAM', '2019-01-06', 2.0);
insert into Results values(null, "100001", 3, 'MID_TERM', '2019-02-06', 5.0);
insert into Results values(null, "100001", 3, 'MID_TERM', '2019-01-15', 4.5);
insert into Results values(null, "100001", 3, 'PROBLEM_SET', '2019-01-02', 4.0);
insert into Results values(null, "100001", 3, 'ACTIVITY', '2019-01-06', 26);

call student_list(2, "ala.ala");
call student_list(2, "bob.bob");
call student_list(6, "mat.mat");
call student_list(6, "ala.mat");

call result_list(4, "mat.mat");
call result_list(4, "bob.bob");
call result_list(6, "mat.mat");
call result_list(6, "ala.mat");

call student_result_list(4, "100004");
call student_result_list(4, "100005");
call student_result_list(6, "100001");
call student_result_list(6, "100009");

call group_list("100001");
call group_list("100009");
call group_list("bob.bob");
call group_list("bob.mat");

delete from Results;
delete from GroupStudents;
delete from Groups;
delete from Students;
delete from Lecturers;
delete from Users;
