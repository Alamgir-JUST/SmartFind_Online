# Instructor guide — 4 lessons of 120 minutes

## Session 1: Database to web application

- 0–10 min: scenario prompt: “How can a student find a lost calculator?”
- 10–30 min: demonstrate all four pages; identify entity vs relationship vs UI.
- 30–60 min: import/open Codespaces; explain Java server and MySQL service.
- 60–90 min: inspect schema, FK, and 6 seed entries; run SQL joins.
- 90–120 min: identify Java `Item` model and `Database` helper; checkpoint.

## Session 2: Create + Read

- 0–15 min: students predict SQL executed when pressing Submit.
- 15–40 min: trace form attributes to Java parameter map.
- 40–70 min: explain prepared statements and transaction in `ItemDAO.insert`.
- 70–105 min: students enter two fictional reports and search results.
- 105–120 min: pair explanation and instructor feedback.

## Session 3: Update + Delete

- 0–15 min: ask which queries need a WHERE clause and why.
- 15–45 min: read `ItemDAO.update`, `delete`, and Java handlers.
- 45–90 min: student edit/delete walkthrough; verify returned records.
- 90–120 min: discuss validation, user input escaping, and why public deployment requires authentication + CSRF defenses.

## Session 4: Smart Matching

- 0–20 min: predict score for matching/nonmatching report pairs.
- 20–55 min: inspect `MatchService.score` and run smoke test.
- 55–90 min: modify a candidate attribute and observe score differences.
- 90–120 min: student pair demonstrations with rubric and Q&A.

## Architecture & educational choices

JDK `HttpServer` (no Spring) makes request handling transparent. `ItemDAO` contains SQL, `MatchService` contains algorithm, and `SmartFindServer` handles HTML forms. MySQL Connector/J is the sole Maven dependency. Browser display works without Swing or XAMPP. MySQL starts as a Docker container using `.devcontainer/docker-compose.yml` when Codespaces starts.

## Suggested short questions

1. Why is `PreparedStatement` safer than concatenated SQL?
2. What is the difference between PK and FK in SmartFind?
3. Why is `students` stored separately from `items`?
4. What is a transaction and why is it used for insertion?
5. Explain what `LIKE` and `JOIN` do.
6. What changes when an item is marked RETURNED?
7. Is 80/100 a probability that an item belongs to a person?
8. Why should the forwarded web port remain private?

## Troubleshooting

* If MySQL has not initialized, wait for db service to turn healthy.
* Sample data is imported once when the MySQL volume is first created.
* If students accidentally upload an extra outer folder, GitHub Codespaces will not see root `.devcontainer`.
* The Maven database dependency is retrieved online on first build.
* Students should stop idle Codespaces to conserve quota.
