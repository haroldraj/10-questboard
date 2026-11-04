# Questboard

The Guild of the Rusty Keyboard is one of the finest adventurers' guilds in the realm. Unfortunately, its quest
administration consists of a pile of parchment that a goblin keeps eating. Adventurers take on quests nobody remembers,
rewards get paid twice, and level 1 rookies keep volunteering to slay dragons (they don't come back).

For the sake of order in the guild and less bloody piles of level 1 rookie skeletons, the Guildmaster has finally approved a budget for a digital **Questboard**. A shiny front-end will be built later by a
very enthusiastic wizard's apprentice. It's your job to build the **back-end**: a REST API that manages adventurers,
quests and the assignments in between.

This is a **small project** meant to let you practice everything you've seen so far. You have **two days**. Don't
over-engineer it: a clean, working, tested application beats a half-finished big one. May your builds be green, and
your NullPointerExceptions few! FOR THE HORDE!

## Timing

Two days. We've split the stories into **Must-Haves** and **Nice-To-Haves**.
Finish the Must-Haves first, and only then start on the Nice-To-Haves.

**Must-Have stories:**
- Story 0 (project setup + technical requirements)
- Story 1: Recruit an adventurer
- Story 2: Post a quest
- Story 3: View the quest board
- Story 4: Accept a quest
- Story 5: Complete a quest

**Nice-To-Have stories:**
- Story 6: Filter the quest board
- Story 7: Guild leaderboard
- Story 8: Adventurer logbook
- Story 9: Abandon a quest / retire an adventurer
- Story 10: Security

## Technical requirements

- Create a new GitHub repository.
    - Think about the files Git should ignore **before** your first commit.
- Provide a REST(ful) Web API (JSON as message format).
- Use Spring Boot (latest version) and Maven.
- **Use Spring Data JPA** (repositories extending `JpaRepository`) for all database access.
    - So: no `EntityManager` or `JdbcTemplate` in your repositories. Derived queries (`findByXxx`) or `@Query` are fine.
- Use PostgreSQL as the database.
    - Run the integration tests against an in-memory database (e.g. H2) or Testcontainers.
    - Use Flyway migrations so the schema is under version control.
- Use DTOs for request and response payloads. Never expose your entities through the API.
- Validate input and answer with sensible HTTP status codes (`201`, `400`, `404`, `409`, ...) and readable error messages.
    - Handle exceptions centrally (e.g. `@RestControllerAdvice`).
- Perform logging. Certainly log all "errors" (illegal arguments, exceptions, ...).
- Provide documentation of your API through OpenAPI / Swagger UI.
- Test your code:
    - **Unit tests** for your business logic (plain JUnit + Mockito/AssertJ where useful).
    - **Integration tests** for your API (e.g. `@SpringBootTest` + `RestAssured` or `MockMvc`) and for your
      repositories (e.g. `@DataJpaTest`).
    - Work test-first where you can.

### Architecture

Keep it simple: a single Maven module with a **layered** package structure is enough.

```
be.switchfully.questboard
 ├── api        (controllers, DTOs, exception handling)
 ├── service    (business logic, mappers)
 └── domain     (entities, repositories)
```

Think about where each responsibility belongs. For example: where do you check that an adventurer is high enough
level for a quest?

## Domain

You'll need (at least) these three concepts:

| Concept        | Data                                                                                          |
|----------------|-----------------------------------------------------------------------------------------------|
| **Adventurer** | id, name (unique), class (`WARRIOR`, `MAGE`, `ROGUE`, `PRIEST`), level, experience points (XP), recruitment date |
| **Quest**      | id, title, description, minimum level, difficulty (`EASY`, `MEDIUM`, `HARD`, `LEGENDARY`), XP reward, gold reward, status (`OPEN`, `ACCEPTED`, `COMPLETED`) |
| **Assignment** | id, quest, adventurer, accepted date, completed date (nullable)                               |

Relationships: an adventurer can take on many quests over time, a quest is (for now) taken by one adventurer.
Think about whether you really need the reverse (`@OneToMany`) sides, and about lazy vs. eager loading.

Leveling rule (keep it simple, you can tweak it): an adventurer needs `level × 100` XP to reach the next level.
Any leftover XP carries over. A big reward can level you up more than once.

## Functional stories

### Story 0: Project setup

As a developer I want a working, runnable project so we can start building features.
- A Spring Boot application that starts and is connected to a PostgreSQL database.
- Swagger UI is reachable.
- Your test configuration runs against an in-memory database (or Testcontainers).
- Your repository is on GitHub, with a proper `.gitignore` and a short README on how to run the app.

### Story 1: Recruit an adventurer

As the Guildmaster I want to recruit a new adventurer so they can start taking on quests.
- When recruiting, the following data should be provided:
    - Name
    - Class (`WARRIOR`, `MAGE`, `ROGUE` or `PRIEST`)
- A new adventurer starts at **level 1 with 0 XP**. The recruitment date is today.
- The name is mandatory and must be unique. Recruiting a second "Gandalf" results in an error
  (`409` / `400`, your pick, but be consistent and explain it in the Swagger docs).

### Story 2: Post a quest

As the Guildmaster I want to post a quest on the board so adventurers can accept it.
- When posting a quest, the following data should be provided:
    - Title
    - Description
    - Minimum level (at least 1)
    - Difficulty
    - XP reward (greater than 0)
    - Gold reward (0 or more)
- The status of a new quest is `OPEN`.

### Story 3: View the quest board

As an adventurer I want to see the quest board so I can pick something worthy of my time.
- Show all quests that are `OPEN`, with title, difficulty, minimum level and rewards.
- Also provide the possibility to look up a single quest by its id (`404` if it doesn't exist).

### Story 4: Accept a quest

As an adventurer I want to accept a quest so the guild knows I'm on it.
- Provide the id of the quest and the id of the adventurer.
- The accepted date is today and the quest status becomes `ACCEPTED`.
- Rules:
    - The quest and the adventurer must exist.
    - The quest must be `OPEN`. Otherwise: error (someone beat you to it).
    - The adventurer's level must be at least the quest's minimum level. Otherwise: error.
    - An adventurer can have **at most 3 accepted (unfinished) quests** at the same time. Otherwise: error.

### Story 5: Complete a quest

As the Guildmaster I want to register that a quest was completed so the reward gets paid out.
- Set the completed date of the assignment to today and the quest status to `COMPLETED`.
- The adventurer earns the XP reward. If they have enough XP they **level up** (see leveling rule).
- The response shows the XP gained, the gold paid out, the new level and whether the adventurer leveled up.
- Completing a quest that doesn't exist, isn't `ACCEPTED`, or was already completed results in an error.
    - The reward must never be paid twice!

### Story 6: Filter the quest board *(Nice-To-Have)*

As an adventurer I want to filter the quest board, so I only see quests that suit me.
- Optional filters: difficulty, maximum minimum level (e.g. "quests I can take at level 5"), and (part of) the title,
  case-insensitive.
- No filters returns all open quests.
- Tip: Spring Data derived queries like `findByDifficultyAndMinimumLevelLessThanEqual...` are your friend.

### Story 7: Guild leaderboard *(Nice-To-Have)*

As the Guildmaster I want a leaderboard, so I know who my best adventurers are.
- Show adventurers ordered by level (highest first), then by XP, then by name.
- Optionally filter by class (e.g. "top mages").
- Show per adventurer: name, class, level, XP and number of completed quests.

### Story 8: Adventurer logbook *(Nice-To-Have)*

As an adventurer I want to see my logbook so I can brag about my accomplishments.
- Show the current quests by default, and optionally also the completed ones (e.g. `?includeCompleted=true`).
- Per quest show the title, difficulty, accepted date and completed date (if any).

### Story 9: Abandon a quest / retire an adventurer *(Nice-To-Have)*

As the Guildmaster I want to keep my data in order.
- An adventurer can abandon an accepted quest: the quest goes back to `OPEN` and no reward is paid.
    - Think about what happens to the assignment (delete it? mark it as abandoned?). Document your choice.
- Retire an adventurer. A retired adventurer can't accept quests anymore, but their history stays.
    - An adventurer with unfinished quests cannot be retired.

### Story 10: Security *(Nice-To-Have)*

As the Guildmaster I want to make sure not just any peasant can change the data.
- Anyone can view the quest board.
- Only the Guildmaster can recruit adventurers and post quests.
- Only the Guildmaster can complete quests (no paying yourself!).
- Basic authentication with in-memory users is fine.

## Hints & remarks

- Start with Story 0 and get **something** running end-to-end very early. A walking skeleton beats big bangs.
- Agree on code style and git workflow upfront if you work in a pair (branches, small commits, meaningful messages).
- Think about which logic belongs in the domain (entities) and which in the services. Does `Adventurer` know how to
  level up? Probably yes!
- Beware of `LazyInitializationException` and N+1 queries. Look at the SQL Hibernate generates (`spring.jpa.show-sql`).
- Don't forget edge cases: unknown ids, empty strings, negative numbers, `null`, a quest worth enough XP for two
  level-ups...
- It's OK to skip nice-to-haves. It's **not** OK to skip tests.

## Definition of done (for each story)

- It works as described, and is documented in Swagger.
- Happy and unhappy paths are covered by tests.
- Errors are logged and answered with a proper status code + message.
- Code is pushed to your repository and the build is green.
