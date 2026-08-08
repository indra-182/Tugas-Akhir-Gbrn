# Task 2 Report

## Status

Implemented the source-level regression assertion in `NormalisasiNilaiCalculatorTest`.

## Changes

- Added UTF-8 source reading imports.
- Added `shouldNotExposeCriterionTypeInCriteriaPanel()` and invoked it from `main`.
- The assertion fails when `KriteriaPanel.java` contains `tipeComboBox` or the exact `"Jenis"` label.
- No production source changes remain; Task 1's production cleanup is preserved.

## Verification

- RED check: temporarily inserted a `"Jenis"` sentinel into `KriteriaPanel.java`; the test failed with `AssertionError: Criterion type controls must be removed`. Sentinel was removed afterward.
- GREEN check: direct Java 8 compilation of all production sources, test compilation, and `java -ea` execution passed.
- `ant clean jar`: fails before compilation with `Class not found: javac1.8` under the installed Ant/toolchain, as documented in the task constraints.

## Commit

Commit: `test: guard removed criterion type UI`
