/**
 * View
 *
 * @see org.springframework.web.servlet.View
 */
package org.springframework.web.servlet._mine.web13_model_and_view.view;

/*
View 													[interface]
	\--impl----- AbstractView 							[abstract class]
		\--extends-- AbstractJackson2View 				[abstract class]
			\--extends-- MappingJackson2JsonView 		[class]
			\--extends-- MappingJackson2XmlView 		[class]
		\--extends-- AbstractPdfView 					[abstract class]
		\--extends-- MarshallingView 					[class]
		\--extends-- AbstractUrlBasedView 				[abstract class]
			\--extends-- AbstractPdfStamperView 		[abstract class]
			\--extends-- RedirectView 					[class]
			\--extends-- AbstractTemplateView 			[abstract class]
				\--extends-- GroovyMarkupView 			[class]
				\--extends-- FreeMarkerView 			[class]
			\--extends-- XsltView 						[class]
			\--extends-- InternalResourceView 			[class]
				\--extends-- JstlView 					[class]
			\--extends-- ScriptTemplateView 			[class]
		\--extends-- AbstractXlsView			 		[abstract class]
			\--extends-- AbstractXlsxView 				[abstract class]
				\--extends-- AbstractXlsxStreamingView  [abstract class]
		\--extends-- AbstractFeedView 					[abstract class]
			\--extends-- AbstractAtomFeedView 			[abstract class]
			\--extends-- AbstractRssFeedView 			[abstract class]
	\--extends-- SmartView 								[interface]
		\--impl----- RedirectView 						[class]
**/