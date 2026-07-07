# Papilio

Papilio is a database schema versioning tool for MongoDB, similar in spirit to [Liquibase](https://www.liquibase.org/). Database changes are declared in XML changelog files and applied sequentially. Each changeset is tracked in the database so that already-applied changes are skipped on subsequent runs.

## Features

- **XML-based changelogs** — Define schema and data changes in a familiar, version-controlled format.
- **Sequential changeset execution** — Changesets run in file order; each changeset must have a unique `id` and an `author`.
- **Modular changelogs** — Split changes across multiple files and include them from a root changelog.
- **Change tracking** — Executed changesets are recorded in the `DATABASE_CHANGE_LOG` collection with an MD5 checksum.
- **Checksum validation** — Detects modifications to previously executed changesets; whitespace-only changes do not affect the checksum.
- **Database locking** — Uses a `DATABASE_CHANGE_LOG_LOCK` collection to prevent concurrent runs.
- **MongoDB operations** — Create collections and indexes, insert, update, delete documents.
- **MongoDB shell scripts** — Run inline JavaScript or external `.js` files via the MongoJS engine.
- **Raw MongoDB commands** — Execute JSON commands directly, with optional FreeMarker or JEL templating.
- **Find-and-update** — Run a find query and apply a per-document update template (FreeMarker).
- **External resource loading** — Load column values from text, JSON, or XML files (paths are relative to the changelog file that references them).
- **Subquery column values** — Populate insert/update columns from the result of a MongoDB command using JXPath.
- **Replica set support** — Connect via `--replicas` or a single `--host` / `--port`.
- **Authentication and SSL** — Optional username/password and `--enable-ssl`.

## Building

```bash
mvn package
```

This produces a distributable zip under `dist/`. The distribution includes `papilio.sh` / `papilio.bat`, libraries under `lib/`, and logging configuration.

## Running

### Using the distribution scripts

```bash
# Linux / macOS
./papilio.sh --host localhost --port 27017 --database mydb --changelog /path/to/master.xml

# Windows
papilio.bat --host localhost --port 27017 --database mydb --changelog C:\path\to\master.xml
```

### Using Java directly

```bash
java com.yukthitech.papilio.Main \
  --host localhost \
  --port 27017 \
  --database mydb \
  --changelog /path/to/master.xml \
  --dbtype mongo
```

### Command-line options

| Option | Short | Description |
|--------|-------|-------------|
| `--changelog` | `-cl` | **(Required)** Path to the root changelog XML file |
| `--dbtype` | `-dt` | Database type. Default: `mongo` |
| `--host` | `-h` | MongoDB host |
| `--port` | `-p` | MongoDB port |
| `--database` | `-db` | Database name |
| `--replicas` | `-r` | Comma-separated list of `host:port` entries (alternative to `--host` / `--port`) |
| `--username` | `-un` | Authentication username |
| `--password` | `-pwd` | Authentication password |
| `--enable-ssl` | `-ssl` | Enable SSL for the connection |

Either `--replicas` or both `--host` and `--port` must be specified.

## Changelog structure

Execution starts from the root changelog file. That file typically includes other version files:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<databaseChangeLog>
    <include path="basic-working.xml"/>
    <include path="ext-files/ext-file-working.xml"/>
    <include path="script-exec.xml"/>
</databaseChangeLog>
```

Included file paths are resolved **relative to the file that contains the `<include>`**.

Each changeset groups one or more changes and is identified by `id` and `author`:

```xml
<changeSet author="akiran" id="Creation of test table">
    <!-- one or more change elements -->
</changeSet>
```

## Supported change types

### Create collection (`<create-table>`)

Creates a MongoDB collection. Supports `ignoreIfExists="true"` and custom collection options.

```xml
<create-table tableName="TEST_COL"/>
```

### Create index (`<create-index>`)

```xml
<create-index indexName="TEST_COL_NAME" tableName="TEST_COL" unique="true">
    <index-column name="name"/>
</create-index>
```

Index columns support `ascending`, `descending`, and `textIndex` attributes.

### Insert (`<insert>`)

```xml
<insert tableName="TEST_COL">
    <column-value name="name" value="Kranthi"/>
    <column-value name="lastName" value="Kiran"/>
    <column-value name="city" value="Hyderabad"/>
</insert>
```

**Column value sources** (attributes on `<column-value>`):

| Attribute | Description |
|-----------|-------------|
| `value` | Literal string value |
| `intValue` | Integer value |
| `booleanValue` | Boolean value |
| `dateValue` | Date value (`yyyy-MM-dd`) |
| `jsonValue` | Inline JSON parsed as an object |
| `jelValue` | JSON template processed with JEL |
| `valueFromFile` | Text content loaded from a file |
| `jsonFromFile` | JSON object loaded from a file |
| `xmlFromFile` | XML converted to a map and used as the value |
| `valueQuery` / `valueQueryPath` | MongoDB command whose result is extracted via JXPath |
| `valueQuery` / `valueQueryJpath` | MongoDB command whose result is extracted via JSONPath |

Only one of `valueQueryPath` and `valueQueryJpath` may be specified on the same `<column-value>`.

Load all fields from a JSON file:

```xml
<column-value-json>document.json</column-value-json>
```

**Subquery example (JXPath)** — fetch a value from another collection:

```xml
<column-value name="testColId" valueQueryPath="/cursor/firstBatch//_id/*[1]">
    <valueQuery>
        {
            "find": "TEST_COL",
            "filter": {"name": "Kranthi"},
            "projection": {"_id": 1}
        }
    </valueQuery>
</column-value>
```

**Subquery example (JSONPath)** — same query using JSONPath instead of JXPath:

```xml
<column-value name="testColId" valueQueryJpath="$.cursor.firstBatch[0]._id.$oid">
    <valueQuery>
        {
            "find": "TEST_COL",
            "filter": {"name": "Kranthi"},
            "projection": {"_id": 1}
        }
    </valueQuery>
</column-value>
```

**External file example**:

```xml
<column-value name="content" valueFromFile="file1.txt"/>
```

### Update (`<update>`)

```xml
<update tableName="TEST_COL">
    <column-value name="lastName" value="PipsyNew"/>
    <condition name="name" value="Pipsy"/>
</update>
```

Also supports `<column-value-json>` and `<column-value-xml>` to load update fields from files.

### Delete (`<delete>`)

```xml
<delete tableName="TEST_COL">
    <condition name="name" value="Pipsy"/>
</delete>
```

### MongoDB shell script (`<script>`)

Runs JavaScript in the MongoDB shell context (`db` is available):

```xml
<script>
    db.TEST_COL.update(
        {},
        {"$set": {"intVal": 10}},
        {"multi": true}
    )
</script>
```

### External script file (`<exec-script-file>`)

Loads and executes a `.js` file relative to the changelog:

```xml
<exec-script-file>script-ext.js</exec-script-file>
```

### Raw MongoDB command (`<query>`)

Executes a JSON MongoDB command via `runCommand`:

```xml
<query>
    {
        "createIndexes": "TEST_COL",
        "indexes": [{"key": {"status": 1}, "name": "status_idx"}]
    }
</query>
```

Templated variants:

- `<query-template>` — processed with FreeMarker (`change` is available in the template context)
- `<query-jel-template>` — processed with JEL

### Find and update (`<findAndUpdate>`)

Runs a find query, then for each returned document applies an update query built from a FreeMarker template. Document fields are available as template variables (e.g. `${name}`, `${name?lower_case}`):

```xml
<findAndUpdate>
    <find-query>
        {
            "find": "TEST_COL"
        }
    </find-query>

    <update-query-template>
        {
            "update": "TEST_COL",
            "updates": [
                {
                    "q": {"name": "${name}"},
                    "u": {"$set": {"lowerName": "${name?lower_case}"}}
                }
            ]
        }
    </update-query-template>
</findAndUpdate>
```

## Change tracking and checksums

On first run, Papilio creates two collections:

- **`DATABASE_CHANGE_LOG`** — stores `CHANGE_SET_ID`, `AUTHOR`, `CHECKSUM`, and `FILE_NAME` for each executed changeset
- **`DATABASE_CHANGE_LOG_LOCK`** — used to obtain an exclusive lock during execution

When a changeset is run again:

- If the checksum matches, the changeset is **skipped**.
- If the checksum differs, execution **fails** with an error indicating the old and new checksums.

Checksums are MD5 hashes of the changeset JSON with whitespace normalized, so formatting-only edits do not trigger a mismatch.

To accept a deliberate changeset modification without re-running it, set the system property:

```bash
java -Dpapilio.updateChecksum=true com.yukthitech.papilio.Main ...
```

This updates the stored checksum for mismatched changesets instead of failing.

If a previous run ended abruptly, the lock collection may retain a stale entry. Remove the lock document manually from `DATABASE_CHANGE_LOG_LOCK` before retrying.

## Examples and tests

The test suite (`src/test/java/com/yukthitech/papilio/mongo/TMongoDb.java`) exercises the main features. Example changelogs live under `src/test/resources/mongo/`:

| File | What it demonstrates |
|------|----------------------|
| `master.xml` | Root changelog with `<include>` |
| `basic-working.xml` | Create collection/index, insert, update, delete, subquery column values |
| `ext-files/ext-file-working.xml` | Loading column values from external text files |
| `script-exec.xml` | Inline `<script>` and `<exec-script-file>` |
| `find-n-update.xml` | `<findAndUpdate>` with FreeMarker templates |
| `jsonpath-subquery.xml` | Subquery column values via JSONPath (`valueQueryJpath`) |
| `checksum/` | Checksum validation and `papilio.updateChecksum` |

To run tests, configure MongoDB connection details in `src/test/resources/mongodb.properties` and run:

```bash
mvn test
```
