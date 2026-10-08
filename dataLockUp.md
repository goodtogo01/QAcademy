# Viewing qAcademy's Data (H2 Console)

Created students (or courses, or anything else) don't show up anywhere in Eclipse's
UI automatically — the app has no built-in data viewer. Data is there, you just need
to open a way to browse it: the H2 web console.

## Steps (while the app is running)

The H2 console is off by default (it's a full, unauthenticated SQL console, so it
doesn't ship enabled) — start the app with the `dev` profile active to turn it on:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

(Eclipse: Run Configuration → Arguments tab → VM arguments → add
`-Dspring.profiles.active=dev`.) Then:

1. Open a browser to `http://localhost:8085/h2-console`
2. On the login page, fill in exactly:
   - **Driver Class:** `org.h2.Driver`
   - **JDBC URL:** `jdbc:h2:file:./data/qacademy` (must match `application.properties` exactly)
   - **User Name:** `sa`
   - **Password:** *(leave blank)*
3. Click **Connect**
4. In the left tree: **PUBLIC → Tables → STUDENTS** → double-click it to
   auto-generate `SELECT * FROM STUDENTS`, or type it yourself and hit Run.

You'll see all rows there, including the seeded Ada Lovelace row plus anything
you've created via the API.

## Troubleshooting: "Database not found"

The relative path `./data/qacademy` is resolved against whatever folder the
process was launched from. If you ran it via an Eclipse Run Configuration, check
that config's **Working Directory** setting — if it isn't the project root, the
`data/` folder ends up somewhere else.

Easiest fix: search for the actual file on disk from a terminal:

```bash
find . -name "qacademy.mv.db"
```

Then adjust the JDBC URL in the H2 console login screen (and/or
`application.properties`) to point at wherever it actually landed.

## Alternative: viewing it inside Eclipse itself (not a browser tab)

Eclipse's built-in **Data Source Explorer** can also connect to the database
directly:

**Window → Show View → Other → Data Management → Data Source Explorer** → new
**Generic JDBC** connection profile, pointing at the H2 driver jar in your local
Maven repo (`~/.m2/repository/com/h2database/h2/<version>/h2-<version>.jar`).

**Caveat:** H2 file databases only allow one process to hold the file open at a
time by default. If the app is already running and holding the file open, Data
Source Explorer's separate connection will conflict with it. To allow both
connections simultaneously, add `;AUTO_SERVER=TRUE` to the JDBC URL in
`application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/qacademy;AUTO_SERVER=TRUE
```

Otherwise, the H2 web console above is simpler — it runs inside the same process
as the app, so it never hits this file-lock issue at all.
