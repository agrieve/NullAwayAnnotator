## CHANGELOG

### Version 1.3.15
- Improved logging for failure in the module configuration to run Annotator.
- Updated javaparser dependency.

### Version 1.3.14
- Improved performance on initialization od Annotator by parsing each source file only once.
- Added a new flag `-ll` or `--language-level` to specify the language level of the source code.

### Version 1.3.13
- Added support for fixes on record declarations.
- Bug fixes and refactorings.

### Version 1.3.12
- Set the java parser language level to 17 making the tool adaptable to source code written in Java 17.

### Version 1.3.11
- Extended downstream dependency analysis to support indexing impacts of making fields nullable on downstream dependencies.

### Version 1.3.10
- Fixed a bug in nonnull symbol detection.
- Fixed bugs in Scanner symbol serialization.

### Version 1.3.9
- Fixed bug in downstream dependency analysis.
- Updated track of flow of nullable from downstream to upstream through field writes.

### Version 1.3.8
- Fixed bug in downstream dependency analysis.
- Updated test infrastructure to compute distinct build commands for target and downstream modules.

### Version 1.3.7
- Added support for applying modifications on local variables
- Added support for type use annotation changes
- Redesigned inference engine to work with custom checkers
- Added flag `-cn, --checker-name <arg>` to required flags. (Use `NULLAWAY` for arg to run inference for NullAway)
- Enhanced lombok support by propagating @Nullable on fields to their corresponding getters.
- Renamed flag `--force-resolove` to `--suppress-remaining-errors`.
- Bug fixes and refactorings.

### Version 1.3.6
- Added inference deactivation mode.
- Split field initialization region into smaller regions where each field declaration is a region.
- Updated injector architecture.
- Fixed bug in path serialization for methods which has a super method declared in the source code.
- Added the mechanism for offset change handling for serialized offsets for errors reported by NullAway.
- Converted all data structures storing errors to `Set` from `List`.
- Fixed bug in test infrastructure.
- Updated annotation injections in Injector to simply add the annotation without indentation.
- Removed up index store.
- Added cached evaluators.
- Enhanced data structure to store the set of resolving fixes.
- Removed triggered fixes from iterations as a driver for the analysis.
- Updated unit test templates for test infrastructure to use error prone `2.0.1`
- Added tracker constructions and public APIs.
- Added support for detecting potentially impacted regions generated by Lombok.
- Changed all path types from `String` or `URI` to `Path`.
- Updated injector class search to support annotations within enum constants.
- Fixed the bug preventing annotator to add `@NullUnmarked` on constructs.
- Added support for acknowledging existing `@Nonnull` annotations and prevent annotator from adding `@Nullable` on these elements.
- Updated impacted region detection to include constructors for uninitialized fields. 
- Updated Annotator to support NullAway `0.10.10`
- Bug fixes and refactorings.

### Version 1.3.5
- Removed Lexical Preserving flag for injector.
- Updated injector to add annotations without indentation.
- Update unit test templates.
- Added infrastructure to support analysis of generated code in detecting potentially impacted regions.
- Added support for Lombok in detecting potentially impacted regions.
- Refactoring and code simplifications.

### Version 1.3.4
- Added inference deactivation mode.
- Updated injector infrastructure.
- Added infrastructure to support backward compatibility with previous versions of NullAway.
- Updated SuppressWarnings injections logic.
- Renamed maven group id and annotator core and scanner modules.
- Added release scripts to cut a release in maven central.
- Updated region computation for field declarations. 
- Bug fixes and refactorings.

### VERSION 1.3.3
- Enabled detection of flow of @Nullable back to upstream from downstream.
- Renamed `MethodInheritanceTree` to `MethodDeclarationTree`.
- Added configuration modes for downstream dependency analysis (`strict`|`lower bound` | `upper bound` | `default`).
- Updated fix tree construction with triggered fixes from downstream dependencies.
- Added report cache.
- Added extra cycle for better reduction of errors.
- Support `@NullUnmarked` and `@SuppressWarnings` injection to resolve all remaining errors.
- Bug fix in retrieving methods inserted in `MethodDeclarationTree` that are not declared in target module.
- Bug fixes and refactorings.

### VERSION 1.3.2

- Added downstream dependency analysis.
- Added Error Prone checkers to all modules.
- Enhanced analysis of downstream dependencies to execute in parallel.
- Renamed `Scanner` module to `Type Annotator Scanner`.
- Added `Library Models Loader` module to interact with `NullAway` when analyzing downstream dependencies.
- Updated `Core` module installation by forcing Maven Publish to use shadowJar output.
- Banned mutable declaration of static fields in Annotator code base.
- Added `UUID` to every generated config file by annotator to ensure re-run of the analysis.
- Updated testing infrastructure.
- Fixed bug in `Injector` when the target element is in generated code.
- Enhanced error report/handling in `Injector` when target class is not located.
- Removed parameter names and uri for method serializations.
- Added guards for wrong configurations.
- Fixed bug when `Scanner` modules crashes when not activated.
- Added javadoc to most parts of `Core` module.
- Updated installation guide and scripts.
- Added `CI` jobs.

---
### VERSION 1.1.0

Our Base Version
