# ClassPathXmlApplicationContext

```java
ClassPathXmlApplicationContext ac =
				new ClassPathXmlApplicationContext("${spring.config:spring}.xml");
```

## 1. ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(java.lang.String)

```text
ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(java.lang.String)
|-- ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(java.lang.String[], boolean, org.springframework.context.ApplicationContext)
```

```java
// 使用给定的父级创建一个新的 ClassPathXmlApplicationContext，并从给定的 XML 文件中加载定义。
public ClassPathXmlApplicationContext(
        String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
        throws BeansException {
    // 设置 ApplicationContext 的 parent ApplicationContext ，并且合并其 environment 到当前 ApplicationContext
    super(parent);
    // 解析 configLocations 中的占位符，并将其值付给 AbstractRefreshableConfigApplicationContext.configLocations
    setConfigLocations(configLocations);
    if (refresh) {
        refresh();
    }
}
```

### 1.1. super(parent)

```text
AbstractXmlApplicationContext#AbstractXmlApplicationContext(org.springframework.context.ApplicationContext)
|-- AbstractRefreshableConfigApplicationContext#AbstractRefreshableConfigApplicationContext(org.springframework.context.ApplicationContext)
    |-- AbstractRefreshableApplicationContext#AbstractRefreshableApplicationContext(org.springframework.context.ApplicationContext)
        |-- AbstractApplicationContext#AbstractApplicationContext(org.springframework.context.ApplicationContext)
```

`AbstractApplicationContext#AbstractApplicationContext(org.springframework.context.ApplicationContext)`：

```java
// 使用给定的父上下文创建一个新的 AbstractApplicationContext。
public AbstractApplicationContext(@Nullable ApplicationContext parent) {
    this();
    // 1. 设置当前 application context 的 parent 属性（父级）
    // 2. parent 的 environment 不为空并且为 ConfigurableEnvironment 实例时，要和当前 application context 的 environment 合并。
    setParent(parent);
}
```

#### 1.1.1. this()

```java
// 1. this()
// AbstractApplicationContext#AbstractApplicationContext()
// 创建一个没有父级的新 AbstractApplicationContext。
public AbstractApplicationContext() {
    // 返回用于将位置模式解析为资源实例的 ResourcePatternResolver。
    this.resourcePatternResolver = getResourcePatternResolver();
}

// 2. this.resourcePatternResolver = getResourcePatternResolver();
// org.springframework.context.support.AbstractApplicationContext#getResourcePatternResolver()
// 返回用于将位置模式解析为资源实例的 ResourcePatternResolver。
protected ResourcePatternResolver getResourcePatternResolver() {
    return new PathMatchingResourcePatternResolver(this);
}
```

#### 1.1.2. setParent(parent)

```java
// 1. setParent(parent);
// AbstractApplicationContext#setParent()

// 设置此应用程序上下文的父级。
// <p>如果父级非 {@code null} 且其环境是 {@link ConfigurableEnvironment} 的实例，
// 则父级 {@linkplain ApplicationContext#getEnvironment() 环境}
// 将与此（子）应用程序上下文环境 {@linkplain ConfigurableEnvironment#merge(ConfigurableEnvironment) 合并}。
// @see ConfigurableEnvironment#merge(ConfigurableEnvironment)
@Override
public void setParent(@Nullable ApplicationContext parent) {
    this.parent = parent;
    if (parent != null) {
        Environment parentEnvironment = parent.getEnvironment();
        if (parentEnvironment instanceof ConfigurableEnvironment configurableEnvironment) {
            // getEnvironment() 默认会调用 org.springframework.core.env.StandardEnvironment#customizePropertySources
            // 并将 操作系统 和 JVM 的环境变量加载进来
            getEnvironment().merge(configurableEnvironment);
            // merge 的内容： propertySources / activeProfiles / defaultProfiles
        }
    }
}

// 2. getEnvironment()
// AbstractApplicationContext#getEnvironment()

// 以可配置的形式返回此应用程序上下文的 {@code Environment}，以允许进一步自定义。
// <p>如果未指定，则将通过 {@link #createEnvironment()} 初始化默认环境。
@Override
public ConfigurableEnvironment getEnvironment() {
    if (this.environment == null) {
        this.environment = createEnvironment();
    }
    return this.environment;
}

// 3. createEnvironment()
// AbstractApplicationContext#createEnvironment()

// 创建并返回一个新的 {@link StandardEnvironment}。
// <p>子类可以重写此方法，以提供自定义的 {@link ConfigurableEnvironment} 实现。
protected ConfigurableEnvironment createEnvironment() {
    // 默认会调用 org.springframework.core.env.StandardEnvironment#customizePropertySources
    // 并将 操作系统 和 JVM 的环境变量加载进来
    return new StandardEnvironment();
}
```

