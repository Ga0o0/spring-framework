package org.springframework.sample.configuration;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.PropertySourceDescriptor;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.PropertySourceProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * {@code @PropertySource} 之解析器 PropertySourceProcessor
 *
 * @see org.springframework.context.annotation.PropertySource
 * @see org.springframework.core.io.support.PropertySourceProcessor
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.PropertySourceRegistry#processPropertySource(AnnotationAttributes)
 * @see org.springframework.core.io.support.PropertySourceProcessor#processPropertySource(org.springframework.core.io.support.PropertySourceDescriptor)
 */
public class CONF11_AtPropertySource_PropertySourceProcessor {

	public static void main(String[] args) {

		// 1. create PropertySourceDescriptor
		String location = "classpath:property-source.properties";
		String name = "my-property-source";
		List<String> locations = List.of(location);
		boolean ignoreResourceNotFound = true;
		Class<? extends PropertySourceFactory> propertySourceFactory = DefaultPropertySourceFactory.class;
		String encoding = StandardCharsets.UTF_8.name();
		// PropertySourceDescriptor
		PropertySourceDescriptor psdWithLocation = new PropertySourceDescriptor(location);
		PropertySourceDescriptor psd = new PropertySourceDescriptor(locations, ignoreResourceNotFound, name, propertySourceFactory, encoding);

		// 2. create PropertySourceProcessor
		StandardEnvironment environment = new StandardEnvironment();
		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		PropertySourceProcessor propertySourceProcessor = new PropertySourceProcessor(environment, resourceLoader);

		try {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~	name with psdWithLocation	~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			propertySourceProcessor.processPropertySource(psdWithLocation);
			String property = environment.getProperty("name");
			System.out.println(property);

			System.out.println("~~~~~~~~~~~~~~~~~~~~~		name with psd		~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			propertySourceProcessor.processPropertySource(psd);
			String property1 = environment.getProperty("name");
			System.out.println(property1);

			System.out.println("~~~~~~~~~~~~~~~~~~~~~environment.getPropertySources()~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			environment.getPropertySources().forEach(System.out::println);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
