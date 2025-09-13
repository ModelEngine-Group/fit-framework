/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation;

import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.i18n.LocaleContextHolder;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.MessageInterpolator;

import java.util.Locale;

/**
 * 检验消息处理的代理类。
 * <p>
 * 从 {@link LocaleContextHolder} 中获取当前线程设置的 {@link Locale} 并委托 {@link MessageInterpolator} 去处理消息。
 * </p>
 *
 * @author 阮睿
 * @since 2025-07-31
 */
public class LocaleContextMessageInterpolator implements MessageInterpolator {
    private final MessageInterpolator targetInterpolator;
    private Locale locale;

    /**
     * 构造函数。
     *
     * @param targetInterpolator 表示目标检验消息处理对象的 {@link MessageInterpolator}。
     */
    public LocaleContextMessageInterpolator(MessageInterpolator targetInterpolator) {
        this.targetInterpolator = targetInterpolator;
        this.locale = Locale.getDefault();
    }

    /**
     * 构造函数，默认使用 {@link ParameterMessageInterpolator} 作为目标检验消息处理对象。
     */
    public LocaleContextMessageInterpolator() {
        this.targetInterpolator = new ParameterMessageInterpolator();
        this.locale = Locale.getDefault();
    }

    /**
     * 构造函数。
     *
     * @param locale 表示当前设置默认的 {@link Locale}。
     */
    public LocaleContextMessageInterpolator(Locale locale) {
        this.targetInterpolator = new ParameterMessageInterpolator();
        this.locale = ObjectUtils.getIfNull(locale, Locale::getDefault);
    }

    /**
     * 构造函数。
     *
     * @param targetInterpolator 表示目标检验消息处理对象的 {@link MessageInterpolator}。
     * @param locale 表示当前设置默认的 {@link Locale}。
     */
    public LocaleContextMessageInterpolator(MessageInterpolator targetInterpolator, Locale locale) {
        this.targetInterpolator = targetInterpolator;
        this.locale = ObjectUtils.getIfNull(locale, Locale::getDefault);
    }

    /**
     * 设置默认的 {@link Locale}。
     *
     * @param locale 默认设置的 {@link Locale}。
     */
    public void setLocale(Locale locale) {
        this.locale = ObjectUtils.getIfNull(locale, Locale::getDefault);
    }

    @Override
    public String interpolate(String messageTemplate, Context context) {
        if (LocaleContextHolder.getLocale() != null) {
            return this.targetInterpolator.interpolate(messageTemplate, context, LocaleContextHolder.getLocale());
        }
        return this.targetInterpolator.interpolate(messageTemplate, context, this.locale);
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        return this.targetInterpolator.interpolate(messageTemplate, context, locale);
    }
}