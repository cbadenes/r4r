package es.upm.oeg.r4r.parser;

import es.upm.oeg.r4r.data.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class DataParser {

    private static final Logger LOG = LoggerFactory.getLogger(DataParser.class);

    @Autowired
    ParserFactory parserFactory;

    public String retrieve(String[] resources, List<Map<String, String>> mapResults) throws IOException {

        try{
            JsonParser parser = parserFactory.newParser(Request.Type.GET, resources);
            return parser.get(mapResults);
        }catch (IOException e){
            LOG.warn(e.getMessage());
            return "";
        }

    }

}
