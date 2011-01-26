Grisu-connectors
===============

Grisu connectors is a meta-project that contains modules that enable connections between a grisu backend and frontend. At the moment only one such connector is implemented: **backend-modules/enunicate-backend**. It publishes the [Grisu API](https://compute-dev.services.bestgrid.org/) via SOAP and REST.

Prerequisites
--------------------

In order to build grisu-connectors from the svn sources, you need: 

- Sun Java Development Kit (version ≥ 6)
- [git](http://git-scm.com) 
- [Apache Maven](http://maven.apache.org) (version >=2)


Checking out sourcecode
-------------------------------------

 `git clone git://github.com/grisu/grisu-connectors.git`

Build grisu-connectors with Maven
------------------------------------------

To build one of the above modules, cd into the module root directory of the module to build and execute: 

    cd grisu-connectors
    mvn clean install
