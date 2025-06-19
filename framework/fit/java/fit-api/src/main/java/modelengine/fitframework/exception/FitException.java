/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.exception;

import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 为 FIT 系统提供基础异常类。
 *
 * @author 张越
 * @author 张中恩
 * @author 季聿阶
 * @since 2020-04-23
 */
@ErrorCode(FitException.CODE)
public class FitException extends RuntimeException {
    /** 表示 FIT 体系异常的根异常码。 */
    public static final int CODE = 0x7F000000;

    /** 表示关联服务唯一标识的键。 */
    private static final String GENERICABLE_ID = "genericableId";

    /** 表示关联服务实现唯一标识的键。 */
    private static final String FITABLE_ID = "fitableId";

    /** 表示异常额外的属性。 */
    private final Map<String, String> properties = new HashMap<>();

    /** 表示全局可识别的异常码，可通过序列化跨进程传递。 */
    private final int code;

    /**
     * 通过异常信息来实例化 {@link FitException}。
     * <p>异常码会通过异常类上的 {@link ErrorCode} 进行获取。</p>
     *
     * @param message 表示异常信息的 {@link String}。
     */
    public FitException(String message) {
        super(message);
        this.code = codeOf(this.getClass());
    }

    /**
     * 通过异常原因来实例化 {@link FitException}。
     * <p>异常码会通过异常类上的 {@link ErrorCode} 进行获取。</p>
     *
     * @param cause 表示异常原因的 {@link Throwable}。
     */
    public FitException(Throwable cause) {
        super(cause);
        this.code = codeOf(this.getClass());
    }

    /**
     * 通过异常信息和异常原因来实例化 {@link FitException}。
     * <p>异常码会通过异常类上的 {@link ErrorCode} 进行获取。</p>
     *
     * @param message 表示异常信息的 {@link String}。
     * @param cause 表示异常原因的 {@link Throwable}。
     */
    public FitException(String message, Throwable cause) {
        super(message, cause);
        this.code = codeOf(this.getClass());
    }

    /**
     * 通过异常码和异常信息来实例化 {@link FitException}。
     * <p>异常码通过传入的方式，主要是针对未知异常类型的场景。</p>
     *
     * @param code 表示异常码的 {@code int}。
     * @param message 表示异常信息的 {@link String}。
     */
    public FitException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 通过异常码、异常信息和异常原因来实例化 {@link FitException}。
     *
     * @param code 表示异常码的 {@code int}。
     * @param message 表示异常信息的 {@link String}。
     * @param cause 表示异常原因的 {@link Throwable}。
     */
    public FitException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 通过异常码和异常原因来实例化 {@link FitException}。
     *
     * @param code 表示异常码的 {@code int}。
     * @param cause 表示异常原因的 {@link Throwable}。
     */
    public FitException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    private static int codeOf(Class<?> exceptionClass) {
        ErrorCode annotation = exceptionClass.getAnnotation(ErrorCode.class);
        if (annotation == null) {
            throw new IllegalStateException(StringUtils.format(
                    "Missing annotation declaration. [class={0}, annotation={1}]",
                    exceptionClass.getName(),
                    ErrorCode.class.getName()));
        }
        return annotation.value();
    }

    /**
     * 获取异常码。
     *
     * @return 表示异常码的 {@code int}。
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 获取异常的属性集合。
     *
     * @return 表示异常的属性集合的 {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}。
     */
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    /**
     * 设置异常的属性。
     *
     * @param key 表示待设置的属性的键的 {@link String}。
     * @param value 表示待设置的属性的值的 {@link String}。
     */
    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }

    /**
     * 设置异常的属性集合。
     *
     * @param properties 表示待设置的属性集合的 {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}。
     */
    public void setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    /**
     * 关联发生异常的服务唯一标识。
     *
     * @param genericableId 表示服务唯一标识的 {@link String}。
     */
    public void associateGenericable(String genericableId) {
        this.setProperty(FitException.GENERICABLE_ID, genericableId);
    }

    /**
     * 关联发生异常的服务唯一标识和服务实现唯一标识。
     *
     * @param genericableId 表示服务唯一标识的 {@link String}。
     * @param fitableId 表示服务实现唯一标识的 {@link String}。
     */
    public void associateFitable(String genericableId, String fitableId) {
        this.setProperty(FitException.GENERICABLE_ID, genericableId);
        this.setProperty(FitException.FITABLE_ID, fitableId);
    }

    /**
     * 获取发生降级异常的服务的唯一标识。
     *
     * @return 表示服务的唯一标识的 {@link String}。
     */
    public String associatedGenericableId() {
        return this.getProperties().get(FitException.GENERICABLE_ID);
    }

    /**
     * 获取发生降级异常的服务实现的唯一标识。
     *
     * @return 表示服务实现的唯一标识的 {@link String}。
     */
    public String associatedFitableId() {
        return this.getProperties().get(FitException.FITABLE_ID);
    }

    /**
     * 包装 FIT 异常。
     *
     * @param cause 表示待抛出的异常的 {@link Throwable}。
     * @param genericableId 表示 FIT 异常关联的服务唯一标识的 {@link String}。
     * @return 表示包装的 FIT 通用异常的 {@link FitException}。
     */
    public static FitException wrap(@Nonnull Throwable cause, String genericableId) {
        return wrap(cause, genericableId, cause.getMessage());
    }

    /**
     * 包装 FIT 异常。
     *
     * @param cause 表示待抛出的异常的 {@link Throwable}。
     * @param genericableId 表示 FIT 异常关联的服务唯一标识的 {@link String}。
     * @param fitableId 表示 FIT 异常关联的服务实现唯一标识的 {@link String}。
     * @return 表示包装的 FIT 通用异常的 {@link FitException}。
     */
    public static FitException wrap(@Nonnull Throwable cause, String genericableId, String fitableId) {
        return wrap(cause, genericableId, fitableId, cause.getMessage());
    }

    /**
     * 包装 FIT 异常。
     *
     * @param cause 表示待抛出的异常的 {@link Throwable}。
     * @param genericableId 表示 FIT 异常关联的服务唯一标识的 {@link String}。
     * @param fitableId 表示 FIT 异常关联的服务实现唯一标识的 {@link String}。
     * @param message 表示 FIT 异常消息的 {@link String}。
     * @return 表示包装的 FIT 通用异常的 {@link FitException}。
     */
    public static FitException wrap(@Nonnull Throwable cause, String genericableId, String fitableId, String message) {
        FitException fitException;
        if (cause instanceof FitException) {
            fitException = (FitException) cause;
        } else {
            fitException = new FitException(FitException.CODE, message, cause);
        }
        if (StringUtils.isBlank(fitException.associatedGenericableId())) {
            fitException.associateFitable(genericableId, fitableId);
        }
        return fitException;
    }
}
