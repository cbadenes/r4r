package es.upm.oeg.r4r.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
*/
@Component
public class CustomWebServer implements EmbeddedServletContainerCustomizer{

    private static final Logger LOG = LoggerFactory.getLogger(CustomWebServer.class);

    @Value("#{environment['SERVER_THREADS']}")
    Integer maxThreads;

    Integer availableThreads = Runtime.getRuntime().availableProcessors();

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        if (container instanceof TomcatEmbeddedServletContainerFactory) {
            TomcatEmbeddedServletContainerFactory tomcatContainer = (TomcatEmbeddedServletContainerFactory) container;

            tomcatContainer.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
                Object defaultMaxThreads = connector.getAttribute("maxThreads");
                Integer newThreads = maxThreads != null? maxThreads : availableThreads;
                connector.setAttribute("maxThreads",newThreads);
                LOG.info("Changed Tomcat connector maxThreads from " + defaultMaxThreads + " to  " + newThreads);
            });



        }
    }
}
