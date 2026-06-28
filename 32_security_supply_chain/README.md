# Chapter 32: Security & Supply Chain -- Java

## What This Chapter Covers
Security is a set of habits across the whole lifecycle, not a feature bolted on at
the end. This chapter covers **application security** (injection, secrets,
password hashing, cryptography, the deserialization trap, the OWASP Top 10 as it
hits Java) and **supply-chain security** (dependency vulnerabilities, scanning,
lock files, SBOMs, signing). The focus is the specific Java footguns and the
concrete tools that defuse them — with **Log4Shell** as the cautionary tale that
made supply chain a board-level topic.

> **Disclaimer:** use these techniques to *defend* your own systems. Do not attack
> systems you don't own.

> **Version note:** Java 21; examples use Spring Security crypto, JJWT/Nimbus for
> JWT, and OWASP Dependency-Check / CycloneDX for supply chain.

> **C++ contrast:** memory-safety bugs (buffer overflows, use-after-free) dominate
> C++ security; the JVM eliminates those by design. Java's risks shift to
> *logic/configuration*: injection, unsafe deserialization, and dependency CVEs.

---

## 32.1 Threat Modeling in One Page

Before writing defenses, ask four questions:

```text
1. What are we protecting?     data, credentials, availability, integrity
2. Who might attack, and why?  external, insider, automated bots
3. Where are the boundaries?   every input: HTTP, files, env, deps, queues
4. What if X is compromised?   blast radius — least privilege limits it
```

Two principles run through everything: **never trust input** (validate/parse at
every boundary, Chapter 31) and **least privilege** (code, tokens, containers, DB
users get the *minimum* access they need, so a compromise is contained).

---

## 32.2 Injection: SQL, Command, and Friends

Injection happens when untrusted data is **interpreted as code/commands**. The fix
is always the same shape: *separate data from instructions* — never concatenate.

```java
// SQL — WRONG: string concatenation lets input become SQL.
// stmt.executeQuery("SELECT * FROM users WHERE name = '" + name + "'");  // NEVER
// RIGHT: PreparedStatement sends data separately from the query (Chapter 24).
try (var ps = conn.prepareStatement("SELECT * FROM users WHERE name = ?")) {
    ps.setString(1, name);                       // safe — bound parameter
    ResultSet rs = ps.executeQuery();
}
```

```java
// Command — WRONG: a shell interprets metacharacters.
// Runtime.getRuntime().exec("convert " + userFile);   // NEVER
// RIGHT: pass an argument array, no shell.
new ProcessBuilder("convert", userFile).start();       // userFile is one literal arg
```

Same rule for JPQL/HQL (use bound parameters, never string-built queries), LDAP,
and XPath. For HTML output, the templating engine must **autoescape** to stop XSS
(Thymeleaf does by default).

---

## 32.3 Secrets Management

Secrets (DB passwords, API keys, signing keys) must **never be hardcoded or
committed**.

```java
// Read from the environment / a secret manager (Chapter 30), not from source.
String apiKey = System.getenv("API_KEY");        // injected by the platform
```

