package org.springframework.sample.aop.aspect;

/**
 * 定义接口
 */
public interface TargetService {
    void doMethod1();
    String doMethod2();
    String doMethod3() throws Exception;
}