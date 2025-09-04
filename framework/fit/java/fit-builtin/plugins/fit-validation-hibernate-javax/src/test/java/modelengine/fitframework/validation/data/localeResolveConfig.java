/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import modelengine.fit.http.util.i18n.LocaleResolveFilter;
import modelengine.fitframework.annotation.Bean;
import modelengine.fitframework.annotation.Component;

/**
 * 表示 {@link LocaleResolveFilter} 的配置类。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
@Component
public class localeResolveConfig {
    /**
     * 注册一个 {@link LocaleResolveFilter} 的 bean。
     *
     * @return 返回一个 {@link LocaleResolveFilter} 的实例。
     */
    @Bean
    public LocaleResolveFilter localeResolveFilter() {
        return new LocaleResolveFilter();
    }
}