- Keep secrets out of VCS; scan history with **gitleaks**/**detect-secrets** in a
  pre-commit hook and CI.
- Store them in a manager (HashiCorp Vault, AWS/GCP/Azure secret stores, K8s
  secrets); inject at runtime via env or mounted files. Spring Cloud Vault / Config
  integrate this.
- **Rotate** regularly and on suspected exposure; scope tokens narrowly.
- Compare secrets in **constant time** — `MessageDigest.isEqual(a, b)`, not
  `String.equals`, to avoid timing leaks.

---

## 32.4 Hashing Passwords (Not Encrypting)

Passwords are **hashed with a slow, salted KDF**, never encrypted or stored
plaintext. Use a purpose-built algorithm — Argon2id (preferred), bcrypt, or scrypt
— *not* a fast hash like SHA-256.

```java
// Spring Security crypto — encoders embed the salt + parameters in the output.
PasswordEncoder enc = new Argon2PasswordEncoder(16, 32, 1, 1 << 16, 3);  // tuneable
String hash = enc.encode(rawPassword);           // unique salt embedded
boolean ok = enc.matches(rawPassword, hash);     // constant-time verify

// DelegatingPasswordEncoder stores an {id} prefix so you can upgrade algorithms:
PasswordEncoder delegating = PasswordEncoderFactories.createDelegatingPasswordEncoder();
```

> **Tune the cost** to a target verify latency (~0.5 s interactive) on your
> hardware, and store parameters with the hash so you can raise them later
> (re-hash on next successful login). Expensive hashing is also a **DoS vector** —
> rate-limit login endpoints so an attacker can't force costly verifies en masse.

For randomness, use **`SecureRandom`** (not `java.util.Random`) for tokens, salts,
and anything security-sensitive.

---

## 32.5 Cryptography Done Right

Rule zero: **don't roll your own crypto.** Use vetted libraries (the JCA, Tink,
Bouncy Castle) and authenticated encryption.

| Goal | Tool |
|---|---|
| Authenticated symmetric encryption | **AES-GCM** or ChaCha20-Poly1305 (via JCA/Tink) |
| Asymmetric (public/private) | RSA, Ed25519, X25519 |
| Integrity / signatures | HMAC, Ed25519 |
| Password hashing (NOT encryption) | Argon2/bcrypt (32.4) |

- Always use **authenticated** encryption (detects tampering); **never reuse a
  nonce** with GCM. Google **Tink** is a higher-level API that makes these mistakes
  hard, vs raw `Cipher` which makes them easy.
- TLS for data in transit (32.8); store encryption keys as secrets (32.3) in a
  KMS/HSM where possible.

---

## 32.6 The Deserialization Trap (Java's Signature Risk)

Java's most notorious vulnerability class:

```java
// NEVER do this with untrusted bytes — arbitrary code execution via gadget chains.
// Object o = new ObjectInputStream(untrustedStream).readObject();   // ⚠ RCE
```

- **Java native serialization (`ObjectInputStream`) on untrusted data executes
  attacker-controlled code** through gadget chains in libraries on your classpath.
  This single mechanism is behind a huge share of Java CVEs. Use JSON/Protobuf for
  any untrusted data (Chapter 31); if you must, use a serialization filter
  (`ObjectInputFilter`, JEP 290) with a strict allow-list.
- Other landmines: **Jackson default/polymorphic typing** on untrusted input
  (32.7/Chapter 31), unsafe XML (XXE — disable external entities, use a hardened
  parser), and SpEL/expression injection.

---

## 32.7 The Software Supply Chain (and Log4Shell)

Most of your code is *other people's code*, running with your privileges. In
December 2021, **Log4Shell** (CVE-2021-44228) — a single string in a log message
triggering remote code execution via Log4j 2's JNDI lookup — affected millions of
systems and made supply-chain security unavoidable.

```text
   maintainer ──▶ Maven Central ──▶ build (mvn/gradle) ──▶ your artifact ──▶ runtime
        ▲              ▲                  ▲                     ▲
   account        typosquat /        transitive            a CVE in a deep
   takeover       malicious dep      pull-in               transitive dep (Log4Shell)
```

Threats: account takeover of a popular library, typosquatting, **dependency
confusion** (an internal artifact name resolvable on a public repo), malicious
build plugins, and known CVEs in transitive dependencies (the long tail).

---

## 32.8 Dependency Hygiene and Scanning

| Tool | Finds |
|---|---|
| **OWASP Dependency-Check** / **Snyk** / **Grype** | known CVEs in your dependencies |
| **Dependabot / Renovate** | automated dependency-update PRs |
| **SpotBugs + FindSecBugs** / **Semgrep** | insecure patterns in *your* code |
| **gitleaks / detect-secrets** | committed secrets |
| **Maven Enforcer / `dependencyManagement`** | pin & converge transitive versions |

Practices:

- **Pin and converge versions** (Maven `dependencyManagement` / a Gradle BOM /
  platform) so builds are reproducible and a transitive bump can't surprise you;
  verify artifact integrity (checksums/signatures from Maven Central).
- **Mitigate dependency confusion:** use a single curated internal repository
  (Artifactory/Nexus) that proxies public artifacts, rather than mixing public and
  private repos by version.
- **Wire scanning into CI** (Chapter 25) and pre-commit so every change is checked;
  fail the build on reachable criticals.

```bash
mvn org.owasp:dependency-check-maven:check     # scan deps for known CVEs
```

---

## 32.9 SBOMs, Signing, and Provenance

For higher assurance, prove *what* you shipped and *that it's authentic*:

- **SBOM (Software Bill of Materials):** a machine-readable inventory of every
  component (CycloneDX/SPDX). Generate with the **CycloneDX** Maven/Gradle plugin;
  consumers scan it for vulnerabilities (this is how orgs found Log4j fast).
- **Artifact signing:** sign JARs/containers with **Sigstore/cosign** (keyless,
  OIDC-based, recorded in the Rekor transparency log) so consumers can verify
  authenticity without long-lived keys.
- **Build provenance (SLSA):** attest *how/where* an artifact was built in a
  hardened, isolated pipeline, so tampering is detectable.

```bash
mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom   # produce an SBOM
```

---

## 32.10 CVE Triage at Scale

A scanner reporting 200 "criticals" trains teams to ignore it. Not every CVE is
exploitable in *your* context:

- **Reachability:** is the vulnerable method actually called on a path handling
  untrusted input? A CVE in an unused code path is low priority.
- **VEX** (Vulnerability Exploitability eXchange): record "not affected — not
  reachable" so consumers and auditors agree.
- **Prioritize** by CVSS *and* exploitability (known exploit? in CISA KEV?) *and*
  reachability — then SLA the *actually-reachable* criticals and batch the rest.

---

## 32.11 OWASP Top 10 — Java Cheat Sheet

Mapped to the **OWASP Top 10 (2021)** — a cheat sheet, not a substitute for the
full list:

| Risk | Java defense |
|---|---|
| **Injection** | `PreparedStatement`, bound JPQL params, `ProcessBuilder`, autoescaping |
| **Broken access control** | enforce authz **server-side** on every request (Spring Security) |
| **Cryptographic failures** | JCA/Tink, AES-GCM, TLS, Argon2/bcrypt for passwords |
| **Insecure design** | threat-model early; secure defaults; defense in depth |
| **Security misconfiguration** | no stack traces to users, hardened parsers, disable XXE |
| **Vulnerable components** | Dependency-Check/Snyk, pinned versions, Dependabot, SBOM |
| **Software/data integrity failures** | no untrusted native deserialization, verify signatures/SLSA |
| **Identification/auth failures** | strong sessions/JWT, MFA, rate-limited login |
| **SSRF** | allow-list outbound URLs; block cloud metadata endpoints |
| **Logging failures** | log security events; never log secrets/PII (Chapter 30) |

### JWT pitfalls (auth done wrong)

- Reject **`alg=none`**; pin the algorithm — never trust the token header.
- Beware **RS256/HS256 confusion** (verifying with a public key as an HMAC secret).
- Always validate **`exp`, `nbf`, `aud`, `iss`** — a valid signature only proves
  *who issued it*. Use a vetted library (Nimbus, JJWT) with an explicit algorithm.

---

## 32.12 Transport Hardening (TLS)

- **Always verify certificates** — never disable hostname/cert verification in prod
  (a common "fix" that opens MITM). Fix the trust store instead.
- Require **TLS 1.2+ (prefer 1.3)** and modern cipher suites.
- For service-to-service, **mTLS** authenticates both sides — the basis of zero-trust
  service meshes.

---

## Summary

- **Never trust input** and apply **least privilege** — these prevent most
  vulnerabilities.
- Defeat **injection** by separating data from code (`PreparedStatement`, bound
  params, `ProcessBuilder`, autoescaping).
- Keep **secrets** out of code/VCS; hash passwords with **Argon2/bcrypt** (tuned,
  rate-limited); use vetted crypto (**JCA/Tink**, AES-GCM) and **`SecureRandom`**.
- Treat **native deserialization of untrusted data as RCE** — Java's signature
  risk; use JSON/Protobuf and serialization filters.
- Secure the **supply chain** (the Log4Shell lesson): scan dependencies
  (Dependency-Check/Snyk), pin/converge versions, generate **SBOMs**, sign
  artifacts (**Sigstore**), and **triage CVEs by reachability**.

## Next Steps

- Run OWASP Dependency-Check on a real project and triage the findings by
  reachability.
- Replace any `new ObjectInputStream(...)` on external data with JSON
  deserialization (Chapter 31).
- Add Spring Security with an Argon2 encoder and a rate-limited login.
- Generate a CycloneDX SBOM and store it as a build artifact.
- Revisit **[Chapter 31: Data Validation & Serialization](../31_data_validation_serialization/README.md)**
  and **[Chapter 30: Production & Operational Concerns](../30_production_operational/README.md)**.
