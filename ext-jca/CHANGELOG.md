About Version
======================

The original jca adaptor is at https://github.com/gaol/vertx-resource-adapter, where 3 releases were made without uploading to Maven Central Repository. After that, it was adopted by Vert-x community, when it starts to have official Maven groupId and aritifactId as:

<pre>

  &lt;dependency&gt;
    &lt;groupId&gt;io.vertx&lt;/groupId&gt;
    &lt;artifactId&gt;jca-adaptor&lt;/artifactId&gt;
  &lt;/dependency&gt;
</pre>

The first version available in Maven Repository will be <b>1.0.3.Beta1</b>, which uses Vertx 2.1RC1.

The <b>1.0.0</b>, <b>1.0.1</b> and <b>1.0.2</b> releases can be found in [Bintary](https://bintray.com/gaol/downloads/vertx-resource-adapter)


Changelogs:
======================

1.0.3.Beta1
------
* Tue Mar 11 2014 Lin Gao <aoingl@gmail.com> - 1.0.3.Beta1
- Update vertx to 2.1RC1
- Using ProgrammableClusterManagerFactory to load customer hazelcast configuration file
- First release to Maven Repository
- Adopted by Vert.x community

1.0.2
------
* Sun Feb 09 2014 Lin Gao <aoingl@gmail.com> - 1.0.2
- Fix dead lock on inbound communication
- Update provided default-cluster.xml to use multicast by default
- Added the examples

1.0.1
------
* Mon Jan 20 2014 Lin Gao <aoingl@gmail.com> - 1.0.1
- Switch to use Vertx provided ClusterManager(see: https://github.com/eclipse/vert.x/pull/759)
- Change the @ConfigPropety annotation to setter method, because Glassfish only knows about setter methods
- Update vertx to 2.1M3

1.0.0
------
* Sat Jan 18 2014 Lin Gao <aoingl@gmail.com> - 1.0.0
- First release of jca-adaptor
- Uses vertx:2.1M1
- Uses its own cluster implementation to communicate with external Vertx platform
