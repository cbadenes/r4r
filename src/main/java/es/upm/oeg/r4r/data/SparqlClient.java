package es.upm.oeg.r4r.data;

import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class SparqlClient {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlClient.class);

    @Value("#{environment['SPARQL_ENDPOINT']?:'${sparql.endpoint}'}")
    String sparqlEndpoint;

    @Value("#{environment['SPARQL_DOMAIN']?:'${sparql.domain}'}")
    String sparqlDomain;

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    private String resourceUri;

    @PostConstruct
    public void setup(){
        resourceUri = sparqlDomain.endsWith("/")? sparqlDomain : sparqlDomain + "/";
    }

    public ResultSet query(String resource) throws IOException {

        Path queryPath = Paths.get(resourceFolder, resource, "get.sparql");
        return query(queryPath, Optional.empty());
    }

    public ResultSet query(String resource, String id) throws IOException {

        Path queryPath = Paths.get(resourceFolder, resource, "getById.sparql");
        return query(queryPath, Optional.of(id));
    }

    public ResultSet query(String resource, String id, String innerResource) throws IOException {

        Path queryPath = Paths.get(resourceFolder, resource, innerResource, "get.sparql");
        return query(queryPath, Optional.of(id));
    }


    private ResultSet query(Path path, Optional<String> id) throws IOException {
        String query = new String (Files.readAllBytes(path), Charset.forName("UTF-8"));

        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

        if (id.isPresent()){
            Resource idLiteral = ResourceFactory.createResource(resourceUri+id.get());
            qs.setParam( "id", idLiteral);
        }

        LOG.debug("Input Query:\n"  + qs );

        QueryExecution exec = QueryExecutionFactory.sparqlService( sparqlEndpoint, qs.asQuery() );

        ResultSet results = exec.execSelect();

        return results;
    }
}
