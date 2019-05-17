package es.upm.oeg.r4r.server;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.stereotype.Component;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class CustomContainer implements EmbeddedServletContainerCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(CustomContainer.class);

    @Value("#{environment['SERVER_PORT']?:${server.port}}")
    Integer port;

    @Value("#{environment['SERVER_PATH']?:'${server.path}'}")
    String prefix;


    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {

        container.setPort(port);
        if (!Strings.isNullOrEmpty(prefix) && !prefix.equals("/")) container.setContextPath(prefix);

        LOG.info("Rest service listening at 0.0.0.0:"+port+prefix);

    }

}