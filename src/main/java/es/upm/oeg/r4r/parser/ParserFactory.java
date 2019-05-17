package es.upm.oeg.r4r.parser;

import es.upm.oeg.r4r.controller.Request;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class ParserFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ParserFactory.class);

    private VelocityEngine velocityEngine;

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    String fileExtension = ".vm";

    @PostConstruct
    public void setup(){
        // Velocity Template
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");

        String basePath = new File(resourceFolder).getAbsolutePath();
        LOG.info("Json Template Path set to: " + basePath);
        velocityEngine.setProperty("file.resource.loader.path", basePath);
        velocityEngine.setProperty("file.resource.loader.cache", false);
        velocityEngine.setProperty("file.resource.loader.modificationCheckInterval", "2");

        velocityEngine.init();
    }



    public JsonParser newParser(Request.Type reqType, String[]resources) throws IOException {

        String httpVerb = reqType.name().toLowerCase();

        String resourcePath;
        Boolean isList = true;
        if (resources.length == 1){
            resourcePath = resources[0] + File.separator + httpVerb + ".json" + fileExtension;
        }else if (resources.length == 2){
            resourcePath = resources[0] + File.separator + httpVerb + "ById.json" + fileExtension;
            isList = false;
        }else{
            resourcePath = resources[0] + File.separator + resources[2] + File.separator + httpVerb +".json" + fileExtension;
        }

        Path templatePath = Paths.get(resourceFolder, resourcePath);
        if (!templatePath.toFile().exists()) throw new IOException("Json template not found at: " + templatePath);

        Template template = velocityEngine.getTemplate(resourcePath);
        return new JsonParser(template,isList);
    }


}

