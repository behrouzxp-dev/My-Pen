# Releases

This document describes how stable My Pen releases are produced and published.
It implements the rules in AGENTS.md (section on GitHub Releases and tags).

## Artifacts

Every release publishes, on a GitHub Release for tag `vX.Y.Z`:

- `My Pen Vx.y.z.apk` — the signed release APK (capital **V**, one space
  after `My Pen`, e.g. `My Pen V1.0.2.apk`)
- `My Pen Vx.y.z.apk.sha256` — SHA-256 checksum of the APK
- Release notes generated from commits, alongside the Persian annotated
  tag description

Releases are published directly (`draft: false`) by `release.yml`,
and only from an approved tag.

## Prerequisites

1. All changes for the release are merged to `main` (release PR already
   approved and merged — documentation included: `CHANGELOG.md`,
   `RELEASES.md`, `README.md`, `.github/README.md`, and affected `docs/`).
2. CI is green on `main` (enforced for pull requests; see `.github/workflows/ci.yml`).
3. Signing secrets are configured in the repository:
   `Keystore_base64`, `Keystore_password`, `Key_alias`,
   `Key_password` (see SECURITY.md).
4. The owner has sent the word **`tag`** and selected the version.

## Tagging (owner-initiated)

When the owner sends `tag`, derive the options from the latest release tag
(or, if none exist yet, from the released versionName `1.0.0`):

| Option | Resulting version (example from v1.0.0) |
| ------ | ---------------------------------------- |
| patch  | v1.0.1 |
| minor  | v1.1.0 |
| major  | v2.0.0 |

Once the owner selects a version:

1. Update all release documentation for that version and merge the release PR
   (with owner approval) into `main`.
2. Create the **annotated** tag with a **Persian** description, e.g.:

   ```bash
   git tag -a v1.0.1 -m "رفع باگ‌های نسخه ۱٫۰٫۱: ..."
   git push origin v1.0.1
   ```

3. `.github/workflows/release.yml` builds, signs, checksums and publishes
   the GitHub Release automatically.

## Verifying a release

```bash
# Download the APK and its checksum from the GitHub Release, then:
sha256sum -c "My Pen Vx.y.z.apk.sha256"
```

The APK filename and its `.sha256` companion use the exact format
`My Pen Vx.y.z.apk` / `My Pen Vx.y.z.apk.sha256`.

## Versioning

Semantic Versioning as `MAJOR.MINOR.PATCH`:

- `1.0.0` — first stable release
- `1.1.0` — backward-compatible features
- `1.1.1` — backward-compatible fixes
- `2.0.0` — incompatible changes

`versionName` in `app/build.gradle.kts` must match the tag (without the `v`).
`versionCode` is an integer that must never decrease; it is currently `1000`
and intentionally independent of `versionName`.

## Release history

- **v1.0.0** — first stable release (2026-11-09). applicationId
  `com.mypen.app` (renamed from `com.arena.mypen` before the first stable
  release); versionCode 1000. Published directly by `release.yml` as
  `My Pen V1.0.0.apk` with its SHA-256 checksum.

## Notes

- No Gradle wrapper is committed yet; CI installs Gradle 8.7 explicitly
  (`gradle/actions/setup-gradle`). Adding the wrapper
  (`gradle wrapper --gradle-version 8.7`) is recommended once a suitably
  equipped machine is available.
- Historical backups (v0) restore into the current version; see
  `docs/ARCHITECTURE.md` for the backup format and
  `app/src/test/resources/sample_insulin_backup.json` for a real fixture.
