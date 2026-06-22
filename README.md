# Groovy Console

A modern, web-based [Groovy](https://groovy-lang.org/) script console built on **Spring Boot 3** and **Java 21**. Write Groovy in the browser, run it on the server, and get clean, structured output — standard output, return value, standard error, and exceptions are each shown separately.

## Features

- **Rich editor** — [CodeMirror](https://codemirror.net/) with Groovy syntax highlighting, bracket matching, and auto-close. Assets are vendored locally, so the console works fully offline.
- **Structured output** — each run reports four channels independently:
  - **Output** — anything written with `println` / `print`
  - **Return value** — the value (and type) of the last expression
  - **Std error** — anything written with `err.println`
  - **Exception** — a sanitized stack trace with framework/internal frames stripped away
- **At-a-glance status** — success/failure badge, execution time, and inferred return type.
- **Quality-of-life** — built-in sample scripts, `⌘/Ctrl + Enter` to run, dark/light theme, graceful fallback to a plain editor if the highlighter ever fails to load.

## Running locally

```bash
./mvnw spring-boot:run
# or, if you have Maven installed:
mvn spring-boot:run
```

Then open <http://localhost:8080/> (which redirects to `/groovy`).

## Architecture

```
com.basic.groovy
├── GroovySpringApplication      # Spring Boot entry point
├── model
│   ├── ScriptRequest            # { "script": "..." } request body
│   └── ScriptResult             # structured execution result (record)
├── service
│   └── GroovyExecutionService   # compiles + evaluates scripts, captures all channels
└── web
    ├── GroovyController         # serves the page + POST /groovy/api/execute
    └── WebConfig                # redirects "/" to the console
```

The UI is a single Thymeleaf page (`templates/groovy-console.html`) that talks to the
JSON API via `fetch`. CodeMirror assets live under `static/vendor/codemirror`.

### Execution model

Each request gets its own `GroovyShell` and `Binding`, so concurrent runs never share
state. Script output is captured by binding `out` and `err` writers (which Groovy's
`println` / `print` helpers write to). Exceptions are sanitized with
`StackTraceUtils.deepSanitize` and then trimmed of host (Spring/Tomcat/service) frames so
only script-relevant lines remain.

## HTTP API

`POST /groovy/api/execute`

```jsonc
// request
{ "script": "println 'hi'\n1 + 2" }

// response
{
  "success": true,
  "stdout": "hi\n",
  "stderr": "",
  "returnValue": "3",
  "returnType": "java.lang.Integer",
  "error": null,
  "executionTimeMs": 12
}
```

## ⚠️ Security warning

This console executes **arbitrary code on the server**. Never expose it publicly without
robust authentication and authorization (for example, Spring Security restricted to admin
users) and appropriate sandboxing. It is intended for local development and trusted,
internal use only.

## Tech stack

- Spring Boot 3.4 (Web, Thymeleaf)
- Apache Groovy 5.0
- Java 21
- CodeMirror 5 (vendored)
