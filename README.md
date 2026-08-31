# EST time converter

A tiny personal Spring Boot app that builds rows for a tutoring platform's
lesson upload sheet: pick a learner and tutor from a saved roster, set an
EST time, and get back a row with everything filled in — including the
platform's `lesson_starttime` / `lesson_endtime` format (EST + a fixed
offset, default 4 hours).

What's on the site:
- **Sessions page** — pick learner + tutor from dropdowns, set subject,
  date, EST start time, and duration. Each one adds a row to a running
  "upload sheet" you can download as an `.xlsx` matching the platform's
  columns (`learner_email`, `learner_name`, `tutor_email`, `tutor_name`,
  `lesson_starttime`, `lesson_endtime`, `teaching_subject_identifier`).
- **Roster page** — add learners/tutors one at a time, or import them in
  bulk from the file the platform exports for all its users (.csv, .xlsx,
  or .xls all work — it just needs a "name" column and an "email" column
  somewhere in the header row, exact header text doesn't matter).
- Two standalone time converters (single + bulk) also live on the Sessions
  page for quick one-off conversions that don't need the roster.

Your roster and sessions are stored in a database — an embedded H2 database
locally (a file under `data/`, zero setup needed), or a real PostgreSQL
database when deployed to a host like Koyeb. See "Deploying to Koyeb" below.

## Run it locally

Requires Java 21 and Maven.

```bash
mvn spring-boot:run
```

Then open http://localhost:8080

## Build a runnable jar

```bash
mvn clean package
java -jar target/est-time-converter-1.0.0.jar
```

## About the offset

The default offset is 4 hours, matching "EST is 4 hours ahead" as stated by
the platform. If the platform actually follows real US Eastern clock time
rather than fixed EST, the correct offset is:

- **4 hours** during Daylight Saving Time (roughly March–November)
- **5 hours** during Standard Time (roughly November–March)

The offset is an input field on the page, so you can adjust it per lesson
or per batch without touching the code.

## Logging in

This app now requires a login. Credentials come from environment variables —
**the easiest and safest way to set them is via a local `.env` file, which
is git-ignored and never gets committed:**

```bash
cp .env.example .env
```

Edit `.env` and set a real, unique password (not something reused
elsewhere — this app may be reachable from the internet whenever ngrok is
running). Generate one if you want:

```bash
openssl rand -base64 24
```

Then start the app with:

```bash
./run.sh
```

This loads `.env` and runs the app for you. If you'd rather set the
variables manually instead of using `.env`:

```bash
export APP_USERNAME=yourname
export APP_PASSWORD="a real password"
mvn spring-boot:run
```

If neither is set, it falls back to `admin` / `changeme` — fine for a
five-second local test, never for anything reachable over the network.

**A few habits worth keeping:**
- Never hardcode credentials into `application.properties` or any source
  file — only reference the env vars, as it already does.
- Avoid typing the password directly on the command line (e.g.
  `APP_PASSWORD=x java -jar ...`); it can end up in your shell history.
  `.env` + `run.sh`, or an interactive `export`, avoid that.
- `.env` is listed in `.gitignore` — double check `git status` never shows
  it as a file to be committed.
- If you ever suspect the password leaked, just change it in `.env` (or
  your exported env var) and restart the app — nothing else to rotate.

## Making it reachable from anywhere (occasional use, free)

Since this is for occasional access rather than an always-on server, the
simplest free option is **ngrok**: it opens a temporary public URL that
tunnels straight to the app running on your own computer. Nothing runs (and
nothing costs anything) when you're not using it.

1. **Sign up free** at [ngrok.com](https://ngrok.com) and install the ngrok
   agent (`brew install ngrok` on Mac, or download from their site).
2. **Authenticate once**:
   ```bash
   ngrok config add-authtoken <your-authtoken-from-the-ngrok-dashboard>
   ```
3. **Claim your free static domain** — in the ngrok dashboard under
   *Domains*, click "New Domain". Free accounts get one, and unlike the
   plain `ngrok http` command, this URL doesn't change every time you
   restart it — so you can bookmark it. It looks like
   `your-name.ngrok-free.app`.
4. **Each time you want to use the app:**
   ```bash
   # terminal 1 — start the app
   export APP_USERNAME=yourname
   export APP_PASSWORD="a real password"
   java -jar target/est-time-converter-1.0.0.jar

   # terminal 2 — open the tunnel
   ngrok http --domain=your-name.ngrok-free.app 8080
   ```
5. Open `https://your-name.ngrok-free.app` from your phone, another
   computer, anywhere — you'll hit the app's own login page first (the one
   added above), then the ngrok tunnel underneath. Two layers, both free.
6. When you're done, `Ctrl+C` both terminals. Nothing is exposed or running
   until you start them again.

The free ngrok tier has modest bandwidth/request limits (fine for one
person's occasional use) — see [ngrok's pricing page](https://ngrok.com/pricing)
if you ever want to check current limits.

## Deploying to Koyeb (free, always reachable — even with your laptop off)

Unlike ngrok, this makes the app run on Koyeb's servers instead of your own
computer, so it's reachable 24/7 without your laptop being on. The trade-off:
Koyeb's free tier puts the app to sleep after an hour with no visitors —
still reachable, just with a few seconds' delay to wake up on the next
request.

**1. Push this project to GitHub** (see the section above if you haven't).

**2. Create a free Koyeb account** at [koyeb.com](https://www.koyeb.com).

**3. Create a database first:**
- In the Koyeb dashboard, create a new **Database Service** (PostgreSQL).
- Once created, copy its connection string — it looks like
  `postgres://user:password@host:port/koyebdb`. You'll need this in step 5.

**4. Create the web service:**
- Click **Create Web Service**, choose **GitHub** as the source, and pick
  this repository.
- Koyeb should detect the `Dockerfile` in this project and build from it
  automatically.

**5. Set environment variables** on the web service (under its settings):
   ```
   APP_USERNAME=yourname
   APP_PASSWORD=a real password
   DATABASE_URL=postgres://user:password@host:port/koyebdb
   ```
(paste in the real connection string you copied in step 3)

**6. Deploy.** Koyeb builds the Docker image and gives you a URL like
`your-app-name.koyeb.app` — that's your permanent, free, always-reachable
address. Open it and log in with the credentials you set above.

**One thing to remember:** Koyeb's free database doesn't get deleted, but it
does "sleep" when unused and wakes up on the next connection — normal, no
action needed. There's no 30-day expiration like some other free tiers have.

## Publishing this to your own GitHub (personal use)

This project is already a git repository with an initial commit. To push it
to your own GitHub account:

```bash
# from inside the est-time-converter folder
git remote add origin https://github.com/<your-username>/<your-repo-name>.git
git branch -M main
git push -u origin main
```

Create the empty repository on GitHub first (no README/license, so it stays
empty), then run the commands above with your own username and repo name.

## Project structure

```
src/main/java/com/personal/esttimeconverter/
  EstTimeConverterApplication.java   entry point; also parses Koyeb's DATABASE_URL
  ConvertController.java             web routes (/, /convert, /convert-bulk)
  ConvertForm.java                   form fields for the standalone time converters
  TimeConverter.java                 core conversion logic
  BulkConverter.java                 parses and converts the bulk textarea
  BulkRow.java                       one row of bulk conversion output

  roster/
    Person.java, PersonRole.java     a learner or tutor entry (JPA entity)
    PersonRepository.java            database queries for people
    RosterService.java               roster logic, CSV/Excel import
    RosterController.java            web routes (/roster, /roster/add, /roster/import)

  session/
    SessionRow.java                  one row destined for the upload sheet (JPA entity)
    SessionRepository.java           database queries for pending sessions
    SessionForm.java                 form fields for the "new session" form
    SessionService.java              pending rows built up before download
    SessionExcelExporter.java        writes the pending rows to an .xlsx
    SessionController.java           web routes (/session/add, /sessions/download, /sessions/clear)

  security/
    SecurityConfig.java              login requirement, single-user auth
    LoginController.java             web route (/login)

src/main/resources/
  templates/index.html               sessions page
  templates/roster.html              roster page
  templates/login.html               login page
  static/styles.css                  shared styling

Dockerfile                           builds the app for Koyeb deployment
src/test/java/.../TimeConverterTest.java  unit tests for the conversion logic
```
