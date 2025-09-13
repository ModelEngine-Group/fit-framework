/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Scope;
import modelengine.fitframework.aop.JoinPoint;
import modelengine.fitframework.aop.annotation.Aspect;
import modelengine.fitframework.aop.annotation.Before;
import modelengine.fitframework.ioc.annotation.PreDestroy;

import org.hibernate.validator.HibernateValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

/**
 * 校验入口类。
 * <p>
 * 当调用的类包含 {@link Validated} 注解时，会对该类公共方法参数进行校验处理。
 * </p>
 *
 * @author 阮睿
 * @since 2025-07-18
 */
@Aspect(scope = Scope.GLOBAL)
@Component
public class ValidationHandler implements AutoCloseable {
    private final ValidatorFactory validatorFactory;
    private final Validator validator;
    private final LocaleContextMessageInterpolator messageInterpolator;

    public ValidationHandler() {
        this.messageInterpolator = new LocaleContextMessageInterpolator();
        this.validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(this.messageInterpolator)
                .failFast(false)
                .buildValidatorFactory();
        this.validator = this.validatorFactory.getValidator();
    }

    /**
     * 设置校验信息语言。
     *
     * @param locale 表示校验语言的 {@link Locale}。
     */
    public void setLocale(Locale locale) {
        this.messageInterpolator.setLocale(locale);
    }

    /**
     * 方法参数校验处理。
     *
     * @param joinPoint 表示切面切点的 {@link JoinPoint}。
     * @param validated 表示切点方法所属类的校验注解的 {@link Validated}。
     */
    @Before(value = "@target(validated) && execution(public * *(..))", argNames = "joinPoint, validated")
    private void handle(JoinPoint joinPoint, Validated validated) {
        // 检查方法参数是否包含被 javax.validation.Constraint 标注的校验注解。
        if (hasJavaxConstraintAnnotations(joinPoint.getMethod().getParameters())) {
            ExecutableValidator execVal = this.validator.forExecutables();
            Set<ConstraintViolation<Object>> result = execVal.validateParameters(joinPoint.getTarget(),
                    joinPoint.getMethod(),
                    joinPoint.getArgs(),
                    validated.value());
            if (!result.isEmpty()) {
                throw new ConstraintViolationException(result);
            }
        }
    }

    @PreDestroy
    @Override
    public void close() {
        this.validatorFactory.close();
    }

    /**
     * 检查方法参数是否包含 {@code javax.validation} 校验注解。
     *
     * @param parameters 表示可能携带校验注解的方法参数数组 {@link Parameter}{@code []}。
     * @return 如果包含 {@code javax.validation} 标注的校验注解则返回 {@code true}，否则返回 {@code false}。
     */
    private boolean hasJavaxConstraintAnnotations(Parameter[] parameters) {
        return Arrays.stream(parameters).anyMatch(this::hasConstraintAnnotationsInParameter);
    }

    /**
     * 检查参数及其泛型类型参数是否包含校验注解。
     *
     * @param parameter 表示可能携带校验注解的方法参数 {@link Parameter}。
     * @return 如果包含 {@code javax.validation} 标注的校验注解则返回 {@code true}，否则返回 {@code false}。
     */
    private boolean hasConstraintAnnotationsInParameter(Parameter parameter) {
        return hasConstraintAnnotationsInType(parameter.getAnnotatedType());
    }

    /**
     * 判断参数类型，解析参数本身注解或其泛型类型参数注解。
     *
     * @param annotatedType 表示待检查的参数类型 {@link AnnotatedType}。
     * @return 如果包含 {@code javax.validation} 标注的校验注解则返回 {@code true}，否则返回 {@code false}。
     */
    private boolean hasConstraintAnnotationsInType(AnnotatedType annotatedType) {
        // 检查当前类型上的注解。
        if (annotatedType.getAnnotations().length > 0) {
            return Arrays.stream(annotatedType.getAnnotations()).anyMatch(this::isJavaxConstraintAnnotation);
        }
        // 如果是参数化类型，递归检查类型参数。
        if (annotatedType instanceof AnnotatedParameterizedType parameterizedType) {
            return Arrays.stream(parameterizedType.getAnnotatedActualTypeArguments())
                    .anyMatch(this::hasConstraintAnnotationsInType);
        }
        return false;
    }

    /**
     * 检查注解是否属于 {@code javax.validation} 注解。
     * <p>
     * 由于存在嵌套校验的情况，{@code @Valid} 与其他校验注解都可以标注参数需要进行校验，但两者的实现与语义上存在差异，处理逻辑不能合并，因此分情况讨论：
     * </p>
     * <ol>
     *     <li>
     *         {@code @Valid} 注解检查。用于标记需要级联校验的对象，例如：{@code void validateCompany(@Valid Company company)}。
     *     </li>
     *     <li>
     *         其他携带 {@code @Constraint} 元注解的校验注解检查。例如：{@code void validateEmployee(@NotBlank String name, @Positive
     *         int)}。
     *     </li>
     * </ol>
     *
     * @param annotation 要检查的注解 {@link java.lang.annotation.Annotation}。
     * @return 如果属于 {@code javax.validation} 注解（即 {@code @Valid} 或携带 {@code @Constraint}），则返回 {@code true}，否则返回
     * {@code false}。
     */
    private boolean isJavaxConstraintAnnotation(Annotation annotation) {
        // @Valid 注解检查。
        if (annotation.annotationType().getName().equals("javax.validation.Valid")) {
            return true;
        }
        // 检查 javax.validation.constraints， org.hibernate.validator.constraints 包下的注解或者用户根据 javax 标准自行实现的注解检查。
        // 通过 Constraint 注解检查当前注解是否为校验注解。
        Annotation[] metaAnnotations = annotation.annotationType().getAnnotations();
        return Arrays.stream(metaAnnotations).anyMatch(metaAnnotation -> {
            String packageName = metaAnnotation.annotationType().getPackage().getName();
            String className = metaAnnotation.annotationType().getSimpleName();
            return "javax.validation".equals(packageName) && "Constraint".equals(className);
        });
    }
}