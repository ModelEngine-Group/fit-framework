/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.ohscript.util;

import modelengine.fit.ohscript.script.errors.GrammarSyntaxException;
import modelengine.fit.ohscript.script.errors.OhPanic;
import modelengine.fit.ohscript.script.interpreter.ASTEnv;
import modelengine.fit.ohscript.script.interpreter.ActivationContext;
import modelengine.fit.ohscript.script.interpreter.ReturnValue;
import modelengine.fit.ohscript.script.parser.nodes.ArgumentNode;
import modelengine.fit.ohscript.script.parser.nodes.BlockNode;
import modelengine.fit.ohscript.script.parser.nodes.FunctionDeclareNode;
import modelengine.fit.ohscript.script.semanticanalyzer.type.expressions.TypeExprFactory;
import modelengine.fitframework.beans.ObjectInstantiator;
import modelengine.fitframework.util.LockUtils;
import modelengine.fitframework.util.ObjectUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 工具
 *
 * @since 1.0
 */
public class Tool {
    private static final Object lock = LockUtils.newSynchronizedLock();

    private static AtomicLong id = new AtomicLong(1);

    /**
     * 打印警告信息
     *
     * @param info 警告信息
     */
    public static void warn(String info) {
    }

    /**
     * 生成一个新的唯一ID
     *
     * @return 返回生成的唯一ID
     */
    public static long newId() {
        long result = id.incrementAndGet();
        // 不能为非正数，有特殊含义
        if (result <= 0) {
            initId();
            return newId();
        }
        return result;
    }

    /**
     * set id
     *
     * @param value value
     */
    protected static void setId(long value) {
        id.set(value);
    }

    /**
     * 初始化id，只有在id为非正数时生效
     */
    private static void initId() {
        synchronized (lock) {
            if (id.get() <= 0) {
                setId(1);
            }
        }
    }

    /**
     * 抛出语法错误
     *
     * @param info 错误信息
     * @throws GrammarSyntaxException 抛出的异常
     */
    public static void grammarError(String info) {
        warn(info);
        throw new GrammarSyntaxException(info);
    }

    /**
     * 生成一个随机的UUID
     *
     * @return 返回生成的UUID
     */
    public static String uuid() {
        return String.valueOf(newId());
    }

    /**
     * 通过反射和Unsafe类创建一个新的实例，无需调用构造方法
     *
     * @param clazz 类型
     * @return 返回创建的实例
     */
    public static <T> T createInstance(Class<T> clazz) {
        return ObjectInstantiator.standard(clazz).newInstance();
    }

    /**
     * 通过一个类型创建一个ohscript代理对象，该ohscript对象代理部分方法，其他有origin提供
     *
     * @param clazz 类型
     * @param agent 该Java对象的ohscript扩展代理
     * @param env astEnv
     * @return 返回创建的代理对象
     * @throws Exception 抛出的异常
     */
    public static <T> T createOhProxy(Class<T> clazz, Map<String, ReturnValue> agent, ASTEnv env) throws Exception {
        OhProxy proxy = new OhProxy(agent, null, env);
        return createProxy(clazz, (callable, method, args) -> proxy.invoke(callable, method, args));
    }

    /**
     * 通过一个类型创建一个ohscript代理对象，该ohscript对象代理部分方法，其他有origin提供
     *
     * @param origin 待扩展Java对象
     * @param agent 该Java对象的ohscript扩展代理
     * @param env astEnv
     * @return 返回创建的代理对象
     * @throws Exception 抛出的异常
     */
    public static <T> T createOhProxy(T origin, Map<String, ReturnValue> agent, ASTEnv env) throws Exception {
        OhProxy proxy = new OhProxy(agent, origin, env);
        return createProxy((Class<T>) origin.getClass(),
                (callable, method, args) -> proxy.invoke(callable, method, args));
    }

    /**
     * 通过类型新建一个代理，类型可以是任意接口，实体类，抽象类
     *
     * @param clazz 类型
     * @param interceptor 方法拦截器，此处使用代理函数代理
     * @return 代理类
     * @throws Exception 异常
     */
    private static <T> T createProxy(Class<T> clazz, TriFunction<Callable<?>, Method, Object[], Object> interceptor)
            throws Exception {
        DynamicType.Builder builder = new ByteBuddy().subclass(clazz).method(ElementMatchers.any()) // 匹配所有方法
                .intercept(MethodDelegation.to(new MethodInterceptor(interceptor)));

        Class<? extends T> proxyClass =
                builder.make().load(Tool.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();

        return createInstance(proxyClass);
    }

    /**
     * 解析函数，并执行函数体内的语句
     *
     * @param function 函数声明节点
     * @param argValues 参数值列表
     * @param current 当前激活环境
     * @return 返回函数执行的结果
     * @throws OhPanic 执行过程中可能抛出的异常
     */
    public static ReturnValue interpretFunction(FunctionDeclareNode function, List<ReturnValue> argValues,
            ActivationContext current) throws OhPanic {
        ReturnValue argValue = argValues.remove(0);
        ArgumentNode arg = function.argument();
        BlockNode block = function.body();
        ASTEnv env = current.env();
        ActivationContext newCurrent = env.push(block.scope(), env, current);
        try {
            if (arg.typeExpr() != TypeExprFactory.createUnit()) {
                newCurrent.put(arg.argument(), argValue);
            }
        } catch (Exception e) {
            newCurrent.put(arg.argument(), argValue);
        }
        ReturnValue value = block.interpret(newCurrent.env(), newCurrent);
        if (!argValues.isEmpty()) {
            value = interpretFunction(ObjectUtils.cast(value.value()), argValues, newCurrent);
        }
        if (value == ReturnValue.IGNORE) {
            value = ReturnValue.UNIT;
        }
        return value;
    }
}

