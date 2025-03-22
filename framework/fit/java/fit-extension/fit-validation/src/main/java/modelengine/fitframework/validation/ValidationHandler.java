/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.validation;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.aop.JoinPoint;
import modelengine.fitframework.aop.annotation.Aspect;
import modelengine.fitframework.aop.annotation.Before;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.pattern.builder.BuilderFactory;
import modelengine.fitframework.util.AnnotationUtils;
import modelengine.fitframework.util.ArrayUtils;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.ReflectionUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.validation.constraints.Constraint;
import modelengine.fitframework.validation.exception.ConstraintViolationException;
import modelengine.fitframework.validation.group.DefaultGroup;
import modelengine.fitframework.value.PropertyValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 校验入口类。
 * <p>当调用的方法参数包含 {@link Validated} 注解时，会对该方法进行校验处理。当前存在两种场景包含该场景：</p>
 * <ol>
 *     <li>方法参数直接包含 {@link Validated} 注解，此时校验的是该参数对象的字段。</li>
 *     <li>方法参数包含约束注解，如 {@link modelengine.fitframework.validation.constraints.NotEmpty}，此时校验的是该参数对象。</li>
 * </ol>
 *
 * @author 邬涨财
 * @since 2023-03-14
 */
@Aspect
@Component
public class ValidationHandler {
    private final BeanContainer container;
    private final Map<ValidatorKey, List<ConstraintValidator<Annotation, Object>>> validatorMap =
            new ConcurrentHashMap<>();

    /**
     * 使用指定的 Bean 容器初始化 {@link ValidationHandler} 的新实例。
     *
     * @param container 表示 Bean 容器的 {@link BeanContainer}。
     * @throws IllegalArgumentException 当 {@code container} 为 {@code null} 时。
     */
    public ValidationHandler(BeanContainer container) {
        this.container =
                Validation.notNull(container, "The bean container cannot be null when construct validation handle.");
    }

    @Before(value = "@params(validated)", argNames = "joinPoint, validated")
    private void handle(JoinPoint joinPoint, Validated validated) {
        Method method = joinPoint.getMethod();
        Object[] args = joinPoint.getArgs();
        List<ConstraintViolation> violations = this.handleValidationMethod(method, args)
                .stream()
                .flatMap(validationMetadata -> this.validate(validationMetadata).stream())
                .collect(Collectors.toList());
        Validation.isTrue(violations.isEmpty(), () -> new ConstraintViolationException(violations));
    }

    private List<ValidationMetadata> handleValidationMethod(Method method, Object[] args) {
        Class<?>[] classGroups = this.getClassGroups(method);
        Parameter[] parameters = ReflectionUtils.getParameters(method);
        List<ValidationMetadata> validationMetadataList = new ArrayList<>();
        for (int index = 0; index < parameters.length; index++) {
            if (this.hasConstraintAnnotation(parameters[index])) {
                ValidationMetadata validationMetadata = ValidationMetadata.createValidationParameter(parameters[index],
                        classGroups,
                        args[index],
                        method);
                validationMetadataList.add(validationMetadata);
                continue;
            }
            if (this.hasValidatedAnnotation(parameters[index])) {
                Class<?>[] validationGroups = this.getValidationGroups(parameters[index], classGroups);
                PropertyValue parameterValue = PropertyValue.createParameterValue(parameters[index]);
                validationMetadataList.addAll(this.getValidationFields(parameterValue.getParameterizedType(),
                        args[index],
                        method,
                        validationGroups));
            }
        }
        return validationMetadataList;
    }

    private Class<?>[] getClassGroups(Method method) {
        return AnnotationUtils.getAnnotation(this.container, method.getDeclaringClass(), Validated.class)
                .map(Validated::value)
                .filter(ArrayUtils::isNotEmpty)
                .orElseGet(() -> new Class[] {DefaultGroup.class});
    }

    private Class<?>[] getValidationGroups(Parameter parameters, Class<?>[] classGroups) {
        return AnnotationUtils.getAnnotation(this.container, parameters, Validated.class)
                .map(Validated::value)
                .filter(ArrayUtils::isNotEmpty)
                .orElse(classGroups);
    }

    private boolean hasValidatedAnnotation(AnnotatedElement element) {
        return AnnotationUtils.getAnnotation(this.container, element, Validated.class).isPresent();
    }

    private boolean hasConstraintAnnotation(AnnotatedElement element) {
        return AnnotationUtils.getAnnotation(this.container, element, Constraint.class).isPresent();
    }