### 1.2. setConfigLocations(configLocations)

```java
// 1. setConfigLocations(configLocations)
// AbstractRefreshableConfigApplicationContext#setConfigLocations()

// 设置此应用上下文的配置位置。
// <p>如果未设置，实现可能会根据需要使用默认值。
public void setConfigLocations(@Nullable String... locations) {
    if (locations != null) {
        Assert.noNullElements(locations, "Config locations must not be null");
        this.configLocations = new String[locations.length];
        for (int i = 0; i < locations.length; i++) {
            this.configLocations[i] = resolvePath(locations[i]).trim();
        }
    }
    else {
        this.configLocations = null;
    }
}


// 2. resolvePath(locations[i])
// AbstractRefreshableConfigApplicationContext#resolvePath()

// 解析给定路径，如有必要，用相应的环境属性值替换占位符。应用于配置位置。
// @param path 原始文件路径
// @return 解析后的文件路径
protected String resolvePath(String path) {
    // config 路径解析：${spring.config:spring}.xml -> spring.xml
    // org.springframework.core.env.AbstractPropertyResolver#createPlaceholderHelper
    // 默认解析器：PropertyPlaceholderHelper#PropertyPlaceholderHelper(String, String, String, boolean)
    return getEnvironment().resolveRequiredPlaceholders(path);
}
```


## 2. refresh()

```java
// org.springframework.context.support.AbstractApplicationContext.refresh()
@Override
public void refresh() throws BeansException, IllegalStateException {
    this.startupShutdownLock.lock();
    try {
        this.startupShutdownThread = Thread.currentThread();

        StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

        // Prepare this context for refreshing. --> 译文：准备刷新此上下文
        prepareRefresh();

        // Tell the subclass to refresh the internal bean factory. --> 译文：告诉子类刷新内部 bean 工厂。
        // 创建 beanFactory 并加载 BeanDefinition 到 beanFactory 中
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

        // Prepare the bean factory for use in this context. --> 译文：准备 bean 工厂以供在此上下文中使用。
        // 配置工厂的标准上下文特性，例如上下文的 ClassLoader 和后置处理器。
        prepareBeanFactory(beanFactory);

        try {
            // Allows post-processing of the bean factory in context subclasses. --> 译文：允许在上下文子类中对 bean 工厂进行后处理。
            postProcessBeanFactory(beanFactory); // 未作处理

            StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
            // Invoke factory processors registered as beans in the context. --> 译文：调用在上下文中注册为 bean 的工厂处理器。
            invokeBeanFactoryPostProcessors(beanFactory); // 实例化并调用所有已注册的 BeanFactoryPostProcessor bean，如果给定，则遵循显式顺序。必须在单例实例化之前调用。
            // Register bean processors that intercept bean creation. --> 译文：注册用于拦截 bean 创建的 bean 处理器。
            registerBeanPostProcessors(beanFactory); // 实例化并注册所有 BeanPostProcessor bean，如果指定则遵循显式顺序。
            beanPostProcess.end();

            // Initialize message source for this context. --> 译文：初始化此上下文的消息源。
            initMessageSource(); // 初始化 MessageSource。如果此上下文中未定义，则使用父级的 MessageSource。

            // Initialize event multicaster for this context. --> 译文：初始化此上下文的事件多播器。
            initApplicationEventMulticaster(); // 初始化 ApplicationEventMulticaster。 如果上下文中未定义，则使用 SimpleApplicationEventMulticaster。

            // Initialize other special beans in specific context subclasses. --> 译文：初始化特定上下文子类中的其他特殊 bean。
            onRefresh();	// 可重写模板方法，以添加特定于上下文的刷新功能。在初始化特殊 bean 时调用，在单例实例化之前。此实现为空。

            // Check for listener beans and register them. --> 译文：检查监听器 bean 并注册它们。
            registerListeners();	// 添加实现了 ApplicationListener 接口的 Bean 作为监听器。

            // Instantiate all remaining (non-lazy-init) singletons. --> 译文：实例化所有剩余的（非延迟初始化）单例。
            finishBeanFactoryInitialization(beanFactory);

            // Last step: publish corresponding event. --> 译文：最后一步：发布相应的事件。
            // 完成此上下文的刷新，调用 LifecycleProcessor 的 onRefresh() 方法并发布 {@link org.springframework.context.event.ContextRefreshedEvent}。
            finishRefresh();
        }

        catch (RuntimeException | Error ex ) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception encountered during context initialization - " +
                        "cancelling refresh attempt: " + ex);
            }

            // Destroy already created singletons to avoid dangling resources. --> 译文：销毁已创建的单例以避免资源悬空。
            destroyBeans();

            // Reset 'active' flag. --> 译文：重置“active”标志。
            cancelRefresh(ex);

            // Propagate exception to caller. --> 译文：将异常传递给调用者。
            throw ex;
        }

        finally {
            contextRefresh.end();
        }
    }
    finally {
        this.startupShutdownThread = null;
        this.startupShutdownLock.unlock();
    }
}
```

