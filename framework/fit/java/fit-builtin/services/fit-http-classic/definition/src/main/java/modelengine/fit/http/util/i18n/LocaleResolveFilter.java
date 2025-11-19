/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.util.i18n;

import modelengine.fit.http.server.DoHttpServerFilterException;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fit.http.server.HttpServerFilter;
import modelengine.fit.http.server.HttpServerFilterChain;
import modelengine.fitframework.annotation.Scope;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 地区解析过滤器，使用 {@link LocaleResolver} 进行地区解析。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
public class LocaleResolveFilter implements HttpServerFilter {
    private LocaleResolver localeResolver = null;
    private List<String> matchPatterns = List.of("/**");
    private List<String> mismatchPatterns = List.of();
    private Scope scope = Scope.PLUGIN;

    /**
     * 构造函数。
     *
     * @param localeResolver 表示地区解析器的 {@link LocaleResolver}。
     */
    public LocaleResolveFilter(LocaleResolver localeResolver) {
        this.localeResolver = Validation.notNull(localeResolver, "The locale resolver cannot be null.");
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
        return this.matchPatterns;
    }

    @Override
    public List<String> mismatchPatterns() {
        return this.mismatchPatterns;
    }

    @Override
    public void doFilter(HttpClassicServerRequest request, HttpClassicServerResponse response,
            HttpServerFilterChain chain) throws DoHttpServerFilterException {
        try {
            Locale responseLocale = this.resolveLocaleFromParam(request);
            // 如果参数中带有地区，说明用户想使用新地区执行后续的操作，直接设置地区。
            if (responseLocale != null) {
                LocaleContextHolder.setLocale(responseLocale);
            } else {
                // 如果参数中不包含地区，则解析请求所带的地区参数。
                Locale locale = this.localeResolver.resolveLocale(request);
                LocaleContextHolder.setLocale(locale);
            }

            // 继续执行后续过滤器。
            chain.doFilter(request, response);

            if (!response.isCommitted()) {
                // responseLocale 是用户期望设置的地区，不受 server 端处理的影响。
                this.localeResolver.setLocale(response, responseLocale);
            }
        } finally {
            LocaleContextHolder.clear();
        }
    }

    @Override
    public Scope scope() {
        return this.scope;
    }

    private Locale resolveLocaleFromParam(HttpClassicServerRequest request) {
        Optional<String> paramLocale = request.queries().first("locale");
        String localeString = paramLocale.orElse(null);
        if (StringUtils.isNotBlank(localeString)) {
            return Locale.forLanguageTag(localeString);
        }
        return null;
    }
}