    private List<ValidationMetadata> getValidationFields(Type validationObject, Object validationValue, Method method,
            Class<?>[] validationGroups) {
        if (validationObject instanceof ParameterizedType) {
            ParameterizedType parameterizedType = ObjectUtils.cast(validationObject);
            if (Collection.class.isAssignableFrom(ObjectUtils.cast(parameterizedType.getRawType()))) {
                return this.handleCollection(parameterizedType.getActualTypeArguments(),
                        ObjectUtils.cast(validationValue),
                        method,
                        validationGroups);
            }
            if (Map.class.isAssignableFrom(ObjectUtils.cast(parameterizedType.getRawType()))) {
                return this.handleMap(parameterizedType.getActualTypeArguments(),
                        ObjectUtils.cast(validationValue),
                        method,
                        validationGroups);
            }
        }
        if (validationObject instanceof Class) {
            Field[] fields = new Field[0];
            Map<String, Object> fieldNameValues = Collections.emptyMap();
            if (!isSimpleClass(ObjectUtils.cast(validationObject))) {
                fields = ReflectionUtils.getDeclaredFields(ObjectUtils.cast(validationObject), true);
                fieldNameValues = Arrays.stream(fields)
                        .collect(HashMap::new,
                                (map, field) -> map.put(field.getName(),
                                        validationValue == null
                                                ? null
                                                : ReflectionUtils.getField(validationValue, field)),
                                HashMap::putAll);
            }
            List<ValidationMetadata> constraintFieldMetadata =
                    this.getConstraintFieldMetadata(method, validationGroups, fields, fieldNameValues);
            List<ValidationMetadata> validatedFieldMetadata =
                    this.getValidatedFieldMetadata(method, validationGroups, fields, fieldNameValues);
            return CollectionUtils.merge(constraintFieldMetadata, validatedFieldMetadata);
        }
        return Collections.emptyList();
    }

    private List<ValidationMetadata> handleCollection(Type[] actualTypeArgs, Collection<?> validationValueCollection,
            Method method, Class<?>[] validationGroups) {
        Validation.equals(actualTypeArgs.length, 1, "The collection must have exactly 1 parameterized type.");
        if (CollectionUtils.isEmpty(validationValueCollection)) {
            return Collections.emptyList();
        }
        return validationValueCollection.stream()
                .flatMap(element -> this.getValidationFields(actualTypeArgs[0], element, method, validationGroups)
                        .stream())
                .collect(Collectors.toList());
    }

    private List<ValidationMetadata> handleMap(Type[] actualTypeArgs, Map<?, ?> validationValueMap, Method method,
            Class<?>[] validationGroups) {
        Validation.equals(actualTypeArgs.length, 2, "The map must have exactly 2 parameterized types.");
        if (MapUtils.isEmpty(validationValueMap)) {
            return Collections.emptyList();
        }
        return validationValueMap.entrySet()
                .stream()
                .flatMap(entry -> Stream.concat(
                        this.getValidationFields(actualTypeArgs[0], entry.getKey(), method, validationGroups).stream(),
                        this.getValidationFields(actualTypeArgs[1], entry.getValue(), method, validationGroups).stream()
                ))
                .collect(Collectors.toList());
    }

    private List<ValidationMetadata> getConstraintFieldMetadata(Method method, Class<?>[] validationGroups,
            Field[] fields, Map<String, Object> fieldNameValues) {
        return Arrays.stream(fields)
                .filter(this::hasConstraintAnnotation)
                .map(field -> ValidationMetadata.createValidationField(field,
                        validationGroups,
                        fieldNameValues.get(field.getName()),
                        method))
                .collect(Collectors.toList());
    }

    private List<ValidationMetadata> getValidatedFieldMetadata(Method method, Class<?>[] validationGroups,
            Field[] fields, Map<String, Object> fieldNameValues) {
        return Arrays.stream(fields)
                .filter(this::hasValidatedAnnotation)
                .flatMap(field -> this.getValidationFields(PropertyValue.createFieldValue(field).getParameterizedType(),
                        fieldNameValues.get(field.getName()),
                        method,
                        validationGroups).stream())
                .collect(Collectors.toList());
    }

    private List<ConstraintViolation> validate(ValidationMetadata validationMetadata) {
        List<ConstraintViolation> violations = new ArrayList<>();
        for (Annotation annotation : validationMetadata.annotations()) {
            if (!this.needValidate(annotation, validationMetadata)) {
                continue;
            }
            this.getConstraintValidators(annotation, validationMetadata)
                    .stream()
                    .filter(validator -> !validator.isValid(validationMetadata.value()))
                    .forEach((validator) -> violations.add(this.buildConstraintViolation(validationMetadata,
                            annotation,
                            validator.args())));
        }
        return violations;
    }

    private List<ConstraintValidator<Annotation, Object>> getConstraintValidators(Annotation annotation,
            ValidationMetadata validationMetadata) {
        ValidatorKey validatorKey =
                ValidatorKey.builder().annotation(annotation).annotatedElement(validationMetadata.element()).build();
        return this.validatorMap.computeIfAbsent(validatorKey, (key) -> this.buildConstraintValidators(annotation));
    }

