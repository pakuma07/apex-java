# Chapter 36: Engineering Practice at Scale -- Java

## What This Chapter Covers
Everything so far made *the code* good. This chapter is about the *engineering
system* around it — how a staff/principal engineer ships safely, leads technically
without managing, and keeps a large codebase and team healthy over years. These are
the skills that separate a senior individual contributor from someone who
multiplies a whole org: **design docs, code review, testing strategy, CI/CD,
release engineering, incident response, and technical leadership.** Less syntax,
more judgment — but no less concrete.

> **Why a coding book ends here:** a 20-year career is not 20 years of typing
> faster. The leverage shifts from "I wrote the code" to "I shaped the decision, the
> interface, and the team's ability to move safely." This chapter names that craft.

> **C++ contrast:** these practices are language-independent — the same design-doc,
> review, CI/CD, and incident disciplines apply to a C++ shop. Only the tools differ
> (CMake/Bazel vs Maven/Gradle, gtest vs JUnit); the judgment is identical.

---

## 36.1 Design Docs and RFCs

Before significant work, write it down. A **design doc** (RFC/one-pager/TDD) forces
clarity, surfaces disagreement *cheaply* (before code exists), and becomes the
durable record of *why*. Writing is thinking.

A reusable structure:

```text
1. Context & problem    what are we solving, for whom, why now
2. Goals / non-goals    explicitly scope OUT what you won't do (prevents scope creep)
3. Proposed design      the approach, with the key decisions made visible
4. Alternatives         what else you considered and WHY you rejected it  ◀ most valuable
5. Trade-offs & risks   what this costs, what could go wrong, how you'll know
6. Rollout & migration  how it ships incrementally, how you back out
7. Open questions       what you're unsure about / want review on
```

The staff-level signal is the **Alternatives** and **Non-goals** sections: junior
docs describe *a* solution; senior docs show the solution *space* and a defended
choice. Keep it as short as the decision allows.

