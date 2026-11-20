# Upload using only a browser

GitHub Codespaces reads configuration files from the repository. If you cannot use a terminal or desktop Git client, you can create everything using the GitHub website.

1. Create repository `smartfind-online` (Private is preferable), initialize with README.
2. Extract the downloaded ZIP using your operating system's built-in archive viewer; no extra software installation is required.
3. Use GitHub's **Add file > Upload files** to upload files from `src/`, `database/`, `pom.xml`, `README.md`, and `docs/`, preserving the folder structure. If the web uploader does not preserve relative paths, create nested filenames under **Add file > Create new file** (e.g. `src/main/java/edu/smartfind/Main.java`) and paste the file contents individually. A quick way to confirm: check that folders display correctly in the repository file browser.
4. For the **hidden folder** `.devcontainer`, explicitly use **Add file > Create new file** and enter `.devcontainer/devcontainer.json` as the filename, then paste the file content and commit. Repeat for `.devcontainer/docker-compose.yml`.
5. Confirm the repository root contains these entries:

   ```text
   .devcontainer/
     devcontainer.json
     docker-compose.yml
   src/
     main/java/edu/smartfind/... (all Java files)
     main/resources/style.css
     test/java/edu/smartfind/service/MatchSmokeTest.java
   database/
     schema.sql
     seed.sql
   pom.xml
   README.md
   docs/
   ```
6. Click **Code > Codespaces > Create codespace on main**, then in its terminal run `mvn -q compile exec:java` and open port 8080.

**Note:** GitHub's web interface does not upload a ZIP and automatically extract its contents into repository folders. The step-by-step browser-only path is possible but takes several file creations. If you have access to `git` already, pushing the extracted folder is much faster.
