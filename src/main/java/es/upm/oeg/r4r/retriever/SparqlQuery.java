package es.upm.oeg.r4r.retriever;

import com.github.jsonldjava.shaded.com.google.common.base.Strings;
import es.upm.oeg.r4r.data.Index;
import es.upm.oeg.r4r.data.QueryView;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDFloat;
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
import java.util.stream.Collectors;

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
        String sortCriteria = "";
        String sortField = "";

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

                if (key.equalsIgnoreCase("sort")){
                    sortCriteria    = val.toLowerCase().startsWith("-")? "DESC" : "ASC";
                    sortField       = sortCriteria.equalsIgnoreCase("DESC")? StringUtils.substringAfter(val, "-") : val.trim();
                    continue;
                }

                if (Strings.isNullOrEmpty(val)) continue;

                Literal literal;
                try{
                    literal = ResourceFactory.createTypedLiteral(new Integer(val));
                }catch (NumberFormatException e){
                    if (key.toLowerCase().endsWith("_dt")) {
                        literal = ResourceFactory.createTypedLiteral(value[0], XSDDatatype.XSDdateTime);
                    }else if (key.toLowerCase().endsWith("_d")){
                        literal = ResourceFactory.createTypedLiteral(value[0], XSDDatatype.XSDdouble);
                    }else if (key.toLowerCase().endsWith("_i")){
                        literal = ResourceFactory.createTypedLiteral(value[0], XSDDatatype.XSDinteger);
                    }else if (key.toLowerCase().endsWith("_s")){
                        literal = ResourceFactory.createTypedLiteral(value[0], XSDDatatype.XSDstring);
                    }else if (key.toLowerCase().endsWith("_b")){
                        literal = ResourceFactory.createTypedLiteral(value[0], XSDDatatype.XSDboolean);
                    }else{
                        literal = ResourceFactory.createPlainLiteral(value[0]);
                    }
                }

                qs.setParam(key,literal);
            }

        }

        int offsetValue = maxSizeParam * offsetParam;


        if (!StringUtils.isEmpty(sortField)){
            // Getting most similar field for order it
            QueryView queryView = new QueryView(qs.toString());
            List<String> fields = queryView.getSelectFields();
            final String refField = sortField;
            Optional<Index> mostSimilarField = fields.stream().map(field -> new Index(field, Double.valueOf(StringUtils.getLevenshteinDistance(refField, field)))).sorted((a, b) -> a.getValue().compareTo(b.getValue())).findFirst();
            if (mostSimilarField.isPresent()) qs.append("\nORDER BY " + sortCriteria+ "(?"+mostSimilarField.get().getText()+")\n");
        }
        qs.append("\nLIMIT " + maxSizeParam + "\n");
        qs.append("\nOFFSET " + offsetValue+ "\n");


        // Replace filtering variables by query parameters
        Query tq = qs.asQuery();

        Map<String, Integer> rvMap = new HashMap<>();
        tq.getResultVars().forEach(v -> rvMap.put(v,1));

        ElementGroup eg = (ElementGroup) tq.getQueryPattern();
        List<Element> elements = eg.getElements();
        Map<String,String> bindingVars = new HashMap<>();
        for (Element el : elements){
            if (el instanceof ElementBind){
                String qel = el.toString();
                // BIND (STRDT ( ?qdate , xsd:dateTime) as ?newdate ) .
                Matcher matcher = pattern.matcher(qel);
                while (matcher.find()) {
                    String qvar = matcher.group(0);
                    String key = StringUtils.substringAfter(qvar,"?");
                    if (!rvMap.containsKey(key) && !qs.getVariableParameters().containsKey(key)){
                        LOG.info("Adding internal binding parameter: " + qvar);
                        bindingVars.put(key,"");
                    }
                }

            }
            if (el instanceof ElementFilter){
                String qel = el.toString();

                // initialize filtering variables
                Matcher matcher = pattern.matcher(qel);
                while (matcher.find()) {
                    String qvar = matcher.group(0);
                    String key = StringUtils.substringAfter(qvar,"?");
                    if (!rvMap.containsKey(key) && !qs.getVariableParameters().containsKey(key) && !bindingVars.containsKey(key)){
                        LOG.info("Filtering Query Parameter: " + qvar);
                        // FILTER ( isNumeric(?param) = True || .. )
                        qs.setParam(qvar,ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDnonNegativeInteger));
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
