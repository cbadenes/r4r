package es.upm.oeg.r4r.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Request {

    private static final Logger LOG = LoggerFactory.getLogger(Request.class);

    public enum Type {
        GET, POST, PUT, DELETE;
    }

}
