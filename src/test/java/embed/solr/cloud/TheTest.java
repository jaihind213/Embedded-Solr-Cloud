package embed.solr.cloud;


import lombok.extern.log4j.Log4j;

import junit.framework.Assert;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;

/**
 * The Test cases
 */
@Log4j
public class TheTest extends BaseTest {

  String configName = "test-collection-config";
  String collectionName = "test-collection";

  @Test
  public void testPingCluster() throws IOException, SolrServerException {
    ClassLoader classLoader = getClass().getClassLoader();
    String collectionConfDir = classLoader.getResource("test-collection-config").getFile();

    //upload config for collection
    miniSolrCloudCluster.getSolrClient().uploadConfig(FileSystems.getDefault().getPath(
        collectionConfDir), configName);

    //create collection
    miniSolrCloudCluster.createCollection("test-collection", 1, 1, configName,
        new HashMap<String, String>());

    miniSolrCloudCluster.getSolrClient().setDefaultCollection("test-collection");

    log.info("Pinging cluster....");
    log.info(miniSolrCloudCluster.getSolrClient().ping());
    log.info("Pinging Done....");
  }

  @Test
  public void testPutAndGetDocumentsTest() throws IOException, SolrServerException {
    ClassLoader classLoader = getClass().getClassLoader();
    String collectionConfDir = classLoader.getResource("test-collection-config").getFile();

    //upload config for collection
    miniSolrCloudCluster.getSolrClient().uploadConfig(FileSystems.getDefault().getPath(
        collectionConfDir), configName);

    //create collection
    miniSolrCloudCluster.createCollection(collectionName, 1, 1, configName,
        new HashMap<String, String>());

    //using cloud solr client instead of client of mini cluster .. for demonstration purposes.
    CloudSolrClient cloudClient = new CloudSolrClient("127.0.0.1:" + ZK_PORT + "/solr", true);
    cloudClient.connect();
    log.info("Successfully connected to solr cloud using Cloud solr client");

    //create records and commit.
    SolrInputDocument doc1 = new SolrInputDocument();
    doc1.addField("id", "vishnu");

    SolrInputDocument doc2 = new SolrInputDocument();
    doc2.addField("id", "rao");

    cloudClient.setDefaultCollection(collectionName);
    cloudClient.add(doc1);
    cloudClient.add(doc2);
    cloudClient.commit();
    log.info("Successfully commited records to solr cloud using Cloud solr client");

    //now query the records.
    final ModifiableSolrParams solrQuery = new ModifiableSolrParams();

    solrQuery.set("qt", "/select");
    solrQuery.set("q", "*:*");
    solrQuery.set("wt", "json");
    solrQuery.set("facet", true);
    solrQuery.set("rows", 20);
    solrQuery.set("collection", collectionName);

    QueryResponse response = miniSolrCloudCluster.getSolrClient().query(solrQuery);
    Assert.assertEquals(2, ((SolrDocumentList) response.getResponse().get("response")).size());
    log.info("Successfully fetched records from solr cloud using Cloud solr client");
  }

}
