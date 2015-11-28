# README #

This is Atomic 1.0.0.

The Atomic Framework provides an [ODATA](http://www.odata.org/) server
for JDBC data sources, according to the specifation of Apache
[Olingo](http://olingo.apache.org/) 2.0.

### How do I get set up? ###

* Run `mvn install`.
* Run `java -cp /usr/share/java/postgresql.jar:$HOME/.m2/repository/org/neptunestation/atomic-standalone/1.0.0/atomic-standalone-1.0.0-jar-with-dependencies.jar -Djdbc-driver=org.postgresql.Driver -Djdbc-url=jdbc:postgresql://127.0.0.1:5432/atomic -Dhttp-port=9080 -Ddebug=true org.neptunestation.atomic.standalone.Atomic`.

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Atomic is developed at
  [NeptuneStation](http://www.neptunestation.com/).