> **Reversible vs irreversible decisions** (Bezos's one-way/two-way doors): spend
> design-doc effort proportional to reversibility. A library you can swap later
> deserves a paragraph; a public API, data model, or wire format (Chapters 31, 35)
> deserves the full treatment, because you'll live with it for years.

---

## 36.2 Code Review

Code review catches defects, spreads knowledge, and enforces consistency — but its
*cultural* effect (raising the bar, shared ownership) matters more than any single
bug caught.

As an **author**: keep PRs **small and single-purpose** (a 200-line PR gets a real
review; a 2,000-line PR gets a rubber stamp). Write a description that says *what*
and *why*, self-review first, and separate refactors from behavior changes.

As a **reviewer**:

- **Review for the right things, in order:** correctness → design/interfaces → tests
  → readability → style. Don't bikeshed formatting when a tool can do it (36.3).
- **Distinguish blocking from non-blocking.** Prefix nits (`nit:`) so the author
  knows what must change vs. what's a suggestion.
- **Ask, don't command.** "What happens if this list is empty?" teaches and invites
  reasoning better than "this is wrong."
- **Be timely.** A review sitting for a day blocks a human; fast turnaround keeps
  throughput high.

> **The most valuable reviews question the *approach*, not the lines.** "Should this
> be a new service at all?" caught early saves more than any inline fix — which
> requires reviewing the design doc (36.1) *before* the code exists.

---

## 36.3 Static Analysis and Automated Quality Gates

Humans should review *judgment*, not whitespace. Push everything mechanical into
tooling that runs locally and in CI (Chapters 25, 26):

| Tool | Catches |
|---|---|
| **Spotless / google-java-format** | formatting — auto-applied, never debated |
| **Checkstyle / PMD** | style and common bug patterns |
| **SpotBugs + FindSecBugs** | bytecode-level bugs and security anti-patterns (Chapter 32) |
| **Error Prone / NullAway** | compile-time bug detection, null-safety |
| **OWASP Dependency-Check / Snyk** | known CVEs in dependencies (Chapter 32) |
| **JaCoCo** | test coverage gate |

```xml
<!-- Wire gates into the Maven build so a violation fails CI, not just a comment. -->
<plugin>
  <groupId>com.diffplug.spotless</groupId>
  <artifactId>spotless-maven-plugin</artifactId>
  <!-- mvn spotless:check in CI; spotless:apply locally -->
</plugin>
```

> **Make the right thing automatic.** A standard a human must remember erodes; a
> standard a tool enforces holds. Spend review attention where tools can't go.

---

## 36.4 Testing Strategy at Scale

Chapter 25 covered *how* to write tests (JUnit 5, Mockito); at scale the question is
*which* tests and *how many*. The **test pyramid** is the default allocation:

```text
        /\        few   end-to-end (slow, brittle, high-fidelity)
       /  \             integration (real DB/queue via Testcontainers)
      /----\      many  unit (fast, isolated, deterministic)
```

- **Unit tests** are the broad base: fast, deterministic, no I/O — run on every
  save. If they're slow or flaky, people stop running them.
- **Integration tests** verify the seams (DB, queue, HTTP) where unit mocks lie. Use
  **Testcontainers** to run a real Postgres/Kafka in Docker rather than mocking the
  component most likely to surprise you.
- **End-to-end tests** are few and precious — they catch what nothing else can but
  are slow and flaky; don't build your safety net out of them.

Cross-cutting practices:

- **Flaky tests are worse than no tests** — they train the team to ignore red.
  Quarantine and fix them; track a flakiness rate.
- **Property-based testing** (jqwik) finds edge cases your examples miss —
  invaluable for parsers and serializers (Chapter 31).
- **Contract tests** (Chapter 35.6) guard API boundaries across teams.
- **Test behavior, not implementation** — tests coupled to internals break on every
  refactor and discourage improvement; assert on observable outcomes.

---

## 36.5 Version Control and Branching

Git discipline scales (or breaks) a team's velocity:

- **Trunk-based development** (short-lived branches merged to `main` daily behind
  feature flags, Chapter 30) keeps integration continuous and avoids the merge-hell
  of long-lived branches. Most high-velocity orgs converge here over heavy GitFlow.
- **Atomic, well-described commits**: each commit is one coherent change with a
  message explaining *why* (the diff shows *what*). This makes `git bisect`, reverts,
  and history archaeology actually work.
- **Decouple deploy from release** with feature flags so merging to `main` ≠
  exposing a feature — ship code dark and turn it on independently.

> A clean history is a debugging tool. When prod breaks at 3am, `git bisect` over
> small atomic commits finds the culprit in minutes; a history of "WIP" and
> 2,000-line squashes finds it never.

---

## 36.6 CI/CD and Release Engineering

**Continuous Integration**: every push runs the full quality gate (compile, lint,
type, test, security, build) so `main` is *always* releasable. **Continuous
Delivery**: every green build *can* deploy; **Continuous Deployment**: it does,
automatically.

```text
push ─▶ CI: compile + lint + test + scan + build artifact (fat JAR / image)
        │ green
        ▼
   deploy to staging ─▶ smoke tests ─▶ canary (1% → 10% → 100%, watch SLOs) ─▶ full
        │ any SLO regression
        ▼  automatic rollback
```

The properties that make this safe (Chapters 30, 33):

- **Reproducible builds & pinned deps** (Chapters 26, 32): the artifact you test is
  the artifact you ship. Maven/Gradle lockfiles + a fixed base image.
- **Progressive delivery:** canary or blue-green with automated rollback on SLO
  regression — never a big-bang deploy.
- **Fast pipelines:** a 40-minute CI run kills flow and tempts people to skip it;
  parallelize modules and cache dependencies to keep it minutes. (Mind JVM warmup,
  Chapter 29 — test startup is part of the cost.)
- **Database migrations are part of the release** and must be backward-compatible
  (expand/contract, Chapter 34) so code and schema roll out — and back —
  independently. Flyway/Liquibase version them.

---

## 36.7 Incident Response and Operability

At scale, incidents are inevitable; the craft is in *responding* and *learning*.
(Builds on Chapter 30's observability.)

- **Roles during an incident:** an **Incident Commander** coordinates (not
  necessarily the most senior engineer), others investigate and communicate. Clear
  roles beat a swarm.
- **Mitigate before you diagnose.** Stop the bleeding (roll back, flip a flag, shed
  load) *first*; root-cause *after* users are safe. The latest deploy is the first
  suspect.
- **Blameless postmortems:** document the timeline, impact, root cause, and —
  crucially — the **systemic** fixes (the process/tooling gap that allowed it), not
  "Bob should be more careful." Blame hides causes; safety surfaces them.
- **Track and close action items.** A postmortem whose follow-ups never ship
  guarantees the repeat incident.

> **You build it, you run it.** Teams that operate their own services (on-call for
> their code) build more operable systems — the feedback loop from 3am pages to
> design decisions is the strongest force for reliability there is. Make services
> *operable*: good logs, metrics, runbooks, and dashboards (Chapter 30) are
> features, not afterthoughts.

---

## 36.8 Managing Technical Debt and Large-Scale Change

- **Technical debt is a tool, not a sin** — taken deliberately to ship, tracked, and
  repaid. The danger is *unacknowledged* debt and debt with compounding interest (a
  flaky core, an un-upgradable dependency — the org still on Java 8). Make it visible
  (a register, budgeted paydown) rather than moralizing.
- **The Boy Scout rule:** leave each file a little better than you found it —
  incremental cleanup beats the doomed "big rewrite."
- **Large-scale migrations** (a JDK upgrade, `javax`→`jakarta`, an API cutover across
  hundreds of call sites) are a discipline: automate with **OpenRewrite** recipes,
  run old and new in parallel, migrate incrementally behind flags, and *delete the
  old path* — an unfinished migration leaving two systems is worse than either alone.
- **Rewrites are usually a trap** (Spolsky): you discard hard-won bug fixes encoded
  in the old code and bet the company on a flag day. Prefer **strangler-fig**:
  incrementally route functionality to the new system until the old one is dead.

---

## 36.9 Technical Leadership Without Authority

The staff/principal role is influence, not management:

- **Multiply, don't hoard.** Your output is increasingly *other people's* output: the
  design review that prevents a bad path, the doc that aligns three teams, the
  abstraction that makes everyone faster. Optimize for team throughput, not your
  personal commit count.
- **Choose the right battles.** Have strong opinions, held loosely; **disagree and
  commit** once a decision is made. Reserve hard lines for the irreversible decisions
  (36.1) worth the capital.
- **Make the implicit explicit.** Write the design principles, the on-call runbook,
  the "why we don't do X" doc. Tribal knowledge doesn't scale; written knowledge
  does.
- **Mentor and sponsor.** Code review (36.2) and design review are teaching surfaces;
  the senior move is leveling up the people around you, not being the single point of
  failure who must touch everything.

---

## Summary

- **Write design docs** for non-trivial work — the *alternatives* and *non-goals* are
  the staff-level content; spend effort in proportion to **reversibility**.
- **Code review** for correctness and design first; keep PRs **small**, push style to
  **automated gates** (Spotless/SpotBugs/Error Prone in CI), and question the
  *approach*, not just the lines.
- Allocate tests by the **pyramid** (many fast unit, fewer integration via
  **Testcontainers**, few e2e), kill **flakiness**, and test **behavior, not
  implementation**.
- Use **trunk-based development** with atomic commits and **feature flags**, and a
  **CI/CD** pipeline with reproducible builds and **progressive delivery + auto
  rollback** (migrations via Flyway/Liquibase).
- Treat **incidents** as learning: mitigate first, run **blameless postmortems** with
  systemic fixes, and make services **operable** ("you build it, you run it").
- Manage **tech debt** deliberately, prefer **strangler-fig** and **OpenRewrite** to
  rewrites, and lead by **multiplying others** — written knowledge, good interfaces,
  and mentorship over personal heroics.

**This concludes the engineering-at-scale arc (Chapters 33–36).** From distributed
systems and data to API contracts and the human system that ships them, these are
the concepts a staff/principal engineer reasons about long after the syntax has
become reflex.

## Next Steps

- Write a one-page design doc for your next feature, with explicit non-goals and at
  least two rejected alternatives.
- Add an integration test with Testcontainers (real Postgres) alongside your unit
  tests and compare confidence.
- Wire Spotless + SpotBugs into the Maven/Gradle build so style and bug checks fail
  CI, not review.
- Run a blameless postmortem template on a past incident and extract one systemic
  fix.
- Revisit **[Chapter 25: Testing](../25_testing/README.md)**,
  **[Chapter 26: Build Tools](../26_build_tools/README.md)**, and
  **[Chapter 30: Production & Operational Concerns](../30_production_operational/README.md)**.
