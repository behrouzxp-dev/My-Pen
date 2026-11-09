Permanent Project Development Instructions

This file defines the permanent development, security, documentation, GitHub, testing, release, and workspace management rules for this project.

All agents working on this repository must read and follow these instructions before performing any task.

Core Principles
Treat this file as the permanent project-wide instruction source.
Read and understand the existing project structure before making changes.
Preserve existing functionality unless a change is explicitly requested.
Avoid unnecessary complexity, dependencies, and architectural changes.
Never make assumptions about important requirements.
Ask the project owner when an important decision is unclear.
Prefer reliable, maintainable, testable, and well-documented solutions.
Never claim that an operation succeeded without verification.
Real-Time Progress Reporting
All user-visible progress reports must be written in Persian.

Requirements:

Use right-to-left (RTL) formatting.
Provide concise reports at meaningful development milestones.
Report completed operations, current activities, discovered problems, and decisions requiring approval.
Clearly distinguish completed work from ongoing work.
Never fabricate progress, test results, build results, or deployment status.
Do not expose passwords, private keys, tokens, or other sensitive information in progress reports.
The final report must also be written in Persian and formatted RTL.

Documentation Language Policy
All source-controlled technical documentation must be written in English, except for the explicit Persian items mandated by the project owner below.

English documentation includes:

README.md (primary project README at the repository root)
AGENTS.md
ARCHITECTURE.md
REQUIREMENTS.md
CHANGELOG.md
RELEASES.md
API.md
Technical reports
Code comments
Commit messages
Branch names
GitHub Issues
Pull Requests
Mandatory Persian exceptions (per section 19.1):

.github/README.md: Full Persian (RTL) counterpart of the root English README.md, placed in .github/README.md so GitHub displays the Persian README on the repository homepage while the project itself retains the root English README.md.
Git tag descriptions (git tag -a): Annotated release tag messages must be written in Persian (and may appear in GitHub Release notes alongside generated commit notes).
Application interface language is independent of documentation language and must follow the project requirements.

All user-facing progress and final reports must remain in Persian.

Project Structure and Documentation
Maintain a clean, logical, and maintainable repository structure.

Recommended documentation structure:

project/
├── .github/
│   └── README.md        # Persian (RTL) README rendered on GitHub's repository page
├── AGENTS.md
├── README.md            # Primary English project README
├── CHANGELOG.md
├── RELEASES.md
├── docs/
│   ├── REQUIREMENTS.md
│   ├── ARCHITECTURE.md
│   ├── PROJECT_PLAN.md
│   └── API.md
├── app/
└── ...
Adapt the structure to the selected framework and project requirements.

Rules:

Maintain both README.md (English, at repository root) and .github/README.md (Persian RTL, for GitHub display), and keep them strictly synchronized whenever features, architecture, build commands, or workflows change.
Do not create unnecessary documentation files.
Keep documentation synchronized with actual implementation.
Update relevant documentation whenever architecture, requirements, or behavior changes.
Never document unimplemented features as completed.
Do not duplicate the same information across multiple documents without a clear reason.
GitHub as the Primary Source of Truth
GitHub is the primary and authoritative platform for:

Source code
Project documentation
Version history
Branches and commits
Issues and Pull Requests
Tags
Releases
Build artifacts
Do not use Google Drive or other cloud storage services for project source code, documentation, development files, or release management.

The repository must remain the authoritative source of project history.

Rules:

Keep the repository organized.
Never upload unnecessary build caches or temporary files.
Never commit credentials or signing keys.
Keep release artifacts separate from the source-code history whenever possible.
Verify repository changes after pushing.
Git and Branch Management
Use the following branch strategy:

