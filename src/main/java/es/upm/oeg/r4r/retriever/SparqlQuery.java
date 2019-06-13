package es.upm.oeg.r4r.retriever;

import com.github.jsonldjava.shaded.com.google.common.base.Strings;
import org.apache.http.client.HttpClient;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SparqlQuery {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlQuery.class);

    private final Path queryPath;
    private final Optional<String> id;

    public SparqlQuery(Path queryPath, Optional<String> id) {
        this.queryPath = queryPath;
        this.id = id;
    }

    public ResultSet execute(HttpClient client, String endpoint, Map<String, String[]> parameters, Integer maxSize, Integer offset) throws IOException {

        if (!queryPath.toFile().exists()) throw new IOException("Query Resource '" + queryPath + "' not found");

        String query = new String (Files.readAllBytes(queryPath), Charset.forName("UTF-8"));

        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

        if (id.isPresent()){
            Resource idLiteral = ResourceFactory.createResource(id.get());
            qs.setParam( "id", idLiteral);
        }

        Integer maxSizeParam = maxSize;
        Integer offsetParam = offset;

        if (!parameters.isEmpty()){

            for(String key: parameters.keySet()){


                String[] value = parameters.get(key);
                String val = value[0];

                if (key.equalsIgnoreCase("size")){
                    maxSizeParam = Integer.valueOf(val);
                    continue;
                }

                if (key.equalsIgnoreCase("offset")){
                    offsetParam = Integer.valueOf(val);
                    continue;
                }

                if (Strings.isNullOrEmpty(val)) continue;

                Literal literal;
                try{
                    literal = ResourceFactory.createTypedLiteral(new Integer(val));
                }catch (NumberFormatException e){
                    literal = ResourceFactory.createPlainLiteral(value[0]);
                }

                qs.setParam(key,literal);
            }

        }

        qs.append("\nLIMIT " + maxSizeParam + "\n");
        qs.append("\nOFFSET " + offsetParam+ "\n");

        Query q = qs.asQuery();

        LOG.info("Sparql-Endpoint: "+ endpoint);
        QueryExecution exec = QueryExecutionFactory.sparqlService(endpoint, q , client);
        LOG.info("->:\n" + q);
        ResultSet results = exec.execSelect();
        LOG.info("<-: rows=" + results.getRowNumber());
        return results;
    }

}
