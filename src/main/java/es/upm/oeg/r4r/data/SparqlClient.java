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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public ResultSet query(String resource) throws IOException {
        // Sparql Query
        Path queryPath = Paths.get("src",resourceFolder,"resources", resource, "get.sparql");

        String query = new String (Files.readAllBytes(queryPath), Charset.forName("UTF-8"));

        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

//        Literal london = ResourceFactory.createLangLiteral( "London", "en" );
//        qs.setParam( "label", london );

        LOG.info("Input Query:\n"  + qs );

        QueryExecution exec = QueryExecutionFactory.sparqlService( sparqlEndpoint, qs.asQuery() );

        ResultSet results = exec.execSelect();

        return results;
    }

    public ResultSet query(String resource, String id) throws IOException {
        // Sparql Query
        Path queryPath = Paths.get("src",resourceFolder,"resources", resource, "getById.sparql");

        String query = new String (Files.readAllBytes(queryPath), Charset.forName("UTF-8"));

        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

        String domainURI = sparqlDomain.endsWith("/")?sparqlDomain : sparqlDomain+"/";
        Resource idResource = ResourceFactory.createResource(domainURI+id);
        qs.setParam( "id", idResource);

        LOG.info("Input Query:\n"  + qs );

        QueryExecution exec = QueryExecutionFactory.sparqlService( sparqlEndpoint, qs.asQuery() );

        ResultSet results = exec.execSelect();

        return results;
    }

    public ResultSet query(String baseResource, String id, String innerResource) throws IOException {
        // Sparql Query
        Path queryPath = Paths.get("src",resourceFolder,"resources", baseResource, innerResource, "get.sparql");

        String query = new String (Files.readAllBytes(queryPath), Charset.forName("UTF-8"));

        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

        Literal idLiteral = ResourceFactory.createStringLiteral(id);
        qs.setParam( "id", idLiteral);

        LOG.info("Input Query:\n"  + qs );

        QueryExecution exec = QueryExecutionFactory.sparqlService( sparqlEndpoint, qs.asQuery() );

        ResultSet results = exec.execSelect();

        return results;
    }
}
