package embed.solr.cloud;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.JettyConfig;
import org.apache.solr.cloud.MiniSolrCloudCluster;
import org.apache.solr.cloud.ZkTestServer;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;

/**
 * Created by vishnuhr on 9/9/16.
 */
@Log4j
public abstract class BaseTest {

  public static final String SOLR_HOME_DIR = "SOLR_HOME";
  public static final String ZK_DIR = "ZK_DIR";
  public static final int ZK_PORT = 2188;

  private static String SOLR_XML = "<solr>\n\n  <str name=\"shareSchema\">${shareSchema:false}</str>\n  <str name=\"configSetBaseDir\">${configSetBaseDir:configsets}</str>\n  <str name=\"coreRootDirectory\">${coreRootDirectory:.}</str>\n\n  <shardHandlerFactory name=\"shardHandlerFactory\" class=\"HttpShardHandlerFactory\">\n    <str name=\"urlScheme\">${urlScheme:}</str>\n    <int name=\"socketTimeout\">${socketTimeout:90000}</int>\n    <int name=\"connTimeout\">${connTimeout:15000}</int>\n  </shardHandlerFactory>\n\n  <solrcloud>\n    <str name=\"host\">127.0.0.1</str>\n    <int name=\"hostPort\">${hostPort:8983}</int>\n    <str name=\"hostContext\">${hostContext:solr}</str>\n    <int name=\"zkClientTimeout\">${solr.zkclienttimeout:30000}</int>\n    <bool name=\"genericCoreNodeNames\">${genericCoreNodeNames:true}</bool>\n    <int name=\"leaderVoteWait\">10000</int>\n    <int name=\"distribUpdateConnTimeout\">${distribUpdateConnTimeout:45000}</int>\n    <int name=\"distribUpdateSoTimeout\">${distribUpdateSoTimeout:340000}</int>\n  </solrcloud>\n  \n</solr>\n";
  private static int numServersInCloud = 1;

  protected MiniSolrCloudCluster miniSolrCloudCluster;
  protected ZkTestServer zkTestServer;
  private File solrHome;
  private File zkDir;

  @Before
  public void setup() throws Exception {

    createDirs();
    JettyConfig jettyConfig = createJettyConfig();

    zkTestServer = createZkServer();
    zkTestServer.run();
    log.info(
        "Started Zk server. Configure solr client to use address: " + zkTestServer.getZkAddress());

    miniSolrCloudCluster = new MiniSolrCloudCluster(numServersInCloud,
        FileSystems.getDefault().getPath(solrHome.getAbsolutePath()),
        SOLR_XML,
        jettyConfig, zkTestServer);
    log.info("Created Minicluster with number of nodes = " + numServersInCloud);
  }

  @After
  public void tearDown() {
    try {
      miniSolrCloudCluster.shutdown();
    } catch (Exception e) {
      log.warn("Failed to shut down mini cluster. ", e);
    }
    try {
      zkTestServer.shutdown();
    } catch (Exception e) {
      log.warn("Failed to shut down zk. ", e);
    }
    try {
      FileUtils.deleteDirectory(solrHome);
    } catch (IOException e) {
      log.warn("Failed to delete SOLR HOME dir. ", e);
    }
    try {
      FileUtils.deleteDirectory(zkDir);
    } catch (IOException e) {
      log.warn("Failed to delete ZK dir. ", e);
    }
    log.info("Tear down complete.");
  }

  private void createDirs() {
    solrHome = new File(SOLR_HOME_DIR);
    zkDir = new File(ZK_DIR);
    solrHome.mkdir();
    zkDir.mkdir();
    log.info("Created solr home and zk dirs");
  }

  private ZkTestServer createZkServer() {
    log.info("Creating zk instance ...");
    return new ZkTestServer(zkDir.getAbsolutePath(), ZK_PORT);
  }

  private JettyConfig createJettyConfig() {
    return JettyConfig.builder()
        .setPort(8989) // if you want multiple servers in the solr cloud comment it out.
        .setContext("/solr")
        .stopAtShutdown(true)
        .withServlets(new HashMap<ServletHolder, String>())
        .withSSLConfig(null)
        .build();
  }
}
