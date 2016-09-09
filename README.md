# Embedded-Solr-Cloud
How to embed Apache Solr Cloud in your unit tests.

# To run tests
mvn clean compile test

# What has been demonstrated here ?

1. creation of solr cloud cluster using 1 node and 1 zookeeper instance
2. how to create a collection along with its configs
3. conncecting to this solr cloud using solr cloud client i.e. using zookeeper url

# Notes:
The tests create 2 directories 'SOLR_HOME' and 'ZK_DIR'. The test might fail if they exist, so simply delete them and tests will pass.
