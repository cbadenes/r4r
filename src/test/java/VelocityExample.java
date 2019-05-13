import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class VelocityExample {

    private static final Logger LOG = LoggerFactory.getLogger(VelocityExample.class);


    @Test
    public void json() throws IOException {


        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        Template t = velocityEngine.getTemplate("movies/get.vm");

        VelocityContext context = new VelocityContext();

        List<Map<String,String>> movieList = new ArrayList<>();

        Map<String,String> movie1 = new HashMap<>();
        movie1.put("val1","uri1");
        movie1.put("val2","name1");
        movieList.add(movie1);

        Map<String,String> movie2 = new HashMap<>();
        movie2.put("val1","uri2");
        movie2.put("val2","name2");
        movieList.add(movie2);

        context.put("movieList", movieList);

        StringWriter fw = new StringWriter();
        t.merge(context, fw);
        fw.close();

        LOG.info("" + fw);

    }

}