main: stable and approved code.
feature/*: new functionality.
fix/*: bug fixes.
refactor/*: internal code improvements.
docs/*: documentation-only changes.
Rules:

Keep commits small and logically separated.
Use meaningful English commit messages.
Follow Conventional Commits where practical.
Examples:

feat: add user profile screen
fix: resolve application startup crash
refactor: simplify database repository
docs: update installation instructions
Do not commit unfinished or untested work to main.
Do not rewrite shared Git history without explicit approval.
Do not force-push to protected or shared branches.
Do not delete important branches without approval.
For small projects, temporary feature branches are sufficient. Do not introduce unnecessary branch complexity.

Pull Request and Merge Policy
Use Pull Requests for significant changes.

Every Pull Request should contain:

A clear English title.
A description of the changes.
Relevant Issue references when applicable.
Testing information.
Potential risks or breaking changes.
Rules:

Review changes before merging.
Resolve important conflicts carefully.
Never silently discard existing functionality.
Obtain project owner approval before merging significant architectural or behavior-changing changes into main.
Do not merge unverified changes.
Issue and Task Management
Use GitHub Issues to track substantial tasks and bugs when appropriate.

Issue descriptions should include:

Problem or objective
Expected behavior
Actual behavior when reporting a bug
Steps to reproduce when applicable
Acceptance criteria
Avoid creating unnecessary Issues for trivial internal operations.

Keep task status accurate.

Version Management
Use Semantic Versioning:

MAJOR.MINOR.PATCH
Examples:

1.0.0: first stable release.
1.1.0: backward-compatible feature addition.
1.1.1: backward-compatible bug fix.
2.0.0: incompatible changes.
Rules:

Keep version information consistent across relevant project files.
Maintain CHANGELOG.md and RELEASES.md.
Update all release-related documentation (CHANGELOG.md, RELEASES.md, README.md, .github/README.md, and affected files in docs/), include those updates in the release Pull Request, and merge them into main before creating or pushing the release tag (per section 19.1).
Use annotated Git tags (git tag -a vX.Y.Z -m "...") for releases, and write tag descriptions in Persian (per section 19.1).
Never reuse an existing release tag for a different version.
Never delete historical releases without explicit approval.
Build and Automated Testing
Before considering a code, build, or workflow task complete:

Run relevant automated tests.
Check for compilation errors.
Run static analysis or lint checks when available.
Verify important application flows.
Check for regressions.
Confirm the final build configuration.
Documentation-only changes (*.md, docs/**, LICENSE, templates, etc.) do not require running CI (ci.yml), per section 19.1.

For Android projects:

Verify Debug builds.
Verify Release builds when release configuration is available.
Check application ID, version information, and APK filename format (My Pen Vx.y.z.apk).
Verify signing configuration without exposing secrets.
Test installation and startup where possible.
Never report a successful build unless the build actually completed successfully.

If a test cannot be performed, clearly explain why.

GitHub Actions and Release Automation
Use GitHub Actions when automation provides meaningful value.

Possible workflows include:

Automated tests.
Static analysis.
Debug builds.
Release builds.
Artifact uploads.
Release publication.
Rules:

Use GitHub Secrets for required credentials.
Never hardcode sensitive values in workflow files.
Apply least-privilege permissions.
Avoid unnecessary workflow executions (documentation-only changes skip ci.yml entirely via paths-ignore, per section 19.1).
Keep workflow files understandable and maintainable.
Verify workflow results before reporting success.
Do not create or push a release tag without project owner approval (tag workflow in section 19.1), and ensure all release documentation updates have already been submitted in the release Pull Request and merged into main before tagging. Once the owner selects and approves the release tag, release.yml publishes the GitHub Release directly (draft: false) without creating a draft.

GitHub Releases and Build Artifacts
Use GitHub Releases for approved stable application releases.

For Android projects, release artifacts may include:

APK named [AppName] Vx.y.z.apk (My Pen Vx.y.z.apk)
AAB (when applicable)
Relevant checksums (My Pen Vx.y.z.apk.sha256)
Release notes (including the Persian annotated tag description)
Rules:

Update documentation (CHANGELOG.md, RELEASES.md, README.md, .github/README.md, and relevant docs/*), submit it with the release Pull Request, and merge it into main prior to creating the release tag (per section 19.1).
Use version tags such as v1.0.0 with Persian tag descriptions.
Name output APK files using the exact format [AppName] Vx.y.z.apk — specifically My Pen Vx.y.z.apk (e.g., My Pen V1.0.2.apk).
Publish releases directly (draft: false) via release.yml once the tag has been approved by the owner.
Include accurate release notes.
Upload only verified build artifacts.
Preserve historical releases.
Never upload signing keys, passwords, or private credentials.
Do not publish test builds as stable releases unless explicitly requested.
Security and Sensitive Information
Security is mandatory throughout the project lifecycle.

Never commit:

Keystore files
Private keys
Passwords
API secrets
Access tokens
Personal credentials
Sensitive configuration files
Use GitHub Secrets for credentials required by GitHub Actions.

Android signing requirements:

Keep release signing keys outside the repository.
Maintain a secure, encrypted backup of the original keystore.
Never rely on a temporary agent workspace as the only location of a signing key.
Preserve the original keystore and its required credentials.
Do not generate a replacement signing key for an existing published application.
Do not expose signing passwords in logs or documentation.
Verify signing configuration before producing a stable release.
If sensitive information is accidentally committed:

Stop further exposure.
Notify the project owner.
Follow an appropriate credential rotation and repository remediation process.
Do not assume deleting the file from the latest commit removes it from Git history.
Code Quality and Application Stability
All implementation work must prioritize:

Readability
Maintainability
Reliability
Performance
Security
Accessibility where applicable
Consistent architecture
Rules:

Follow the conventions of the selected language and framework.
Avoid unnecessary code duplication.
Use clear names for classes, methods, and variables.
Handle errors explicitly.
Validate user inputs.
Avoid introducing breaking changes without authorization.
Remove obsolete code only after verifying it is no longer required.
Do not introduce dependencies without a clear technical reason.
Change and Update Management
When implementing a requested update:

Work from the latest approved project state.
Inspect the current implementation before modifying it.
Do not rebuild the project from an outdated version.
Identify the affected components and dependencies.
Preserve unrelated functionality.
Update tests and documentation where necessary.
Verify the changes before committing.
For bug fixes:

Identify the root cause.
Avoid superficial workarounds when a proper fix is possible.
Add regression tests when appropriate.
Verify that the issue is actually resolved.
Data Preservation and Workspace Cleanup
Workspace cleanup is required at the end of each completed task.

Before deleting any file:

Determine whether the file may be required in future development.
Check whether it exists in GitHub or another authorized project source.
Determine whether it can be reliably regenerated or downloaded.
Preserve important files that cannot be recovered.
Never delete the only copy of essential project data.
Important files include:

Source code
Project documentation
Configuration files
Database migration files
Signing keys
Required certificates
Uncommitted work
Essential test resources
After verification:

Remove unnecessary temporary files.
Remove safe-to-delete build caches.
Remove redundant generated artifacts.
Keep the workspace organized and ready for the next task.
Never delete essential files merely to reduce workspace size.

Actions Requiring Project Owner Approval
Obtain explicit approval before:

Creating or pushing a release tag (which directly publishes a stable GitHub Release).
Deleting a repository.
Rewriting shared Git history.
Replacing an existing signing key.
Changing the application ID of an established application.
Performing destructive database migrations.
Removing important application functionality.
Introducing major architectural changes.
Deleting important project data.
Making irreversible changes to production services.
Do not interpret silence as approval.

Final Delivery Checklist
Before completing a task, verify:

Requested functionality is implemented.
Existing functionality has not been unintentionally broken.
Relevant tests have been executed.
Build results have been verified when applicable.
Documentation (README.md, .github/README.md, and relevant docs/*) has been updated.
Git changes are organized.
No secrets or sensitive files were committed.
Important files have been preserved.
Temporary files have been safely cleaned up.
The final report accurately describes the work.
The final report must be written in Persian and include:

Completed work.

Modified or created files.

GitHub changes.

Tests performed and their actual results.

Build status.

Known issues and limitations.

Remaining tasks.

Any required project owner decisions.

Project-Specific Instructions

Project-specific requirements may be added to this file or to dedicated documentation.

When project-specific instructions conflict with general recommendations:

Follow explicitly approved project requirements.
Preserve security requirements.
Ask the project owner when the conflict cannot be resolved safely.
Do not assume that instructions from another project apply to this repository.

19.1 Owner operating rules for agents
These rules are set by the project owner and take precedence over the general recommendations in sections 3, 4, 6, 7, 9, 10, 11, 12, and 17.

No pull request without an explicit request. Do not open a pull request unless the project owner directly asks for one.
No merge without an explicit request. Do not merge a pull request unless the project owner directly asks for that merge. A green pipeline is never treated as approval to merge.
One branch: dev. All changes are committed to a single branch named dev; do not create additional branches. This replaces the feature/*, fix/*, refactor/* and docs/* prefixes of section 6 for this repository. Inside an Arena Agent session the session branch is fixed by the platform and cannot be renamed or replaced, so apply the rule wherever branch creation is possible and keep the session history clean.
CI runs only after a pull request (excluding documentation-only changes). ci.yml is triggered only by the pull_request event targeting main when source code, resources, build files, or workflows change. When ci.yml runs and passes, stop, report the result and wait for the owner's approval before merging.
No CI run for documentation-only changes. Changes limited to documentation and repository metadata files (**.md, docs/**, README.md, .github/README.md, AGENTS.md, CHANGELOG.md, RELEASES.md, SECURITY.md, LICENSE, .gitattributes, .github/ISSUE_TEMPLATE/**, and PR templates) do not require running CI. ci.yml ignores these paths via paths-ignore so documentation-only pull requests do not trigger a workflow run; report such pull requests ready for owner review/merge without running CI.
Update and merge documentation before releasing. Before creating and pushing any release tag (vX.Y.Z), update all relevant documentation files (CHANGELOG.md, RELEASES.md, README.md, .github/README.md, and affected files in docs/) for that release version, include those documentation updates in the release Pull Request, and merge that Pull Request into main (with explicit owner approval for opening and merging the PR) prior to tagging. Never create a release tag first and open a post-release documentation Pull Request afterward — the tagged commit on main must already contain the finalized release documentation.
Releases start with the word tag. When the owner sends the word tag, list the available tag options (patch, minor and major with the resulting version numbers, derived from the latest release tag) and wait for the choice. Once the owner selects a version, ensure the release documentation for that version has been updated, submitted via Pull Request, and merged into main (with owner approval) before creating and pushing the release tag.
Direct release publication (no drafts). Once the owner selects and approves a release tag via the tag workflow (and pre-release documentation is merged on main), release.yml must publish the GitHub Release directly (draft: false). Do not create draft releases.
Persian Git tag descriptions. Always create annotated release tags (git tag -a vX.Y.Z -m "...") with clear Persian descriptions summarizing the changes in that version.
Dual README (README.md in English + .github/README.md in Persian). Keep the primary project README.md at the repository root in English for project and tooling use, and maintain a complete Persian (RTL) counterpart at .github/README.md so GitHub renders the Persian README on the repository landing page. Update both files together whenever README-level information changes.
APK filename format ([AppName] Vx.y.z). Generated APK files must use the exact naming pattern [AppName] Vx.y.z.apk — specifically My Pen Vx.y.z.apk (capital V and a single space between My Pen and the version, e.g., My Pen V1.0.2.apk, with companion checksum My Pen V1.0.2.apk.sha256).
Final Operating Principle
Work carefully, preserve project history, protect sensitive information, and keep the repository maintainable.

Prefer verified results over fast but unreliable changes.

Never sacrifice project integrity, data safety, or release compatibility for convenience.

Always report honestly, work transparently, and obtain approval before irreversible or sensitive operations.
