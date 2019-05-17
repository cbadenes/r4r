package es.upm.oeg.r4r.controller;

import com.github.jsonldjava.shaded.com.google.common.base.Strings;
import es.upm.oeg.r4r.service.DataService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RootController {

    private static final Logger LOG = LoggerFactory.getLogger(RootController.class);

    @Autowired
    DataService dataService;

    @Value("#{environment['SERVER_PATH']?:'${server.path}'}")
    String basePath;

    @RequestMapping(value="**",method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> get(HttpServletRequest req){
        LOG.debug("Root Controller: " + req);

        Map<String, String[]> parameters = req.getParameterMap();

        String uri = req.getRequestURI();

        LOG.info("Request by URI: " + uri + " and parameters: " + (!parameters.isEmpty()? parameters.keySet() : "[]"));

        String[] resources = StringUtils.split(StringUtils.substringAfter(uri, basePath), "/");

        if (resources.length == 0){
            return new ResponseEntity<>("Welcome to R4R ;)", HttpStatus.ACCEPTED);
        }

        if (resources.length > 3) {
            LOG.warn("Request exceeds max depth allowed (3), it is: '" + uri +"'");
            return new ResponseEntity<>("bad-request", HttpStatus.BAD_REQUEST);
        }

        try{
            String response = dataService.get(resources, parameters);
            if (Strings.isNullOrEmpty(response)) return new ResponseEntity<>("resource-not-found", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (IOException e){
            return new ResponseEntity<>("resource-not-found", HttpStatus.NOT_FOUND);
        }catch (Exception e){
            LOG.error("Unexpected error", e);
            return new ResponseEntity<>("internal-error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
