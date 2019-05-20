package es.upm.oeg.r4r.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import java.io.File;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Configuration
public class BaseConfig extends WebMvcConfigurationSupport {

    private static final Logger LOG = LoggerFactory.getLogger(BaseConfig.class);

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        File file = new File(resourceFolder);

        if (!file.exists()) return;

        registry
                .addResourceHandler("/doc/**")
                .addResourceLocations("file:"+file.getAbsolutePath()+"/doc/");
        super.addResourceHandlers(registry);

    }

    @Bean
    public ViewResolver viewResolver() {
        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(InternalResourceView.class);
        return viewResolver;
    }

}
