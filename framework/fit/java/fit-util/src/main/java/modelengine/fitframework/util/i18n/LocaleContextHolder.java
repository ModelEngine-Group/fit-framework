/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.util.i18n;

import java.util.Locale;

/**
 * 表示存储地区的线程上下文。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
public class LocaleContextHolder {
    private static final ThreadLocal<Locale> LOCALE_CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的地区上下文。
     *
     * @param locale 表示待存储在当前线程地区上下文的 {@link Locale}。
     */
    public static void setLocale(Locale locale) {
        if (locale != null) {
            LOCALE_CONTEXT_HOLDER.set(locale);
        }
    }

    /**
     * 获取当前线程的地区。
     *
     * @return 表示当前线程上下文存储地区信息的 {@link Locale}。
     */
    public static Locale getLocale() {
        return LOCALE_CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的地区上下文。
     */
    public static void clear() {
        LOCALE_CONTEXT_HOLDER.remove();
    }
}