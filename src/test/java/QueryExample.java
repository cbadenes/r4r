import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class QueryExample {

    private static final Logger LOG = LoggerFactory.getLogger(QueryExample.class);

    @Test
    public void query() throws IOException {


        String sparqlEndpoint = "http://dbpedia.org/sparql";

        Path queryPath = Paths.get("src","test","resources", "movies/query.sparql");

        String query = new String (Files.readAllBytes(queryPath), Charset.forName("UTF-8"));

        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

        Literal london = ResourceFactory.createLangLiteral( "London", "en" );
        qs.setParam( "label", london );

        System.out.println( qs );

        QueryExecution exec = QueryExecutionFactory.sparqlService( sparqlEndpoint, qs.asQuery() );

        // Normally you'd just do results = exec.execSelect(), but I want to
        // use this ResultSet twice, so I'm making a copy of it.


        ResultSet results = exec.execSelect();


        List<String> resultVars = results.getResultVars();

        LOG.info("Result Vars: " + results.getResultVars());

        // A simpler way of printing the results.
        ResultSetFormatter.out( results );
    }

}
