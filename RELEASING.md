# Releasing webarchive-commomns

1. Update dependencies. `mvn versions:display-dependency-updates -DprocessDependencyManagementTransitive=false`
2. Prepare release notes in [CHANGES.md](CHANGES.md)
3. Prepare maven release `mvn release:prepare -Prelease`
4. Perform maven release `mvn release:perform -Prelease`