package es.upm.oeg.r4r.retriever;

import com.github.jsonldjava.shaded.com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SparqlQuery {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlQuery.class);

    private final Path queryPath;
    private final Optional<String> id;
    private final String regex;
    private final Pattern pattern;

    public SparqlQuery(Path queryPath, Optional<String> id) {
        this.queryPath = queryPath;
        this.id = id;
        this.regex = "(\\?[w]*)\\w+";
        this.pattern = Pattern.compile(regex, Pattern.MULTILINE);
    }

    public ResultSet execute(HttpClient client, String endpoint, Map<String, String[]> parameters, Integer maxSize, Integer offset) throws IOException {

        if (!queryPath.toFile().exists()) throw new IOException("Query Resource '" + queryPath + "' not found");

        String query = new String (Files.readAllBytes(queryPath), Charset.forName("UTF-8"));

        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

        if (id.isPresent()){
            Resource idLiteral = ResourceFactory.createResource(id.get());
            qs.setParam( "id", idLiteral);
            String shortId = StringUtils.substringAfterLast(id.get(), "/");
            if (!Strings.isNullOrEmpty(shortId)){
                Literal shortIdLiteral = ResourceFactory.createStringLiteral(shortId);
                qs.setParam( "sid", shortIdLiteral);
            }
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

        int offsetValue = maxSizeParam * offsetParam;

        qs.append("\nLIMIT " + maxSizeParam + "\n");
        qs.append("\nOFFSET " + offsetValue+ "\n");

        Query tq = qs.asQuery();

        Map<String, Integer> rvMap = new HashMap<>();
        tq.getResultVars().forEach(v -> rvMap.put(v,1));

        ElementGroup eg = (ElementGroup) tq.getQueryPattern();
        List<Element> elements = eg.getElements();
        for (Element el : elements){
            if (el instanceof ElementFilter){
                String qel = el.toString();

                // initialize filtering variables
                Matcher matcher = pattern.matcher(qel);
                while (matcher.find()) {
                    String qvar = matcher.group(0);
                    String key = StringUtils.substringAfter(qvar,"?");
                    if (!rvMap.containsKey(key) && !qs.getVariableParameters().containsKey(key)){
                        LOG.info("Filtering Query Parameter: " + qvar);
                        Literal literal = ResourceFactory.createPlainLiteral("");
                        qs.setParam(qvar,literal);
                    }
                }
            }
        }

        Query q = qs.asQuery();

        LOG.info("Sparql-Endpoint: "+ endpoint);
        LOG.info("Query Params: " + qs.getVariableParameters());
        QueryExecution exec = QueryExecutionFactory.sparqlService(endpoint, q , client);
        LOG.info("->:\n" + q);
        ResultSet results = exec.execSelect();
        return results;
    }

}
