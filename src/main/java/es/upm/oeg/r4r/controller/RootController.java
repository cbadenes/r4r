package es.upm.oeg.r4r.controller;

import es.upm.oeg.r4r.builder.JsonBuilder;
import es.upm.oeg.r4r.data.SparqlClient;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/")
public class RootController {

    private static final Logger LOG = LoggerFactory.getLogger(RootController.class);

    @Autowired
    JsonBuilder jsonBuilder;

    @Autowired
    SparqlClient sparqlClient;

    @PostConstruct
    public void setup(){

    }

    @PreDestroy
    public void destroy(){

    }

    @RequestMapping(value="**",method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> get(HttpServletRequest req){
        LOG.info("Root Controller: " + req);

        Map<String, String[]> parameters = req.getParameterMap();

        String uri = req.getRequestURI();


        String[] resources = StringUtils.split(uri, "/");

        if (resources.length == 0){
            return new ResponseEntity<String>("Welcome to R4R ;)", HttpStatus.ACCEPTED);
        }

        if (resources.length > 3) {
            LOG.warn("Request exceeds max depth allowed (3), it is: '" + uri +"'");
            return new ResponseEntity<String>("bad-request", HttpStatus.BAD_REQUEST);
        }

        try {
            ResultSet results;
            if (resources.length == 1){
                results = sparqlClient.query(resources[0]);
            }else if (resources.length == 2){
                results = sparqlClient.query(resources[0],resources[1]);
            }else{
                results = sparqlClient.query(resources[0],resources[1],resources[2]);
            }


            List<Map<String,String>> mapResults = new ArrayList<>();

            while(results.hasNext()){

                QuerySolution result = results.next();

                Map<String,String> mapResult = new HashMap<>();

                Iterator<String> varNames = result.varNames();
                while(varNames.hasNext()){
                    String varName = varNames.next();
                    mapResult.put(varName, result.get(varName).toString());
                }

                mapResults.add(mapResult);
            }

            if (mapResults.isEmpty()) return new ResponseEntity<String>("{}",HttpStatus.OK);

            String json;
            if (resources.length == 1){
                json = jsonBuilder.compose(resources[0], mapResults);
            }else if (resources.length == 2){
                json = jsonBuilder.compose(resources[0], mapResults.get(0));
            }else{
                json = jsonBuilder.compose(resources[0], resources[2], mapResults);
            }

            return new ResponseEntity<String>(json,HttpStatus.OK);

        } catch (Exception e) {
            LOG.warn("Error",e);
            return new ResponseEntity<String>("redirect:404.html", HttpStatus.NOT_FOUND);
        }
    }
}
