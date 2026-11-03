package com.filesys.config;

import com.filesys.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 资料上传下载系统 —— SpringMVC 配置（由 web.xml 中名为 filesys 的 DispatcherServlet 加载，映射 /filesys/*）
 *
 *   数据层（数据源、MyBatis、事务）：spring-datasource.xml，通过 @ImportResource 引入
 *   表现层：本类（视图解析、静态资源、拦截器）
 *   业务层 / 切面：@ComponentScan 扫描
 */
@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan({"com.filesys.controller", "com.filesys.service", "com.filesys.interceptor", "com.filesys.aspect"})
@ImportResource("classpath:filesys/spring-datasource.xml")
public class FileSysMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public FileSysMvcConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    /** 文件上传解析器：Bean 名必须是 multipartResolver；大小硬上限见 web.xml 的 multipart-config */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /** 逻辑视图名 files → /WEB-INF/views/filesys/files.jsp */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/views/filesys/", ".jsp");
    }

    /** /filesys/static/** → webapp/static/filesys/（CSS、JS、图片） */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/filesys/");
    }

    /**
     * 拦截器注册（任务书要求：WebMvcConfigurer#addInterceptors，不用 XML）
     * 路径相对于 Servlet 映射 /filesys：
     *   放行：/login（登录页 GET 与登录接口 POST）、/static/**（CSS / JS / 图片）
     *   其余全部要求登录
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/static/**");
    }
}
