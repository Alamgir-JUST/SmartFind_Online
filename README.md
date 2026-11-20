# SmartFind Online — Java OOP + JDBC + MySQL (4-page browser application)

A small, complete, real-life DBMS lab application. Students can open the code **and** run the app entirely in **GitHub Codespaces**, without installing Eclipse, XAMPP, Java, or MySQL on their own computer.

## Features (exactly four main screens)

| Screen | Route | DBMS outcome |
|---|---|---|
| Report Item | `/report` | `CREATE` (insert student and lost/found report) |
| Explore Items | `/items` | `READ` (`SELECT`, `JOIN`, `WHERE`, `LIKE`) |
| Smart Matching | `/matches` | `READ` plus Java rule-based matching |
| Manage Items | `/manage` | `UPDATE` and `DELETE` |

Edit is a subroute of Manage (`/manage/edit?id=...`), not a separate menu page.

## A. Run online in a browser (recommended for students)

**Prerequisite: a free GitHub account and browser access. GitHub Codespaces has monthly usage limits; check your account's usage.**

1. Download/extract the project ZIP to your computer (no installation needed).
2. Create a new GitHub repository named `smartfind-online` (private recommended).
3. Select **Add file > Upload files** and upload the **contents of `SmartFind_Online/`**, including hidden `.devcontainer` directory. GitHub's web uploader may not reliably preserve folder trees from a drag-and-drop of individual files; the easiest reliable method is to use the **Import repository via GitHub Desktop or `git`** if available, OR follow `docs/UPLOAD_WITH_BROWSER.md` to upload the archive via GitHub's web editor workflow. **Important: Codespaces requires `.devcontainer/devcontainer.json` and `.devcontainer/docker-compose.yml` in the repository.**
4. Open your repository page, click **Code > Codespaces > Create codespace on main**.
5. Wait for Codespaces to finish starting. Its `.devcontainer` config creates two containers: Java workspace and MySQL database (automatically imports `schema.sql` and `seed.sql` on the first launch).
6. In the Codespaces terminal, run:

   ```bash
   mvn -q compile exec:java
   ```

7. Open the forwarded port **8080** using the **Ports** tab (or the pop-up “Open in Browser”). The four navigation pages are in the left sidebar. If port forwarding doesn't appear automatically, add port 8080 in the Ports tab.
8. Press **Ctrl+C** in the terminal when finished, then **stop your Codespace** to conserve quota.

**Do not set port 8080 to Public**. This teaching demo has no accounts, login, session access control, or CSRF defense; anyone able to reach its address can edit/delete sample records. Keep it **Private**, use fictional contact information, and do not deploy it as a public production service.

### Alternative: Browser-only creation of repository files
If browser uploads don't preserve `.devcontainer`, use GitHub's web UI: **Add file > Create new file**, type `.devcontainer/devcontainer.json` in the filename field, paste the file contents, and commit; repeat for `.devcontainer/docker-compose.yml`. Upload the remaining folders normally. See `docs/UPLOAD_WITH_BROWSER.md` for an explicit checklist. The easiest bulk upload is to push the unzipped folder with `git` from any device where `git` is already available; installation-free browser upload is possible but more manual.

## B. How the online system works

```text
Student browser
    |  http(s) forwarded :8080
Java JDK HttpServer (Main -> SmartFindServer)
    |  JDBC prepared statements
MySQL Connector/J (Maven dependency)
    |  Docker internal host: db:3306
MySQL 8.4 container (database: smartfind)
```

**Java is still the back end**. No PHP, Python, cloud API, JavaScript framework, or trained AI is used. The UI is server-rendered HTML/CSS. The single external application library is **MySQL Connector/J**, automatically downloaded by Maven.

### OOP classes

- `model/Item.java` — immutable Java record (data model / encapsulation).
- `db/Database.java` — JDBC connection helper.
- `dao/ItemDAO.java` — Data Access Object: SQL joins, transactions, prepared statements, CRUD.
- `service/MatchService.java` — independent matching logic; deterministic scoring.
- `web/SmartFindServer.java` — URL routing, forms, HTML response, and event handling.
- `Main.java` — starts the application server.

### Database

`students(student_id PK, full_name, email UNIQUE)`

`items(item_id PK, student_id FK, item_type, item_name, category, color, location, description, incident_date, status, created_at)`

Relationship: `students (1) ---> (N) items`. Student profiles are matched by email when items are submitted. Demo entries are provided as a seed file.

### Matching algorithm

Only **OPEN reports of opposite types** (LOST vs FOUND) are eligible. For every candidate:

- Same category: 30
- Same color: 20
- Same location: 20
- At least one shared alphanumeric word of length >= 3 in item name or description: 20
- Dates within 7 days: 10, or within 30 days: 5

Maximum is **100 points**. Scores sort descending. This is a **rule-based similarity heuristic, not a trained ML model nor a probability**. Use a score to suggest follow-up, not to validate ownership.

## C. Classroom practice (four 120-minute labs)

1. **Database + connection**: inspect schema, PK/FK, JOINs; start Codespace and Java server; identify `DB_URL`, driver, connection.
2. **CREATE + READ**: submit a LOST report, search on Explore Items; trace HTTP form -> Java method -> `ItemDAO` -> MySQL.
3. **UPDATE + DELETE**: edit an item, mark RETURNED, delete a dummy item; explain SQL parameter binding and rollback.
4. **Matching + demonstration**: add a FOUND record, select LOST source, inspect scoring and ranking; extend rules, demonstrate testing.

## D. Verifying the project

Run the built-in matching smoke test in Codespaces:

```bash
mvn -q test-compile
java -cp target/test-classes:target/classes edu.smartfind.service.MatchSmokeTest
```

Expected: `PASS: 4 matching smoke checks`.

To check the web app after starting:

```bash
curl -I http://localhost:8080/report
```

Note: the app handles `GET` pages; `curl -I` sends `HEAD` and may be handled differently. Use `curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/report` for reliable GET verification.

## E. Optional offline usage

If you have JDK 21, Maven, and MySQL installed elsewhere, create/import `database/schema.sql` + `database/seed.sql`; set:

```text
DB_URL=jdbc:mysql://localhost:3306/smartfind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=smartfind
DB_PASSWORD=<your database password>
PORT=8080
```

Run `mvn compile exec:java` and open `http://localhost:8080/report`.

## F. Troubleshooting

- **Database unavailable**: wait for the MySQL service to become healthy, then rerun Maven; verify the `.devcontainer` file structure.
- **Maven fails to get dependencies**: check Codespaces internet access and retry. Maven needs to download the MySQL connector the first time.
- **Port 8080 not shown**: open **Ports** tab, select **Forward a Port**, enter 8080. Keep visibility Private.
- **Missing demo records**: SQL seed scripts run only on the **first** initialization of the MySQL data volume. Use SQL manually to repopulate or recreate the volume (destructive).
- **Wrong repository root**: `pom.xml` must be beside `.devcontainer/`, `src/`, and `database/`, not inside an extra nested folder.
- **Source file upload problems**: use the GitHub browser folder checklist `docs/UPLOAD_WITH_BROWSER.md`.

## G. Ethics + intended limitations

The demo has **no authentication**, **no authorization**, **no CSRF protection**, and simplified matching; it is meant for **private classroom practice only**. The displayed student email is fictional. Keep forwarded app ports private. Add proper security before any public or real campus deployment.

Read `docs/STUDENT_GUIDE.md` and `docs/INSTRUCTOR_GUIDE.md` for handouts and assessment.
