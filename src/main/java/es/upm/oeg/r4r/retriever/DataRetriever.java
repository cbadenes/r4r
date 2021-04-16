package es.upm.oeg.r4r.retriever;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import es.upm.oeg.r4r.data.Request;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class DataRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(DataRetriever.class);

    @Value("#{environment['SPARQL_ENDPOINT']?:'${sparql.endpoint}'}")
    String sparqlEndpoint;

    @Value("#{environment['GRAPH_IRI']?:'${graph.iri}'}")
    String graphIRI;

    @Value("#{environment['SPARQL_SIZE']?:${sparql.size}}")
    Integer maxSize;

    @Value("#{environment['SPARQL_OFFSET']?:${sparql.offset}}")
    Integer offset;

    @Autowired
    QueryFactory sparqlQueryFactory;

    CloseableHttpClient httpclient;

    @PostConstruct
    public void setup(){
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

        //Set the maximum number of connections in the pool
        connManager.setMaxTotal(100);

        HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(connManager);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(1000)
                .build();

        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Header> headers = Lists.newArrayList(header);

        //Build the CloseableHttpClient object using the build() method.
        httpclient = clientbuilder.create()
                .setDefaultHeaders(headers)
                .disableAuthCaching()
                .disableAutomaticRetries()
                .disableConnectionState()
                .disableContentCompression()
                .disableCookieManagement()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }


    public Optional<ResultSet> retrieve(String[] resources, Map<String, String[]> parameters){

        try {
            SparqlQuery query   = sparqlQueryFactory.newQuery(Request.Type.GET, resources);
            ResultSet result    = query.execute(httpclient, sparqlEndpoint, graphIRI, parameters, maxSize, offset);
            return Optional.of(result);
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            return Optional.empty();
        }

    }

}