### prepareRefresh()

```java
// 准备此上下文以进行刷新、设置其启动日期和活动标志以及执行任何属性源的初始化。
protected void prepareRefresh() {
    // Switch to active. --> 译文：切换至活动状态。
    this.startupDate = System.currentTimeMillis();
    this.closed.set(false);
    this.active.set(true);

    if (logger.isDebugEnabled()) {
        if (logger.isTraceEnabled()) {
            logger.trace("Refreshing " + this);
        }
        else {
            logger.debug("Refreshing " + getDisplayName());
        }
    }

    // Initialize any placeholder property sources in the context environment. --> 译文：初始化上下文环境中的任何占位符属性源。
    initPropertySources();

    // Validate that all properties marked as required are resolvable:
    // see ConfigurablePropertyResolver#setRequiredProperties
    // --> 译文：验证所有标记为必需的属性是否可解析：参见 ConfigurablePropertyResolver#setRequiredProperties
    getEnvironment().validateRequiredProperties();

    // Store pre-refresh ApplicationListeners... --> 译文：存储刷新前的 ApplicationListeners...
    if (this.earlyApplicationListeners == null) {
        this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
    }
    else {
        // Reset local application listeners to pre-refresh state. --> 译文：将本地应用程序监听器重置为刷新前状态
        this.applicationListeners.clear();
        this.applicationListeners.addAll(this.earlyApplicationListeners);
    }

    // Allow for the collection of early ApplicationEvents,
    // to be published once the multicaster is available...
    // --> 译文：允许收集早期的 ApplicationEvents，并在多播器可用时发布...
    this.earlyApplicationEvents = new LinkedHashSet<>();
}
```

### 【重要】ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory()

```java
// 1. obtainFreshBeanFactory()
// AbstractApplicationContext#obtainFreshBeanFactory()

// 告诉子类刷新内部 bean 工厂。
// @return 新的 BeanFactory 实例
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    refreshBeanFactory();
    return getBeanFactory();
}
```

#### refreshBeanFactory()

```java
// AbstractRefreshableApplicationContext#refreshBeanFactory()
// 此实现对此上下文的底层 bean 工厂执行实际刷新，关闭前一个 bean 工厂（如果有）并为上下文生命周期的下一阶段初始化一个新的 bean 工厂。
@Override
protected final void refreshBeanFactory() throws BeansException {
    if (hasBeanFactory()) { // 还没有 beanFactory，故不走这里
        destroyBeans();
        closeBeanFactory();
    }
    try {
        // 为该上下文创建一个内部 Bean 工厂。
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        beanFactory.setSerializationId(getId());
        beanFactory.setApplicationStartup(getApplicationStartup());
        // 自定义此上下文使用的内部 bean 工厂。
        customizeBeanFactory(beanFactory);
        // 将 bean 定义加载到指定的 bean 工厂中，通常通过委托给一个或多个 bean 定义读取器来实现。
        loadBeanDefinitions(beanFactory);
        this.beanFactory = beanFactory;
    }
    catch (IOException ex) {
        throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
    }
}
```

##### DefaultListableBeanFactory beanFactory = createBeanFactory()

