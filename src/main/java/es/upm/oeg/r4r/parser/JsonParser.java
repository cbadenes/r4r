package es.upm.oeg.r4r.parser;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class JsonParser {

    private static final Logger LOG = LoggerFactory.getLogger(JsonParser.class);

    private final Template template;
    private final Boolean isList;

    public JsonParser(Template template, Boolean isList) {
        this.template = template;
        this.isList = isList;
    }

    public String get(List<Map<String,String>> mapResults){
        try {

            if (mapResults.isEmpty()) return "{}";

            VelocityContext context = new VelocityContext();


            if (isList){
                context.put("results", mapResults);
            }else{
                mapResults.get(0).entrySet().forEach(entry -> context.put(entry.getKey(), entry.getValue()));
            }

            StringWriter fw = new StringWriter();
            template.merge(context, fw);
            fw.close();
            return fw.toString();
        } catch (Exception e) {
            LOG.warn("Unexpected error parsing json template",e);
            return "{}";
        }

    }
}
