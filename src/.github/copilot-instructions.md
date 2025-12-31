# Copilot / AI Agent Instructions for Greengrocer Management System

This project is a JavaFX desktop application (modular Java) with a simple JDBC-backed persistence layer. Use these notes to make focused, safe edits.

- Purpose: UI + local MySQL database for managing products, orders and users. App entry: `main.Main` loads `resources/login.fxml` and calls `main.DatabaseAdapter.createTables()`.

- Key architecture:
  - UI: `main.controllers` (controllers extend `BaseController`) and FXML files in `resources/` (e.g. `login.fxml`). Navigation uses `BaseController.changeScene()`.
  - Domain: `main.models` (JavaFX `Property`-based models like `Product` — note `getEffectivePrice()` logic and `setStock()` setter exists).
  - Data access: `main.DatabaseAdapter` centralizes JDBC connection details and schema creation. DAO classes live in `main.dao` (e.g. `OrderDAO`, `ProductDAO`, `UserDAO`) and must use `DatabaseAdapter.getConnection()`.
  - Utilities: `main.utils` contains helpers like `PasswordUtil` (SHA-256 hashing). Database SQL uses `SHA2(...,256)` when inserting/checking passwords — keep these consistent.

- Important patterns and conventions (do not change without reason):
  - Packages are under `main` (e.g. `main.controllers`, `main.models`). Keep package names consistent with file locations.
  - UI models use JavaFX `Property` types. When changing model fields, provide matching property getters (e.g., `priceProperty()`) so bindings in controllers/tables keep working.
  - Navigation should use `BaseController.changeScene(node, fxmlPath, title)` to preserve CSS loading and window behavior.
  - Database credentials are hardcoded in `DatabaseAdapter` constants (`DB_URL`, `USER`, `PASS`) — update cautiously and document changes.

- Database and persistence notes:
  - Schema is created at startup by `DatabaseAdapter.createTables()`; it uses `users`, `products`, `orders`, `order_items` tables.
  - Use prepared statements for queries (codebase already follows this in some places). Avoid string concatenation for SQL to prevent injection.

- Tests & build:
  - A unit test exists: `main/Member3NumberPolicyTest.java`. There is no `pom.xml` or `build.gradle` in the repository root. Prefer opening the project in an IDE (IntelliJ / VS Code Java extensions) for running and debugging tests. If using command-line, compile/run with `javac`/`java` and include module path and JavaFX libs as needed.

- Quick navigation hints (where to edit common behaviors):
  - Login/auth: `main.DatabaseAdapter.validateLogin(...)` and `main.utils.PasswordUtil`.
  - Scene navigation & CSS: `main.controllers.BaseController.changeScene(...)` and `resources/styles.css`.
  - DB schema / seed data: `main.DatabaseAdapter.createTables()`.
  - Product business rule: `main.models.Product.getEffectivePrice()`.

- When proposing code changes:
  - Reference exact files/lines in your suggestion and explain why (e.g., "update `DatabaseAdapter` DB_URL` because CI uses a different DB").
  - Preserve JavaFX `Property` API and FXML ids — UI bindings are sensitive to renames.
  - Run the app after DB or UI changes; many issues only surface at runtime (missing FXML, CSS, or SQL errors).

If anything here is unclear or you'd like me to add run/build commands for your environment, tell me what OS/tooling you prefer and I will update this file.
