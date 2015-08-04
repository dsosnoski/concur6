concur6
============

The [sixth article in my JVM Concurrency series](http://www.ibm.com/developerworks/library/j-jvmc6/index.html) on IBM
developerWorks, "Building actor applications with Akka", gives an introduction to constructing actual
applications with actor interactions. This article only uses Scala code, since it's significantly more
readable than Java code would be. See [the preceding article](http://www.ibm.com/developerworks/library/j-jvmc6/index.html)
to see how Java actor code using Akka matches up to the Scala equivalent.

The project uses a Maven build, so just do the usual `mvn clean install` to get
everything to a working state. The code is in a single package, `com.sosnoski.concur.article6scala`,
within the *main/scala* tree.

You can run the sample applications from the command line with
`mvn scala:run -Dlauncher={name}`, where {name} selects the test code:

1. `stars1` - Simple Stars example
2. `stars2` - Movie-making with Stars example

You can import the project into ScalaIDE with the standard Maven project import handling.