```java
// AbstractRefreshableApplicationContext#createBeanFactory()

// 为该上下文创建一个内部 Bean 工厂。每次尝试 {@link #refresh()} 时都会调用。
protected DefaultListableBeanFactory createBeanFactory() {
    return new DefaultListableBeanFactory(getInternalParentBeanFactory());
}
```

##### customizeBeanFactory(beanFactory)

```java
// AbstractRefreshableApplicationContext#customizeBeanFactory()

// 自定义此上下文使用的内部 bean 工厂。每次尝试 {@link #refresh()} 时都会调用。
protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
    if (this.allowBeanDefinitionOverriding != null) {
        // 允许 BeanDefinition 重写
        beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
    }
    if (this.allowCircularReferences != null) {
        // 允许循环引用
        beanFactory.setAllowCircularReferences(this.allowCircularReferences);
    }
}
```

##### loadBeanDefinitions(beanFactory)

```java
// 1. loadBeanDefinitions(beanFactory)
// AbstractXmlApplicationContext#loadBeanDefinitions(DefaultListableBeanFactory)

// 通过 XmlBeanDefinitionReader 加载 bean 定义。
@Override
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
    // Create a new XmlBeanDefinitionReader for the given BeanFactory. --> 译文：为给定的 BeanFactory 创建一个新的 XmlBeanDefinitionReader。
    XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

    // Configure the bean definition reader with this context's
    // resource loading environment. --> 译文：使用此上下文的资源加载环境配置 Bean 定义读取器。
    // 设置读取 Bean 定义时要使用的环境。
    beanDefinitionReader.setEnvironment(getEnvironment());
    // 设置要用于资源位置的 ResourceLoader。
    beanDefinitionReader.setResourceLoader(this);
    // 设置用于分析的 SAX 实体解析程序。
    beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

    // Allow a subclass to provide custom initialization of the reader,
    // then proceed with actually loading the bean definitions. --> 译文：允许子类提供读取器的自定义初始化，然后继续实际加载 Bean 定义。
    // 初始化用于加载此上下文的 bean 定义的 bean 定义读取器。默认实现会设置验证标志。
    initBeanDefinitionReader(beanDefinitionReader);
    // 使用给定的 XmlBeanDefinitionReader 加载 Bean 定义。
    loadBeanDefinitions(beanDefinitionReader);
}

// 2. loadBeanDefinitions(beanDefinitionReader)
// AbstractXmlApplicationContext#loadBeanDefinitions(XmlBeanDefinitionReader)

// 使用给定的 XmlBeanDefinitionReader 加载 Bean 定义。
protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
    // 返回一个 Resource 对象数组，指向构建此上下文所需的 XML Bean 定义文件。
    Resource[] configResources = getConfigResources();
    if (configResources != null) {
        reader.loadBeanDefinitions(configResources);
    }
    // 返回一个资源位置数组，指向构建此上下文所需的 XML bean 定义文件。
    String[] configLocations = getConfigLocations();
    if (configLocations != null) {
        // 从指定的资源位置加载 bean 定义。
        reader.loadBeanDefinitions(configLocations);
    }
}

// 3. reader.loadBeanDefinitions(configLocations)
// org.springframework.beans.factory.support.AbstractBeanDefinitionReader.loadBeanDefinitions(java.lang.String...)
@Override
public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
    Assert.notNull(locations, "Location array must not be null");
    int count = 0;
    for (String location : locations) {
        count += loadBeanDefinitions(location);
    }
    return count;
}
```

```text
AbstractBeanDefinitionReader.loadBeanDefinitions(java.lang.String...)
|-- AbstractBeanDefinitionReader.loadBeanDefinitions(java.lang.String, java.util.Set<org.springframework.core.io.Resource>)
    |-- AbstractBeanDefinitionReader.loadBeanDefinitions(org.springframework.core.io.Resource...)
        |-- XmlBeanDefinitionReader.loadBeanDefinitions(org.springframework.core.io.Resource)
            |-- XmlBeanDefinitionReader.doLoadBeanDefinitions(...)
```

