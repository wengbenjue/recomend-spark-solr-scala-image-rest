package com.soledede.recomend.solr.impl

import com.soledede.recomend.solr.SolrClient
import org.apache.solr.client.solrj.impl.{HttpSolrClient, HttpSolrServer}

/**
  * Created by soledede on 2015/12/16.
  */
class HttpSolrQuery extends SolrClient{


  override def searchByUrl(url:String):AnyRef = {
     val url: String = "http://localhost:8983/solr"
    val server = new HttpSolrClient( url );
    //server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
    server.setConnectionTimeout(5000); // 5 seconds to establish TCP
    // Setting the XML response parser is only required for cross
    // version compatibility and only when one side is 1.4.1 or
    // earlier and the other side is 3.1 or later.
    //server.setParser(new XMLResponseParser()); // binary parser is used by default
    // The following settings are provided here for completeness.
    // They will not normally be required, and should only be used
    // after consulting javadocs to know whether they are truly required.
    server.setSoTimeout(1000);  // socket read timeout
    server.setDefaultMaxConnectionsPerHost(100);
    server.setMaxTotalConnections(100);
    server.setFollowRedirects(false);  // defaults to false
    // allowCompression defaults to false.
    // Server side must support gzip or deflate for this to have any effect.
    server.setAllowCompression(true);
    null
  }
}
