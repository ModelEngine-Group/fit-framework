/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.util;

import java.util.Locale;

/**
 * 表示存储地区上下文的线程上下文。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
public class LocaleContextHolder {
    private static final ThreadLocal<LocaleContext> LOCALE_CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的地区上下文。
     *
     * @param localeContext 表示将要存储在当前线程的地区上下文 {@link LocaleContext}。
     */
    public static void setLocaleContext(LocaleContext localeContext) {
        LOCALE_CONTEXT_HOLDER.set(localeContext);
    }

    /**
     * 获取当前线程的地区上下文。
     *
     * @return 表示当前线程的地区上下文 {@link LocaleContext}。
     */
    public static LocaleContext getLocaleContext() {
        return LOCALE_CONTEXT_HOLDER.get();
    }

    /**
     * 获取当前线程的地区。
     *
     * @return 表示当前线程上下文存储的地区信息 {@link Locale}。
     */
    public static Locale getLocale() {
        LocaleContext context = getLocaleContext();
        return context != null ? context.getLocale() : null;
    }

    /**
     * 清除当前线程的地区上下文。
     */
    public static void clear() {
        LOCALE_CONTEXT_HOLDER.remove();
    }
}
