# honeycomb

## What is Honeycomb?
Honeycomb is distributed key/value storage (currently, in-memory storage).

## What can Honeycomb do? (What problems does Honeycomb solve?)
- Different with memcached, it supports multi-node systems.
- Easy interface (http REST interface - only GET/POST with simple URI)
- Easy to add a new node (by using consistent-hashing)
- Currently NOT, BUT in TODO list
  - Fault tolerance
  - No SPOF (single point of failure)

## Design
Honeycomb has single parent actor (under guardian) called controller, and it has four children - hashService, membershipService, bucketService and zookeeperService.
- Controller: it manages children actors' lifecycle.
- HashService: hash function, for example md5
- BucketService: key/value store by using tree map (treemap inherits from red-black tree)
- ZooKeeperCLient: currently, it is dummy actor. Goal is to communicate with zookeeper for leader election.
- RESTService: It is NOT actor, but is HTTP server. (by using unfiltered library)
<pre>
+-------------------------------------------+
|     [RESTService]                         |
|                                           |
|     [Controller] +--- [HashService]       |
|                  +--- [MembershipService] |
|                  +--- [BucketService]     |
|                  +--- [ZooKeeperClient]   |
+-------------------------------------------+
Logical execution context in single honeycomb instance
</pre>

## How to compile
Use sbt<br />
$ sbt compile

## TODO
- No SPOF
- Fault tolerance
- Persistent storage backup (for recovery when crash)
- Leader selection by using zookeeper
