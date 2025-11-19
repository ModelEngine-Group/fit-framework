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
 * 表示地区解析过滤器的配置类。
 *
 * @author 阮睿
 * @since 2025-09-11
 */
@Component
public class LocaleResolveFilterConfig {
    /**
     * 创建地区解析过滤器 bean 对象。
     *
     * @return 表示作为 bean 的地区解析过滤器对象的 {@link LocaleResolveFilter}。
     */
    @Bean
    public LocaleResolveFilter localeResolveFilter() {
        return new LocaleResolveFilter();
    }
}