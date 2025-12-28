package cz.petrf.prani.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/{spring:\\w+}")
        .setViewName("forward:/index.html");
    registry.addViewController("/**/{spring:\\w+}")
        .setViewName("forward:/index.html");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(new PathResourceResolver() {
          @Override
          protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requestedResource = location.createRelative(resourcePath);

            // Pokud je to API požadavek, nechť ho zpracuje controller
            if (resourcePath.startsWith("api/") || resourcePath.startsWith("actuator/")) {
              return null;
            }

            // Pokud soubor existuje, vrať ho
            if (requestedResource.exists() && requestedResource.isReadable()) {
              return requestedResource;
            }

            // Jinak vrať index.html pro Angular routing (SPA)
            return location.createRelative("index.html");
          }
        });
  }

}