```java
// 1. XmlBeanDefinitionReader.doLoadBeanDefinitions(...)

// 从指定的 XML 文件实际加载 bean 定义。
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
        throws BeanDefinitionStoreException {

    try {
        // 使用已配置的 DocumentLoader 实际加载指定文档。
        Document doc = doLoadDocument(inputSource, resource);
        // 注册给定 DOM 文档中包含的 Bean 定义。
        int count = registerBeanDefinitions(doc, resource);
        if (logger.isDebugEnabled()) {
            logger.debug("Loaded " + count + " bean definitions from " + resource);
        }
        return count;
    } catch (BeanDefinitionStoreException ex) {
        throw ex;
    }
    // ...
}

// 2. int count = registerBeanDefinitions(doc, resource)
// org.springframework.beans.factory.xml.XmlBeanDefinitionReader.registerBeanDefinitions()

// 注册给定 DOM 文档中包含的 Bean 定义。由 {@code loadBeanDefinitions} 调用。
// <p>创建解析器类的新实例，并对其调用 {@code registerBeanDefinitions}。
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
    // 创建 {@link BeanDefinitionDocumentReader} 用于从 XML 文档中实际读取 Bean 定义。
    BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
    // 获取 BeanDefinitionRegistry，并返回其定义的 bean 数量。
    int countBefore = getRegistry().getBeanDefinitionCount();
    // 从给定的 DOM 文档中读取 bean 定义，并在给定的读取器上下文中将其注册到注册表。
    documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
    // 返回本次加载的 bean 定义数
    return getRegistry().getBeanDefinitionCount() - countBefore;
}

// 3.1 BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader()
// org.springframework.beans.factory.xml.XmlBeanDefinitionReader.createBeanDefinitionDocumentReader()

private Class<? extends BeanDefinitionDocumentReader> documentReaderClass =
        DefaultBeanDefinitionDocumentReader.class;
// 创建 {@link BeanDefinitionDocumentReader} 用于从 XML 文档中实际读取 Bean 定义。
protected BeanDefinitionDocumentReader createBeanDefinitionDocumentReader() {
    return BeanUtils.instantiateClass(this.documentReaderClass);
}

// 3.2 documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
// org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.registerBeanDefinitions()

// 此实现根据 “spring-beans” XSD（或历史上的 DTD）解析 bean 定义。
// <p>打开 DOM 文档；然后初始化在 {@code <beans/>} 级别指定的默认设置；然后解析包含的 bean 定义。
@Override
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
    this.readerContext = readerContext;
    // 在给定的根 {@code <beans/>} 元素内注册每个 bean 定义。
    doRegisterBeanDefinitions(doc.getDocumentElement());
}
```


