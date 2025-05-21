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

```text
org.springframework.context.support.AbstractApplicationContext#AbstractApplicationContext()
```

```java
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



### 1.2. setConfigLocations(configLocations)


## 2. refresh()