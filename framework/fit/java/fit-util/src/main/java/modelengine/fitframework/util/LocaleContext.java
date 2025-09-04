/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.util;

import java.util.Locale;

/**
 * 表示存储地区信息的上下文。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
public class LocaleContext {
    private final Locale locale;

    /**
     * 构造函数。
     *
     * @param locale 地区信息 {@link Locale}。
     */
    public LocaleContext(Locale locale) {
        this.locale = locale;
    }

    /**
     * 获取上下文中的地区信息。
     *
     * @return 当前上下文中存储的地区信息 {@link Locale}。
     */
    public Locale getLocale() {
        return locale;
    }
}