    private List<ConstraintValidator<Annotation, Object>> buildConstraintValidators(Annotation annotation) {
        Class<? extends ConstraintValidator<?, ?>>[] validatorClasses =
                AnnotationUtils.getAnnotation(this.container, annotation.annotationType(), Constraint.class)
                        .orElseThrow(IllegalStateException::new)
                        .value();
        return Arrays.stream(validatorClasses)
                .map(validatorClass -> this.buildConstraintValidator(annotation, validatorClass))
                .collect(Collectors.toList());
    }

    private ConstraintValidator<Annotation, Object> buildConstraintValidator(Annotation annotation,
            Class<? extends ConstraintValidator<?, ?>> validatorClass) {
        ConstraintValidator<Annotation, Object> validator =
                ObjectUtils.cast(ReflectionUtils.instantiate(validatorClass));
        validator.initialize(annotation);
        return validator;
    }

    private ConstraintViolation buildConstraintViolation(ValidationMetadata validationMetadata, Annotation annotation,
            Object[] args) {
        String message =
                String.valueOf(this.getAnnotationPropertyValue(annotation, "message").orElse(StringUtils.EMPTY));
        return ConstraintViolation.builder()
                .message(message)
                .propertyName(validationMetadata.name())
                .propertyValue(validationMetadata.value())
                .validationMethod(validationMetadata.getValidationMethod())
                .args(args)
                .build();
    }

    /**
     * 判断该校验对象是否需要校验，通过比较校验对象的 {@link Validated} 注解和 {@link Constraint} 注解是否有相同的分组。
     * <p>其中通过校验元数据 {@link ValidationMetadata} 可以获得 {@link Validated} 注解的分组；通过约束注解可以获得 {@link Constraint}
     * 上的分组值。</p>
     *
     * @param constraintAnnotation 表示约束注解的 {@link Annotation}。
     * @param validationMetadata 表示需要校验的元数据的 {@link ValidationMetadata}。
     * @return 表示是否需要校验的 {@code boolean}。
     */
    private boolean needValidate(Annotation constraintAnnotation, ValidationMetadata validationMetadata) {
        if (constraintAnnotation.annotationType().getAnnotation(Constraint.class) == null) {
            return false;
        }
        List<Class<?>> validationClasses = Arrays.asList(validationMetadata.groups());
        Optional<Object> optionGroups = this.getAnnotationPropertyValue(constraintAnnotation, "groups");
        if (!optionGroups.isPresent()) {
            return validationClasses.contains(DefaultGroup.class);
        }
        Class<?>[] groups = ObjectUtils.cast(optionGroups.get());
        if (groups.length == 0) {
            return validationClasses.contains(DefaultGroup.class);
        }
        return !CollectionUtils.intersect(validationClasses, Arrays.asList(groups)).isEmpty();
    }

    private Optional<Object> getAnnotationPropertyValue(Annotation annotation, String propertyKey) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        try {
            Method method = annotationType.getDeclaredMethod(propertyKey);
            return Optional.ofNullable(ReflectionUtils.invoke(annotation, method));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private boolean isSimpleClass(Class<?> validationClass) {
        return validationClass.isPrimitive() || ReflectionUtils.isPrimitiveWrapper(validationClass)
                || validationClass == String.class;
    }

    /**
     * 表示校验器缓存 {@link #validatorMap} 的键。
     */
    public interface ValidatorKey {
        /**
         * 获取校验元素的 {@link AnnotatedElement}。
         *
         * @return 表示校验元素的 {@link AnnotatedElement}。
         */
        AnnotatedElement annotatedElement();

        /**
         * 获取校验的约束注解的 {@link Annotation}。
         *
         * @return 表示校验的约束注解的 {@link Annotation}。
         */
        Annotation annotation();

        /**
         * {@link ValidatorKey} 的构建器。
         */
        interface Builder {
            /**
             * 向构建器设置校验元素的唯一标识。
             *
             * @param annotatedElement 表示设置的校验元素的唯一标识的 {@link String}。
             * @return 表示构建器的 {@link Builder}。
             */
            Builder annotatedElement(AnnotatedElement annotatedElement);

            /**
             * 向构建器设置校验的约束注解。
             *
             * @param annotation 表示设置的校验约束注解的 {@link Annotation}。
             * @return 表示构建器的 {@link Builder}。
             */
            Builder annotation(Annotation annotation);

            /**
             * 构建对象。
             *
             * @return 表示构建出来的对象的 {@link }。
             */
            ValidatorKey build();
        }

        /**
         * 获取 {@link ValidatorKey} 的构建器。
         *
         * @return 表示 {@link ValidatorKey} 的构建器的 {@link Builder}。
         */
        static Builder builder() {
            return builder(null);
        }

        /**
         * 获取 {@link ValidatorKey} 的构建器，同时将指定对象的值进行填充。
         *
         * @param value 表示指定对象的 {@link ValidatorKey}。
         * @return 表示 {@link ValidatorKey} 的构建器的 {@link Builder}。
         */
        static Builder builder(ValidatorKey value) {
            return BuilderFactory.get(ValidatorKey.class, Builder.class).create(value);
        }
    }
}