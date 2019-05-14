package es.upm.oeg.r4r.builder;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @PostConstruct
    public void setup(){
        // Velocity Template
        velocityEngine = new VelocityEngine();

        // classpath
//        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
//        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");

        String basePath = new File(resourceFolder).getAbsolutePath();
        LOG.info("Json Template Path set to: " + basePath);
        velocityEngine.setProperty("file.resource.loader.path", basePath);
        velocityEngine.setProperty("file.resource.loader.cache", false);
        velocityEngine.setProperty("file.resource.loader.modificationCheckInterval", "2");

        velocityEngine.init();
    }

    // resource list
    public String compose(String resource, List<Map<String,String>> mapResults) throws IOException {
        return get(resource, mapResults);
    }

    // resource detail
    public String compose(String resource, Map<String,String> mapResult) throws IOException {
        return getById(resource, mapResult);
    }

    // inner resource list
    public String compose(String baseResource, String innerResource, List<Map<String,String>> mapResults) throws IOException {
        String resource = baseResource + File.separator + innerResource;
        return get(resource, mapResults);
    }

    // inner resource detail
    public String compose(String baseResource, String innerResource, Map<String,String> mapResult) throws IOException {
        String resource = baseResource + File.separator + innerResource;
        return getById(resource, mapResult);
    }

    private String get(String resource, List<Map<String,String>> mapResults){
        Template t = velocityEngine.getTemplate(resource + File.separator + "get.vm");

        VelocityContext context = new VelocityContext();

        context.put("resultList", mapResults);

        return merge(t, context);
    }

    private String getById(String resource, Map<String,String> mapResult){
        Template t = velocityEngine.getTemplate(resource + File.separator + "getById.vm");

        VelocityContext context = new VelocityContext();

        mapResult.entrySet().forEach(entry -> context.put(entry.getKey(), entry.getValue()));

        return merge(t, context);
    }


    private String merge(Template t, VelocityContext context){
        try {
            StringWriter fw = new StringWriter();
            t.merge(context, fw);
            fw.close();
            return fw.toString();
        } catch (Exception e) {
            LOG.warn("Unexpected error parsing json template",e);
            return "{}";
        }
    }

}
