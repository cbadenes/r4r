package es.upm.oeg.r4r.retriever;

import es.upm.oeg.r4r.data.Request;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    @Value("#{environment['SPARQL_SIZE']?:${sparql.size}}")
    Integer maxSize;

    @Value("#{environment['SPARQL_OFFSET']?:${sparql.offset}}")
    Integer offset;


    @Autowired
    QueryFactory sparqlQueryFactory;


    public Optional<ResultSet> retrieve(String[] resources, Map<String, String[]> parameters){

        try {
            SparqlQuery query   = sparqlQueryFactory.newQuery(Request.Type.GET, resources);
            ResultSet result    = query.execute(sparqlEndpoint, parameters, maxSize, offset);
            return Optional.of(result);
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            return Optional.empty();
        }

    }

}
