# Dependency Matrix & Component Approval

## 1. Approved Ecosystem Modules

The following upstream Apotheosis components are approved for porting into the combined Fabric distributable:

| Module / Component | Approval Status | Scope & Responsibilities | Notes / Constraints |
| :--- | :--- | :--- | :--- |
| **Apotheosis Core** | **APPROVED** | Module loading, shared configuration, utilities, shared tags | Cohesive single mod ID |
| **Placebo (Required Subsets)** | **APPROVED** | Recipe registration helpers, config bridges, JSON utility types | Port only functionality strictly required by modules |
| **Apothic Spawners** | **APPROVED** | Spawner modifiers, silk-touch capture, delayed spawning, stat control | High-priority milestone |
| **Apothic Attributes** | **APPROVED** | Custom attributes, attribute formatting, stat integration | Exclude obsolete custom mining speed in favor of vanilla `MINING_EFFICIENCY` |
| **Apothic Enchanting** | **APPROVED** | Extended enchanting table, stats (eterna/quanta/arcana), tomes, capturing | Align with Minecraft 26.3 data-driven enchantment format |
| **Apothic Adventure** | *PENDING APPROVAL* | Bosses, affixes, socketing, gems | Requires review of entity renderers and RenderPearl GPU pipelines |

---

## 2. Platform Dependencies & Third-Party Integrations

| Dependency | Scope | Version Target | Usage Justification |
| :--- | :--- | :--- | :--- |
| **Fabric API** | Compile & Runtime | Compatible with 26.3 | Events, networking, resource reloader, registry extensions |
| **YetAnotherConfigLib (YACL)** | Compile & Runtime | v3.x for 26.3 | Standardized GUI configuration for client with server sync |
| **RenderPearl** | Compile & Runtime | Public APIs for 26.3 | Modern GPU rendering pipeline integration where high-level vanilla APIs are insufficient |
| **ModMenu** | Runtime (Client optional) | Compatible with 26.3 | Access point for YACL config screen |

---

## 3. Excluded Upstream Elements

| Component | Status | Reason |
| :--- | :--- | :--- |
| **NeoForge Core / FML** | **STRICTLY EXCLUDED** | Incompatible architecture; replaced with Fabric API and native mixins |
| **Custom Mining Speed Attribute** | **EXCLUDED** | Obsolete; vanilla 26.3 natively provides `Attributes.MINING_EFFICIENCY` |
| **NeoForge Capabilities** | **EXCLUDED** | Replaced with Fabric Transfer API / vanilla `DataComponents` where applicable |
