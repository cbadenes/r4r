package es.upm.oeg.r4r.retriever;

import es.upm.oeg.r4r.data.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class QueryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(QueryFactory.class);

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @Value("#{environment['RESOURCE_NAMESPACE']?:'${resource.namespace}'}")
    String resourceNamespace;


    private String resourceBaseUri;

    @PostConstruct
    public void setup(){
        resourceBaseUri = resourceNamespace.endsWith("/") ? resourceNamespace : resourceNamespace + "/";
    }

    public SparqlQuery newQuery(Request.Type reqType, String[] resources) throws IOException {

        String httpVerb = reqType.name().toLowerCase();

        Path queryPath;
        Optional<String> id;
        if (resources.length == 1){
            queryPath   = Paths.get(resourceFolder, resources[0], httpVerb+".sparql");
            id          = Optional.empty();
        }else if (resources.length == 2){
            queryPath   = Paths.get(resourceFolder, resources[0], httpVerb+"ById.sparql");
            id          = Optional.of(resourceBaseUri+resources[1]);
        }else{
            queryPath   = Paths.get(resourceFolder, resources[0], resources[2], httpVerb+".sparql");
            id          = Optional.of(resourceBaseUri+resources[1]);
        }

        if (!queryPath.toFile().exists()) throw new IOException("Query template not found at: " + queryPath);
        return new SparqlQuery(queryPath, id);

    }


}
