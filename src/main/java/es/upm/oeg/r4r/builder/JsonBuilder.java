package es.upm.oeg.r4r.builder;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class JsonBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(JsonBuilder.class);

    private VelocityEngine velocityEngine;

    @PostConstruct
    public void setup(){
        // Velocity Template
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    // resource list
    public String compose(String resource, List<Map<String,String>> mapResults) throws IOException {
        Template t = velocityEngine.getTemplate(resource + File.separator + "get.vm");

        VelocityContext context = new VelocityContext();

        context.put("resultList", mapResults);

        StringWriter fw = new StringWriter();
        t.merge(context, fw);
        fw.close();

        return fw.toString();
    }

    // resource detail
    public String compose(String resource, Map<String,String> mapResult) throws IOException {
        Template t = velocityEngine.getTemplate(resource + File.separator + "getById.vm");

        VelocityContext context = new VelocityContext();

        mapResult.entrySet().forEach(entry -> context.put(entry.getKey(), entry.getValue()));

        StringWriter fw = new StringWriter();
        t.merge(context, fw);
        fw.close();

        return fw.toString();
    }

    // resource inner list
    public String compose(String baseResource, String innerResource, List<Map<String,String>> mapResults) throws IOException {
        Template t = velocityEngine.getTemplate(baseResource + File.separator + innerResource + "get.vm");

        VelocityContext context = new VelocityContext();

        context.put("resultList", mapResults);

        StringWriter fw = new StringWriter();
        t.merge(context, fw);
        fw.close();

        return fw.toString();
    }

}
