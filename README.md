# maven-watch [![Build Status](https://travis-ci.com/dkapil/maven-watch.svg?branch=master)](https://travis-ci.com/dkapil/maven-watch)
A watcher for huge multi-module maven projects

Multi-module maven projects can become unmanageable when they grow in size. Nested pom packaging and child projects existing in hierarchies makes it difficult to manage dependencies, plugins as well as sometimes these results into unexpected behaviour.

Some common problems in multi-module projects
- Redundant dependencies re-declaration in child projects goes unnoticed and these does not gets the benefit if dependency is updated in parent projects.
- Redundant plugins re-declaration in child projects sometimes makes the overall behaviour unexpected.

What this project expects to solve
- Provides an interface to navigate multi-module maven projects easily
- Provides information about re-declaration of plugins in child projects
- Provides information about re-declaration of dependencies in child projects
- Provides information about current state of dependencies used across projects
