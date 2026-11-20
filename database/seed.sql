USE smartfind;
INSERT INTO students(full_name,email) VALUES
('Ayesha Rahman','ayesha@example.edu'),('Rafi Ahmed','rafi@example.edu'),('Nila Sultana','nila@example.edu'),('Tanvir Hasan','tanvir@example.edu');
INSERT INTO items(student_id,item_type,item_name,category,color,location,description,incident_date) VALUES
(1,'LOST','Casio FX-991 Calculator','Electronics','Black','Library','Casio FX-991 scientific calculator in black case','2026-11-18'),
(2,'FOUND','Scientific Calculator','Electronics','Black','Library','Found a Casio FX-991 scientific calculator','2026-11-19'),
(3,'LOST','Student ID Card','ID Card','Blue','Cafeteria','Blue lanyard, student identification card','2026-11-17'),
(4,'FOUND','Blue Student ID','ID Card','Blue','Cafeteria','Found student ID with blue lanyard near counter','2026-11-18'),
(1,'LOST','USB Flash Drive','Electronics','Silver','Lab 3','Small 32GB silver USB drive','2026-11-16'),
(3,'FOUND','Water Bottle','Accessories','Green','Auditorium','Reusable green water bottle','2026-11-19');
