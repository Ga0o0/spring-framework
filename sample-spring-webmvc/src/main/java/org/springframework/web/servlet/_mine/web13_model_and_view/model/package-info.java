/**
 * Model
 *
 * @see org.springframework.ui.Model
 * @see org.springframework.ui.ModelMap
 */
package org.springframework.web.servlet._mine.web13_model_and_view.model;
/*
Model 												[interface]
	\--extends-- RedirectAttributes 				[interface]
		\--impl----- RedirectAttributesModelMap 	[class]
	\--impl----- ConcurrentModel		 			[class]
		\--extends--BindingAwareConcurrentModel 	[class]
	\--impl----- ExtendedModelMap 					[class]
		\--extends-- BindingAwareModelMap 			[class]
**/