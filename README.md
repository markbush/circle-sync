circle-sync
===========
Author: Mark Bush, Ocado Ltd

The circle-sync application is an example of how to use the Google+
Domains API.  The application provides a way of managing the members of
one or more Circles based on an external source.

This application is set up as an Eclipse project, however you can import
it into your preferred development environment.

Builing the application
-----------------------

I have used Maven to manage the build process.  If you are using an IDE,
then there will be a plugin available which will help with building and
running projects.  You can build on the command line using:

mvn package appassembler:assemble

The "package" goal compiles the source and then packages it into a JAR
file.  The "appassembler:assemble" goal creates an application setup
in the project's "target/appassembler" folder containing all the dependant
JARs and scripts (for both Windows and UNIX based systems) that enable
you to run it easily.

Configuring the Application
---------------------------

By default, the application looks for a file called "circle-sync.conf" in
the current directory.  This file should have lines of the form:

<source identifier>:<Circle name>

All white space in the file is significant.  The base application expects
<source identifier> to be the name of a group to be looked up in LDAP
(such as Active Directory).

NOTE: the group name should be fully qualified, such as:
      CN=Marketing,OU=Groups,DC=example,DC=com

The Circle will be created if it doesn't already exist.

You can specify an alternate configuration file as an argument to the
application when you run it.

If you replace the definition of "sourceLoader" in CircleSync with an
instance of FileSourceLoader, then the source identifier can be the
name of a file containing email addresses (one per line).

Running the Application
-----------------------

On Windows, use the generated script:
target/appassembler/bin/circle-sync.bat

On UNIX based systems (Mac, Linux, etc), use the alternate script:
target/appassembler/bin/circle-sync
