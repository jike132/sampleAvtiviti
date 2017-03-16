package com.hkust.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConf extends WebMvcConfigurerAdapter {
	
	@Autowired
    private UserSecurityInterceptor securityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	//Configure the login interceptor to intercept the path
        registry.addInterceptor(securityInterceptor)
        		.addPathPatterns("/mvc/**")
        		.excludePathPatterns("/admin/**");

    }
    

    @Override
    public void addViewControllers( ViewControllerRegistry registry ) {
        registry.addViewController( "/" ).setViewName( "forward:/mvc/list" );
        registry.setOrder( Ordered.HIGHEST_PRECEDENCE );
        super.addViewControllers( registry );
    } 
    
//    @Bean(name="simpleMappingExceptionResolver")
//    public SimpleMappingExceptionResolver
//                    createSimpleMappingExceptionResolver() {
//      SimpleMappingExceptionResolver r =
//                  new SimpleMappingExceptionResolver();
//
//      Properties mappings = new Properties();
//      //mappings.setProperty("DatabaseException", "databaseError");
//      //mappings.setProperty("InvalidCreditCardException", "creditCardError");
//
//      r.setExceptionMappings(mappings);  // None by default
//      r.setDefaultErrorView("error");    // No default
//      r.setExceptionAttribute("ex");     // Default is "exception"
//      r.setWarnLogCategory("example.MvcLogger");     // No default
//      return r;
//    }
}
