/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.util.i18n;

import modelengine.fit.http.server.*;
import modelengine.fitframework.annotation.Scope;
import modelengine.fitframework.util.LocaleContext;
import modelengine.fitframework.util.LocaleContextHolder;

import java.util.List;
import java.util.Locale;

/**
 * 地区解析过滤器。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
public class LocaleResolveFilter implements HttpServerFilter {

    private LocaleResolver localeResolver = null;

    private List<String> matchPatterns = List.of("/**");

    private List<String> mismatchPatterns = List.of();

    private Scope scope = Scope.GLOBAL;

    /**
     * 构造函数。
     *
     * @param localeResolver 表示地区解析器的 {@link LocaleResolver}。
     */
    public LocaleResolveFilter(LocaleResolver localeResolver) {
        localeResolver = localeResolver;
    }

    /**
     * 默认构造函数。
     */
    public LocaleResolveFilter() {
        this.localeResolver = new DefualtLocaleResolver();
    }

    @Override
    public String name() {
        return "LocaleResolveFilter";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public List<String> matchPatterns() {
        return matchPatterns;
    }

    @Override
    public List<String> mismatchPatterns() {
        return mismatchPatterns;
    }

    @Override
    public void doFilter(HttpClassicServerRequest request, HttpClassicServerResponse response,
            HttpServerFilterChain chain) throws DoHttpServerFilterException {
        try {
            // 如果参数中带有地区，说明用户想使用新地区执行后续的操作，直接设置地区。
            String paramLocale = request.queries().first("locale").orElse(null);
            Locale responseLocale = null;
            if (paramLocale != null && !paramLocale.trim().isEmpty()) {
                responseLocale = Locale.forLanguageTag(paramLocale);
                LocaleContextHolder.setLocaleContext(new LocaleContext(responseLocale));
            }
            // 如果参数中不包含地区，则解析请求所带的地区参数。
            else {
                Locale locale = localeResolver.resolveLocale(request);
                LocaleContextHolder.setLocaleContext(new LocaleContext(locale));
            }

            // 继续执行后续过滤器。
            chain.doFilter(request, response);

            // responseLocale 是用户期望设置的地区，不受 server 端处理的影响。
            localeResolver.setLocale(response, responseLocale);
        } finally {
            LocaleContextHolder.clear();
        }

    }

    @Override
    public Scope scope() {
        return scope;
    }
}
