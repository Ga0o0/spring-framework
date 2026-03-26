package org.springframework.sample.aop.aspect;

import org.springframework.stereotype.Service;

/**
 * {@link TargetService} 实现类
 */
@Service
public class TargetServiceImpl implements TargetService {

    @Override
    public void doMethod1() {
        System.out.println("invoke TargetServiceImpl#doMethod1()");
    }

    @Override
    public String doMethod2() {
        System.out.println("invoke TargetServiceImpl#doMethod2()");
        return "hello world";
    }

    @Override
    public String doMethod3() throws Exception {
        System.out.println("invoke TargetServiceImpl#doMethod3()");
        throw new Exception("some exception");
    }

}