```java
// 1. doRegisterBeanDefinitions(doc.getDocumentElement())
// org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.doRegisterBeanDefinitions()

// 在给定的根 {@code <beans/>} 元素内注册每个 bean 定义。
@SuppressWarnings("deprecation")  // for Environment.acceptsProfiles(String...)
protected void doRegisterBeanDefinitions(Element root) {
    // Any nested <beans> elements will cause recursion in this method. In
    // order to propagate and preserve <beans> default-* attributes correctly,
    // keep track of the current (parent) delegate, which may be null. Create
    // the new (child) delegate with a reference to the parent for fallback purposes,
    // then ultimately reset this.delegate back to its original (parent) reference.
    // this behavior emulates a stack of delegates without actually necessitating one.
    // --> 译文：任何嵌套的 <beans> 元素都会导致此方法的递归。
    // 为了正确传播和保留 <beans> default-* 属性，请跟踪当前（父）委托，该委托可能为空。
    // 创建新的（子）委托时，会引用父委托，以便进行回退，然后最终将 this.delegate 重置回其原始（父）引用。
    // 此行为模拟了委托堆栈，但实际上并不需要委托堆栈。
    BeanDefinitionParserDelegate parent = this.delegate;
    // 创建 BeanDefinitionParserDelegate，并对其进行初始化默认的 lazy-init、自动装配、依赖项检查设置、init-method、destroy-method 和合并设置。
    BeanDefinitionParserDelegate current = createDelegate(getReaderContext(), root, parent);
    this.delegate = current;

    // 确定给定的节点是否指示默认命名空间。
    if (current.isDefaultNamespace(root)) {
        String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE); // profile
        if (StringUtils.hasText(profileSpec)) {
            // 通过 {@link StringTokenizer} 将给定的 {@code String} 标记为 {@code String} 数组。
            String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
                    profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
            // We cannot use Profiles.of(...) since profile expressions are not supported
            // in XML config. See SPR-12458 for details. --> 译文：由于 XML 配置不支持配置文件表达式，因此无法使用 Profiles.of(...)。详情请参阅 SPR-12458。
            if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                            "] not matching: " + getReaderContext().getResource());
                }
                return;
            }
        }
    }

    preProcessXml(root); // 在开始处理 Bean 定义之前，通过先处理任何自定义元素类型来扩展 XML。此方法是任何其他自定义 XML 预处理的自然扩展点。默认实现为空。
    parseBeanDefinitions(root, current); // 解析文档中根级别的元素：“import”、“alias”、“bean”。
    postProcessXml(root); // 在完成 Bean 定义处理后，通过最后处理任何自定义元素类型，允许 XML 进行扩展。此方法是任何其他自定义 XML 后处理的自然扩展点。

    this.delegate = parent;
}


// 2. parseBeanDefinitions(root, current);
// org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.parseBeanDefinitions()

// 解析文档中根级别的元素：“import”、“alias”、“bean”。
// @param root 文档的 DOM 根元素
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    // 确定给定的节点是否指示默认命名空间。
    // 默认命名空间为 org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.BEANS_NAMESPACE_URI
    // 即： "http://www.springframework.org/schema/beans"
    if (delegate.isDefaultNamespace(root)) {
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element ele) {
                // 默认命名空间
                if (delegate.isDefaultNamespace(ele)) {
                    parseDefaultElement(ele, delegate);
                }
                // 其他命名空间；例如：http://www.springframework.org/schema/mvc
                else {
                    // 解析自定义元素（默认命名空间之外）。
                    delegate.parseCustomElement(ele);
                }
            }
        }
    }
    else {
        delegate.parseCustomElement(root); // 解析自定义元素（默认命名空间之外）。
    }
}

// 3.1. parseDefaultElement(ele, delegate);
// org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.parseDefaultElement()

private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
    if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) { // <import/>
        // 解析 “import” 元素并将来自给定资源的 bean 定义加载到 bean 工厂中。
        importBeanDefinitionResource(ele);
    }
    else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) { // <alias/>
        // 处理给定的别名元素，向注册表注册别名。
        processAliasRegistration(ele);
    }
    else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) { // <bean/>
        // 处理给定的 bean 元素，解析 bean 定义并将其注册到注册表。
        processBeanDefinition(ele, delegate);
    }
    else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {  // <beans/>
        // recurse --> 译文：递归
        doRegisterBeanDefinitions(ele); // 在给定的根 {@code <beans/>} 元素内注册每个 bean 定义。
    }
}

// 3.2. 【1】delegate.parseCustomElement(ele)/delegate.parseCustomElement(root)
// org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(org.w3c.dom.Element)

// 解析自定义元素（默认命名空间之外）。
@Nullable
public BeanDefinition parseCustomElement(Element ele) {
    return parseCustomElement(ele, null);
}

// 3.2. 【2】parseCustomElement(ele, null);
// org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(org.w3c.dom.Element, org.springframework.beans.factory.config.BeanDefinition)

// 解析自定义元素（默认命名空间之外）。
// @param ele 待解析的元素
// @param containingBd 包含 bean 的定义（如果有）
// @return 返回生成的 bean 定义
@Nullable
public BeanDefinition parseCustomElement(Element ele, @Nullable BeanDefinition containingBd) {
    // 获取指定节点的命名空间 URI。
    String namespaceUri = getNamespaceURI(ele);
    if (namespaceUri == null) {
        return null;
    }
    // 返回命名空间解析器，并解析命名空间 URI 并返回找到的 NamespaceHandler 实现。
    NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
    if (handler == null) {
        error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
        return null;
    }
    // 解析指定的 {@link Element}，并将生成的 {@link BeanDefinition BeanDefinitions} 注册到嵌入在提供的 {@link ParserContext}
    // 中的 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}。
    return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
}

// 3.2. 【3】NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
// org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver.resolve()

public static final String DEFAULT_HANDLER_MAPPINGS_LOCATION = "META-INF/spring.handlers";
// 从已配置的映射中查找所提供命名空间 URI 的 {@link NamespaceHandler}。
@Override
@Nullable
public NamespaceHandler resolve(String namespaceUri) {
    // 延迟加载指定的 NamespaceHandler 映射。
    Map<String, Object> handlerMappings = getHandlerMappings();
    Object handlerOrClassName = handlerMappings.get(namespaceUri);
    if (handlerOrClassName == null) {
        return null;
    }
    else if (handlerOrClassName instanceof NamespaceHandler namespaceHandler) {
        return namespaceHandler;
    }
    else {
        String className = (String) handlerOrClassName;
        try {
            Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);
            if (!NamespaceHandler.class.isAssignableFrom(handlerClass)) {
                throw new FatalBeanException("Class [" + className + "] for namespace [" + namespaceUri +
                        "] does not implement the [" + NamespaceHandler.class.getName() + "] interface");
            }
            NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClass);
            // 在构造之后但在解析任何自定义元素之前由 {@link DefaultBeanDefinitionDocumentReader} 调用。
            // 例如：org.springframework.web.servlet.config.MvcNamespaceHandler.init() 初始化了一系列标签的解析器
            namespaceHandler.init();
            handlerMappings.put(namespaceUri, namespaceHandler);
            return namespaceHandler;
        }
        catch (ClassNotFoundException ex) {
            throw new FatalBeanException("Could not find NamespaceHandler class [" + className +
                    "] for namespace [" + namespaceUri + "]", ex);
        }
        catch (LinkageError err) {
            throw new FatalBeanException("Unresolvable class definition for NamespaceHandler class [" +
                    className + "] for namespace [" + namespaceUri + "]", err);
        }
    }
}
```

