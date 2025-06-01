/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.globalization;

import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.util.ArrayUtils;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 为 {@link StringResource} 提供基于 {@link ResourceBundle} 的实现。
 * <p>可用以获取指定资源的在多语言环境下使用的消息资源。</p>
 *
 * @author 梁济时
 * @since 2022-11-21
 */
final class ResourceBundleStringResource implements StringResource {
    private final ClassLoader loader;
    private final String baseName;
    private final Map<Locale, ResourceBundle> bundles;
    private final MessageBundleControl control;

    ResourceBundleStringResource(ClassLoader loader, String baseName, String encoding) {
        this.loader = loader;
        this.baseName = baseName;
        this.bundles = new ConcurrentHashMap<>();
        this.control = new MessageBundleControl(encoding);
    }

    @Override
    public String toString() {
        return this.baseName;
    }

    @Override
    public String getMessageWithDefault(Locale locale, String key, String defaultMessage, Object[] args) {
        String message = this.getMessageWithDefault(locale, key, defaultMessage);
        if (ArrayUtils.isNotEmpty(args)) {
            message = StringUtils.format(message, args);
        }
        return message;
    }

    @Override
    public String getMessageWithDefault(Locale locale, String key, String defaultMessage, Map<String, Object> args) {
        String message = this.getMessageWithDefault(locale, key, defaultMessage);
        if (MapUtils.isNotEmpty(args)) {
            message = StringUtils.format(message, args);
        }
        return message;
    }

    private String getMessageWithDefault(Locale locale, String key, String defaultMessage) {
        ResourceBundle bundle = this.bundles.computeIfAbsent(locale, this::loadBundle);
        String message;
        try {
            message = bundle.getString(key);
        } catch (MissingResourceException ignored) {
            message = null;
        }
        if (StringUtils.isBlank(message) && StringUtils.isNotBlank(defaultMessage)) {
            message = defaultMessage;
        }
        return message;
    }

    private ResourceBundle loadBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle(this.baseName, locale, this.loader, this.control);
        } catch (MissingResourceException ignored) {
            return Empty.INSTANCE;
        }
    }

    private static class Empty extends ResourceBundle {
        static final Empty INSTANCE = new Empty();

        @Override
        protected Object handleGetObject(@Nonnull String key) {
            return null;
        }

        @Override
        @Nonnull
        public Enumeration<String> getKeys() {
            return new Enumeration<String>() {
                @Override
                public boolean hasMoreElements() {
                    return false;
                }

                @Override
                public String nextElement() {
                    throw new NoSuchElementException();
                }
            };
        }
    }
}
