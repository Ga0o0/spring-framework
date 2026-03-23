/**
 * @jakarta.inject.Inject
 *
 * @see jakarta.inject.Inject
 *
 * @see org.springframework.sample.bean_post_processor.annotation_autowired.autowired
 * @see org.springframework.sample.bean_post_processor.resolve_dependency
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(Object, String, org.springframework.beans.PropertyValues)
 */
package org.springframework.sample.bean_post_processor.annotation_autowired.jakarta_inject;
/*		@Inject
********************************* Class API Docs *********************************
标识可注入的构造函数、方法和字段。可应用于静态成员和实例成员。可注入成员可以具有任何访问修饰符（private、package-private、protected、public）。
* 构造函数首先被注入，然后是字段，最后是方法。超类中的字段和方法优先于子类中的字段和方法被注入。同一类中字段和方法的注入顺序未指定。

可注入的构造函数使用 @Inject 注解，并接受零个或多个依赖项作为参数。每个类最多只能有一个构造函数使用 @Inject 注解。

	@Inject ConstructorModifiers SimpleTypeName(FormalParameterList) Throws ConstructorBody

对于没有其他构造函数的公共无参构造函数，@Inject 注解是可选的。这使得注入器可以调用默认构造函数。

	@Inject Annotations public SimpleTypeName() Throws ConstructorBody

可注入的字段：
	* 使用 @Inject 注解。
	* 不是 final 的。
	* 可以具有任何其他有效的名称。

	@Inject FieldModifiers Type VariableDeclarators;

可注入方法：
	* 使用 @Inject 注解。
	* 不是抽象方法。
	* 不声明自己的类型参数。
	* 可以返回结果。
	* 可以具有任何其他有效的名称。
	* 接受零个或多个依赖项作为参数。

	@Inject MethodModifiers ResultType Identifier(FormalParameterList) Throws MethodBody

注入器会忽略注入方法的结果，但允许非 void 返回类型，以便支持在其他上下文中使用该方法（例如，构建器风格的方法链）。

示例：

	public class Car {
		// 可注入构造函数
		@Inject public Car(Engine engine) { ... }

		// 可注入字段
		@Inject private Provider<Seat> seatProvider;

		// 可注入的包私有方法
		@Inject void install(Windshield windshield, Trunk trunk) { ... }
	}

使用 @Inject 注解的方法如果覆盖了另一个使用 @Inject 注解的方法，则每个实例每次注入请求只会注入一次。
没有 @Inject 注解的方法如果覆盖了使用 @Inject 注解的方法，则不会被注入。

使用 @Inject 注解的成员必须注入。虽然可注入的成员可以使用任何访问修饰符（包括 private），
但平台或注入器的限制（例如安全限制或缺乏反射支持）可能会阻止非公共成员的注入。

## 限定符

限定符可以注解可注入的字段或参数，并与类型结合使用，以标识要注入的实现。限定符是可选的，并且在与注入器无关的类中使用 @Inject 时，
单个字段或参数只能使用一个限定符。以下示例中，限定符以粗体显示：

	public class Car {
		@Inject private @Leather Provider<Seat> seatProvider;
		@Inject void install(@Tinted Windshield windshield,
		@Big Trunk trunk) { ... }
	}

如果一个可注入方法覆盖了另一个方法，则覆盖方法的参数不会自动继承被覆盖方法参数的限定符。

## 可注入值

对于给定的类型 T 和可选限定符，注入器必须能够注入一个用户指定的类，该类：
	a. 与 T 赋值兼容，并且
	b. 具有可注入的构造函数。

例如，用户可以使用外部配置来选择 T 的一个实现。除此之外，注入哪些值取决于注入器的实现及其配置。

## 循环依赖

检测和解决循环依赖是注入器实现的职责。两个构造函数之间的循环依赖是一个显而易见的问题，但可注入字段或方法之间也可能存在循环依赖：

	class A {
		@Inject B b;
	}
	class B {
		@Inject A a;
	}

在构造 A 的实例时，一个简单的注入器实现可能会陷入无限循环：构造一个 B 的实例并将其赋值给 A，
再构造一个 A 的实例并将其赋值给 B，再构造一个 B 的实例并将其赋值给第二个 A 的实例，以此类推。

一个更保守的注入器可能会在构建时检测到循环依赖并生成错误，此时程序员可以通过分别注入 Provider<A>
或 Provider<B> 来打破循环依赖，而不是注入 A 或 B。直接从构造函数或注入方法调用 Provider#get() 会破坏 Provider 打破循环依赖的能力。
对于方法或字段注入，限制其中一个依赖项的作用域（例如使用单例作用域）也可能导致有效的循环依赖关系。


********************************* Class Definition *********************************
@Target({ METHOD, CONSTRUCTOR, FIELD })
@Retention(RUNTIME)
@Documented
public @interface Inject {}
**/