### prepareBeanFactory(beanFactory)

```java
// org.springframework.context.support.AbstractApplicationContext.prepareBeanFactory()
// 配置工厂的标准上下文特性，例如上下文的 ClassLoader 和后置处理器。
// @param beanFactory 要配置的 BeanFactory
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    // Tell the internal bean factory to use the context's class loader etc. --> 译文：告诉内部 bean 工厂使用上下文的类加载器等。
    beanFactory.setBeanClassLoader(getClassLoader());
    // 指定 Bean 定义值中表达式的解析策略。
    beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
    // 添加一个 PropertyEditorRegistrar 以应用于所有 Bean 创建过程。
    beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

    // Configure the bean factory with context callbacks. --> 译文：使用上下文回调配置 bean 工厂。
    // 添加一个新的 BeanPostProcessor，它将应用于此工厂创建的 Bean。
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    // 忽略自动装配时指定的依赖接口。
    beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
    beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
    beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
    beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationStartupAware.class);

    // BeanFactory interface not registered as resolvable type in a plain factory.
    // MessageSource registered (and found for autowiring) as a bean.
    // --> 译文：BeanFactory 接口未在普通工厂中注册为可解析类型。MessageSource 已注册（并已找到用于自动装配）为 Bean。
    // 注册一个特殊的依赖类型及其对应的自动装配值。
    beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
    beanFactory.registerResolvableDependency(ResourceLoader.class, this);
    beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
    beanFactory.registerResolvableDependency(ApplicationContext.class, this);

    // Register early post-processor for detecting inner beans as ApplicationListeners. --> 译文：注册早期后处理器，用于检测内部 bean 作为 ApplicationListener。
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

    // Detect a LoadTimeWeaver and prepare for weaving, if found. --> 译文：检测 LoadTimeWeaver 并准备织入（如果找到）。
    if (!NativeDetector.inNativeImage() && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
        beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
        // Set a temporary ClassLoader for type matching. --> 译文：设置一个临时 ClassLoader 用于类型匹配。
        beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
    }

    // Register default environment beans. --> 译文：注册默认环境 bean。
    if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) { // environment：工厂中的 {@link Environment} bean 的名称。
        beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) { // systemProperties：工厂中的系统属性 bean 的名称。参见 java.lang.System#getProperties()；系统属性；例如：file.encoding
        beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) { // systemEnvironment：工厂中系统环境 bean 的名称。参见 java.lang.System#getenv()；系统环境；例如：PATH
        beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
    }
    if (!beanFactory.containsLocalBean(APPLICATION_STARTUP_BEAN_NAME)) { // applicationStartup：工厂中的 {@link ApplicationStartup} bean 的名称。
        beanFactory.registerSingleton(APPLICATION_STARTUP_BEAN_NAME, getApplicationStartup());
    }
}
```
