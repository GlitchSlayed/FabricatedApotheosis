# Apotheosis Fabric Port — Project Instructions

## Goal

Port the pinned Minecraft 26.1.2 NeoForge Apotheosis ecosystem to
Minecraft 26.3 Fabric, with one combined ecosystem distributable.

Include Apotheosis, required Placebo functionality, Apothic Attributes,
Apothic Enchanting, Apothic Spawners, and the additional components
explicitly approved in `docs/DEPENDENCY_MATRIX.md`.

Use Fabric API and YACL. Use Minecraft 26.3 rendering abstractions,
including public RenderPearl APIs where needed.

## Starting a task

Before implementation, read:
- `docs/STATUS.md`
- `docs/DECISIONS.md`
- `docs/UPSTREAM_LOCK.md`
- `docs/REFERENCE_INDEX.md`
- Relevant sections of `docs/FEATURE_PARITY.md`
- Relevant source and migration audit documents

If these files do not exist during initial setup, create them as part
of discovery. Do not invent previous decisions or completed work.

Implement the requested milestone and its necessary supporting work.
Preserve unrelated changes.

## Local Minecraft source references

The `reference/` directory (aliasing `Refrences/`) contains local mcsrc source
trees and client JARs for Minecraft 26.1.2 and Minecraft 26.3:
- Keep and use these existing local copies; do not download replacement trees
  unless something required is missing.
- Discover their exact paths and record them in `docs/REFERENCE_INDEX.md`.
- Record version/build identity, source provenance, mapping/naming scheme,
  and client/server coverage when ascertainable. Mark unknowns explicitly.
- Include `REFERENCE_INDEX` findings in `docs/HANDOFF_REPORT.md`.

Treat these reference files and source trees as read-only:
- Do not edit, format, rename, move, or delete existing reference files.
- Ensure the Minecraft reference trees are strictly excluded from Git (`.gitignore`),
  Gradle source sets, generated source archives (`sourcesJar`), and distributable
  artifacts (`jar`), while remaining readable by local file/search tools (`javap`,
  `unzip`, python inspection).
- Preserve any files already tracked; report those instead of removing them.

Use targeted searches and read relevant classes/methods rather than
loading entire source trees into context.

For version-sensitive implementation:
1. Inspect the relevant 26.1.2 vanilla code.
2. Inspect the upstream mod and any NeoForge hooks or patches it uses.
3. Inspect the corresponding 26.3 vanilla code.
4. Identify the actual behavioral change.
5. Choose vanilla functionality, Fabric API, or a narrow mixin.
6. Record the evidence and verification needed.

Vanilla mcsrc does not contain every NeoForge extension or patch.
Consult the pinned NeoForge source where upstream behavior depends on it.

Use 26.3 source as the target API reference. Use the pinned upstream
mod implementation as the reference for intended mod behavior.

If source availability, mappings, or build identity are uncertain,
report the uncertainty rather than guessing method names.

## Avoid unnecessary duplication

Before porting a custom attribute, helper, renderer, recipe mechanism,
or data system, check whether vanilla 26.3 or Fabric API already
provides equivalent functionality.

Equivalence requires matching:
- Meaning and affected entities/items
- Default values, units, and bounds
- Modifier operations
- Execution timing and ordering
- Persistence and synchronization
- Relevant edge cases

A similar name is not proof of equivalence.
NeoForge-provided functionality is not automatically vanilla functionality.

Record keep/replace/adapt decisions in `docs/VANILLA_OVERLAP.md`.
Do not reintroduce upstream functionality already removed in favor
of vanilla, including the obsolete custom mining-speed attribute.

## Implementation requirements

- Preserve upstream gameplay unless an intentional difference is recorded.
- Preserve persistent identifiers or implement explicit migrations.
- Never bundle NeoForge binaries into Fabric.
- Prefer vanilla, then Fabric API, then narrowly targeted mixins.
- Do not build a general NeoForge compatibility layer.
- Keep client-only code out of dedicated-server class loading.
- Keep gameplay calculations and inventory transactions server-authoritative.
- Prevent duplicate attribute application, damage processing, and registration.
- Preserve reload behavior and invalidate caches correctly.
- Do not advertise upstream API compatibility that is not implemented.

For every mixin, document its purpose, target, injection point,
ordering requirements, and regression coverage.

## Rendering

Prefer Minecraft/Fabric high-level rendering integration.
Use public RenderPearl abstractions when direct GPU work is necessary.

Do not use raw OpenGL/Vulkan calls, backend-specific casts, or imports
from RenderPearl backend implementations for ordinary mod rendering.

Respect render-state extraction, render passes, resource ownership,
buffer lifetimes, resource reload, resizing, and cleanup.

Do not claim OpenGL or Vulkan compatibility without recording which
backends were actually tested.

## Verification and reporting

Never present stubs, no-ops, disabled features, or compilation alone
as completed functionality.

Run checks appropriate to the change. Distinguish:
- Compilation
- Automated behavioral tests
- Client launch
- Dedicated-server launch
- In-game verification
- Multiplayer verification

Do not claim tests passed if they were not executed.

After each milestone update:
- `docs/FEATURE_PARITY.md`
- `docs/STATUS.md`
- `docs/HANDOFF_REPORT.md`
- `docs/DECISIONS.md` when a decision changes

Report changed files, tests and outcomes, remaining limitations,
and the next concrete implementation task.

Do not publish releases without an explicit request.
