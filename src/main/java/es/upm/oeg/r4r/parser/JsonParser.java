package es.upm.oeg.r4r.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.JsonValueSerializer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private Map<String,String> escape(Map<String,String> raw) {
        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                  entry -> entry.getKey(),
                  entry -> StringEscapeUtils.escapeJson(entry.getValue())
                ));
    }


    public String get(List<Map<String,String>> mapResults){
        try {
            VelocityContext context = new VelocityContext();
            if (isList){
                context.put("results", mapResults.stream().map(map -> escape(map)).collect(Collectors.toList()));
            }else if (!mapResults.isEmpty()){
                for(Map.Entry<String,String> entry : mapResults.get(0).entrySet()){
                    String value    = entry.getValue();
                    String payload  = StringEscapeUtils.escapeJson(value);
                    context.put(entry.getKey(), payload);
                }

            }

            StringWriter fw = new StringWriter();
            template.merge(context, fw);
            fw.close();
            String payload = fw.toString();
            return payload;
        } catch (Exception e) {
            LOG.warn(e.getMessage());
            return "{}";
        }

    }


    public static void main(String[] args) {
        //System.out.println(StringEscapeUtils.escapeJson("http://dbpedia.org/resource/!Women_Art_Revolution"));
        System.out.println(StringEscapeUtils.escapeJson("http://dbpedia.org/resource/!Women_Art_Revolution"));
    }
}
