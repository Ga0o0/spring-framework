/**
 * @EnableWebMvc
 *
 * @see org.springframework.web.servlet.config.annotation.EnableWebMvc
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
package org.springframework.web.servlet._mine.servlet.servlet10_atenablewebmvc;


/**
 * @see org.springframework.web.servlet.config.annotation.EnableWebMvc
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration
 * @see  org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
 *
 * ## 1. WebMvcConfigurationSupport 中 @Bean
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerMapping(org.springframework.web.accept.ContentNegotiationManager, org.springframework.format.support.FormattingConversionService, org.springframework.web.servlet.resource.ResourceUrlProvider)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcPatternParser()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcUrlPathHelper()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcPathMatcher()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcContentNegotiationManager()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#viewControllerHandlerMapping(org.springframework.format.support.FormattingConversionService, org.springframework.web.servlet.resource.ResourceUrlProvider)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#beanNameHandlerMapping(org.springframework.format.support.FormattingConversionService, org.springframework.web.servlet.resource.ResourceUrlProvider)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#routerFunctionMapping(org.springframework.format.support.FormattingConversionService, org.springframework.web.servlet.resource.ResourceUrlProvider)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#resourceHandlerMapping(org.springframework.web.accept.ContentNegotiationManager, org.springframework.format.support.FormattingConversionService, org.springframework.web.servlet.resource.ResourceUrlProvider)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcResourceUrlProvider()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#defaultServletHandlerMapping()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerAdapter(org.springframework.web.accept.ContentNegotiationManager, org.springframework.format.support.FormattingConversionService, org.springframework.validation.Validator)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerFunctionAdapter()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcConversionService()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcValidator()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcUriComponentsContributor(org.springframework.format.support.FormattingConversionService, org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#httpRequestHandlerAdapter()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#simpleControllerHandlerAdapter()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver(org.springframework.web.accept.ContentNegotiationManager)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcViewResolver(org.springframework.web.accept.ContentNegotiationManager)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#mvcHandlerMappingIntrospector()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#localeResolver()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#themeResolver()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#flashMapManager()
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#viewNameTranslator()
 */

/**
 * @see org.springframework.web.servlet.config.annotation.EnableWebMvc
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
 *
 * ## 1. WebMvcConfigurer 通过 @Autowired 注入到 DelegatingWebMvcConfiguration#configurers
 *
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration#configurers
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration#setConfigurers(java.util.List)
 */

/**
 * WebMvcConfigurer 中的配置如何加载到应用中 - 示例 -> 加载 WebMvcConfigurer 实例中的 HandlerExceptionResolver
 *
 * ## 1. WebMvcConfigurationSupport 中 @Bean - WebMvcConfigurer 加载 HandlerExceptionResolver  -> WebMvcConfigurationSupport#handlerExceptionResolver(...)
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver(org.springframework.web.accept.ContentNegotiationManager)
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration#configureHandlerExceptionResolvers(java.util.List)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerComposite#configureHandlerExceptionResolvers(java.util.List)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#configureHandlerExceptionResolvers(java.util.List)
 */