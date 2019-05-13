import org.apache.jena.query.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CommandLineExample {

    private static final Logger LOG = LoggerFactory.getLogger(CommandLineExample.class);


    String sparqlEndpoint = "http://dbpedia.org/sparql";

    String resource = System.getProperty("resource");


    @Test
    public void query() throws IOException {


        // Sparql Query
        Path queryPath = Paths.get("src","test","resources", resource, "get.sparql");

        String query = new String (Files.readAllBytes(queryPath), Charset.forName("UTF-8"));

        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

//        Literal london = ResourceFactory.createLangLiteral( "London", "en" );
//        qs.setParam( "label", london );

        LOG.info("Input Query:\n"  + qs );

        QueryExecution exec = QueryExecutionFactory.sparqlService( sparqlEndpoint, qs.asQuery() );

        ResultSet results = exec.execSelect();


        // Velocity Template
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        Template t = velocityEngine.getTemplate(resource + File.separator + "get.vm");

        VelocityContext context = new VelocityContext();

        List<Map<String,String>> mapResults = new ArrayList<>();

        while(results.hasNext()){

            QuerySolution result = results.next();

            Map<String,String> mapResult = new HashMap<>();

            Iterator<String> varNames = result.varNames();
            while(varNames.hasNext()){
                String varName = varNames.next();
                mapResult.put(varName, result.get(varName).toString());
            }

            mapResults.add(mapResult);
        }


        context.put("resultList", mapResults);

        StringWriter fw = new StringWriter();
        t.merge(context, fw);
        fw.close();

        LOG.info("Result JSON:\n" + fw);

    }

}
