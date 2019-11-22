package es.upm.oeg.r4r.service;

import es.upm.oeg.r4r.retriever.DataRetriever;
import es.upm.oeg.r4r.parser.DataParser;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class DataService {

    private static final Logger LOG = LoggerFactory.getLogger(DataService.class);

    @Autowired
    DataRetriever dataRetriever;

    @Autowired
    DataParser dataParser;

    public String get(String[] resources, Map<String, String[]> parameters) throws IOException {

        Optional<ResultSet> results = dataRetriever.retrieve(resources, parameters);

        List<Map<String,String>> mapResults = new ArrayList<>();

        int rows = 0;
        if (results.isPresent()){

            ResultSet iterator = results.get();
            while(iterator.hasNext()){

                QuerySolution result = iterator.next();

                Map<String,String> mapResult = new HashMap<>();

                Iterator<String> varNames = result.varNames();
                while(varNames.hasNext()){
                    String varName = varNames.next();
                    RDFNode node = result.get(varName);
                    String value = node.isResource()? node.asResource().getURI() : node.asLiteral().getString();
                    //mapResult.put(varName, value.replace("\n","").replace("\"",""));
                    mapResult.put(varName, value.replaceAll("\\P{Print}", ""));
                }
                rows++;
                mapResults.add(mapResult);
            }

        }

        LOG.info("<-: rows=" + rows);
        String json = dataParser.retrieve(resources, mapResults);
        return json;

    }

}
