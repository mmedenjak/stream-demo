package com.ingemark.stream.config.init;

import com.ingemark.stream.config.WebMvcConfig;
import com.ingemark.stream.util.Util;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.*;

public class WebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{WebMvcConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    public void onStartup(ServletContext ctx) throws ServletException {
        ctx.setInitParameter("spring.profiles.active", "production");
        ctx.addListener(new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent sce) {
                Util.logger.info("\n\n******************* Servlet Context initialized **************************\n");
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
                System.err.println("\n\n******************* Servlet Context destroyed **************************\n");
            }
        });
        super.onStartup(ctx);
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(new MultipartConfigElement("/tmp"));
        registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");
    }
}