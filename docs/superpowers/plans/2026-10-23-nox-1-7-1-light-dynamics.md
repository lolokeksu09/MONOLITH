# NOX 1.7.1 — Light Dynamics patch execution

1. Record test contracts for AURORA multi-layer ribbons, PULSE outward seamless waves with touch centre, COMET varied depth/velocity/culling, and stability of original shader branches. Observe expected test failure. Commit test and changelog.
2. Replace only mode 3–5 GLSL shader blocks in GlBackend.kt. Keep mode 0–2 byte-identical, use existing uniforms and fixed loops. Document and commit.
3. Version to 1.7.1/code 11, side-by-side debug package .lumina, new artifact NOX-1.7.1-light-dynamics-APK, update README, CHANGELOG and QA_STATUS. Commit.
4. CI review and error fixes with individual documented commits; pull request targeting feature/nox-1-7-living-scenes (not main).
5. Download exact-head CI artifact and inspect ZIP and APK. Report unverified GPU runtime on Lenovo.
