# Student handout — SmartFind Online

**Course:** Database Management Systems (Theory + Lab)  
**Technologies:** Java 21, OOP, JDBC, MySQL 8.4, HTML/CSS, GitHub Codespaces  
**Duration:** Four labs × 120 minutes  
**Expected outcome:** one functional four-screen browser application backed by a relational database.

## Learning outcomes

After the lab, students can:
1. Explain a client / Java-server / JDBC / database architecture.
2. Identify OOP models, DAOs, services and controllers, and their responsibilities.
3. Execute INSERT, SELECT/JOIN, UPDATE, DELETE via JDBC `PreparedStatement`.
4. Use a PK–FK relationship between `students` and `items`.
5. Implement and explain an auditable ranking formula for possible matches.
6. Test real CRUD operations and document outcomes.

## Before your first lab

Create a GitHub account. Upload project contents in one private repository. Open Code > Codespaces > Create codespace on main. The supplied `.devcontainer` starts **Java + MySQL** in the cloud. The SQL scripts create tables and sample rows automatically. Run `mvn -q compile exec:java` in Codespaces terminal and open forwarded port 8080.

No local Java, Eclipse, XAMPP, database server, or software installations are needed.

## Mini case study

Ayesha loses a black scientific calculator in the campus library. Rafi finds a similar calculator and reports it. After data entry, SmartFind uses the category, color, location, shared words, and date difference to suggest a matching report. The match is a clue, not evidence of ownership.

## Lab 1: Connection and database

- Open `database/schema.sql`. Identify two primary keys, one foreign key, the 1:N relationship, and NOT NULL fields.
- Explain the difference between MySQL server (`db:3306`) and browser port (`8080`).
- Start the application and open the Report page.
- Inspect `Database.connect()` and the environment variables.
- Practice SQL: `SELECT * FROM students;` and `SELECT i.item_name,s.full_name FROM items i JOIN students s ON i.student_id=s.student_id;`

**Checkpoint:** Show the app, explain the PK/FK relationship, and locate the JDBC connection code.

## Lab 2: CREATE and READ

- Submit a new LOST report under a fictional name and email.
- Open Explore Items; search by item name, location, and report type.
- Read `ItemDAO.insert()` and `ItemDAO.all()`. Identify SQL placeholders and parameter values.
- Explain why a new report also needs an owner in the `students` table.

**Checkpoint:** Demonstrate an insertion and successful search in a joined result.

## Lab 3: UPDATE and DELETE

- On Manage Items, edit the previous report and update its color.
- Mark it RETURNED, then confirm matching does not propose it.
- Add a throwaway dummy record and delete it.
- Find `UPDATE items SET... WHERE item_id=?` and `DELETE FROM items WHERE item_id=?`.

**Checkpoint:** Trace the POST form from browser to `ItemDAO.update()` or `ItemDAO.delete()`.

## Lab 4: Matching + presentation

- Select an OPEN LOST item on the Matching page.
- Create a similar FOUND record and evaluate its score.
- Change one attribute at a time (color, location, or date), then record the score difference.
- Explain why a displayed '100/100' is not a probability or proof of ownership.
- Optional challenge: require identical categories or rank by shared keyword overlap count.

**Checkpoint:** Live demo + 2 minute explanation of your score calculation.

## Suggested student lab report (2–3 pages)

1. Title, objective, software architecture (browser → Java → JDBC → MySQL).
2. ER diagram with keys, relationship and constraints.
3. OOP responsibilities table.
4. Evidence: screenshots of Create, Read, Update/Delete, Match score.
5. One sample INSERT, SELECT JOIN, UPDATE, DELETE query.
6. Score example showing all components and a short limitation discussion.

## Assessment out of 20

- 8 marks — functional CRUD operations and correct JDBC access.
- 4 marks — schema, PK/FK, JOIN and OOP explanation.
- 4 marks — matching algorithm and demonstration.
- 2 marks — clear, accessible user interface.
- 2 marks — lab report, testing and presentation.

**Data policy:** Use fictional student details. The app has no login or access control and must remain private to your own Codespace.
