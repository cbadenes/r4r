package es.upm.oeg.r4r.controller;

import com.github.jsonldjava.shaded.com.google.common.base.Strings;
import es.upm.oeg.r4r.service.DataService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.config.ResourceNotFoundException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RootController {

    private static final Logger LOG = LoggerFactory.getLogger(RootController.class);

    @Autowired
    DataService dataService;

    @Value("#{environment['SERVER_PATH']?:'${server.path}'}")
    String basePath;

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @GetMapping(value = {"/doc/*"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity doc(HttpServletRequest req) throws IOException {
        String uri = req.getRequestURI();

        LOG.info("Request by URI: " + uri );

        String resource = StringUtils.substringAfter(uri, "/doc/");

        Path path = Paths.get(resourceFolder, "doc", resource);
        File file = path.toFile();

        InputStream inputStream;
        if (!file.exists() || file.isDirectory()){
             inputStream = new ByteArrayInputStream("Hi from R4R!!".getBytes());
        }else{
            inputStream = new FileInputStream(file);
        }
        return ResponseEntity.ok(new InputStreamResource(inputStream));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
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
        }catch (IOException e) {
            LOG.warn(e.getMessage());
            return new ResponseEntity<>("resource-not-found", HttpStatus.NOT_FOUND);
        }catch (QueryExceptionHTTP e) {
            LOG.warn(e.getMessage());
            Throwable cause = e.getCause();
            if (cause instanceof HttpHostConnectException){
                return new ResponseEntity<>("sparql-endpoint-not-available", HttpStatus.GATEWAY_TIMEOUT);
            }else{
                return new ResponseEntity<>("sparql-query-error", HttpStatus.NOT_IMPLEMENTED);
            }
        }catch (Exception e){
            LOG.error(e.getMessage());
            return new ResponseEntity<>("internal-error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
