package com.ingemark.stream.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ingemark.stream.controller.ConfigController;
import com.ingemark.stream.stream.JacksonStreamSerializer;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.hibernate5.support.OpenSessionInViewInterceptor;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;

import static com.fasterxml.jackson.core.Version.unknownVersion;
import static java.util.Collections.singletonList;

@Configuration
@ComponentScan(basePackageClasses = {ConfigController.class})
@Import({DataConfig.class})
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Autowired
    private OpenSessionInViewInterceptor openSessionInViewInterceptor;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(mapper()));
        addDefaultHttpMessageConverters(converters);
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
        registry.addWebRequestInterceptor(openSessionInViewInterceptor);
    }

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        // TODO switch to commons multipart
        return new StandardServletMultipartResolver();
    }

    @Autowired
    @Bean
    public OpenSessionInViewInterceptor openSessionInViewInterceptor(SessionFactory sf) {
        final OpenSessionInViewInterceptor tor = new OpenSessionInViewInterceptor();
        tor.setSessionFactory(sf);
        return tor;
    }

    @Bean
    public ObjectMapper mapper() {
        final ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        om.registerModule(new SimpleModule("Streams API", unknownVersion(), singletonList(new JacksonStreamSerializer())));
        om.registerModule(new JavaTimeModule());
        return om;
    }
}
