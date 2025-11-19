/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.util.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link LocaleContextHolder} 的单元测试。
 *
 * @author 阮睿
 * @since 2025-09-09
 */
@DisplayName("测试 LocaleContextHolder")
public class LocaleContextHolderTest {
    @AfterEach
    void tearDown() {
        LocaleContextHolder.clear();
    }

    @Nested
    @DisplayName("Test method: setLocale and getLocale")
    class TestSetAndGetLocaleContext {
        @Test
        @DisplayName("Given locale with zh_CN then return the same locale")
        void givenLocaleContextWithZhCNThenReturnSameLocaleContext() {
            Locale locale = Locale.SIMPLIFIED_CHINESE;
            LocaleContextHolder.setLocale(locale);
            assertThat(LocaleContextHolder.getLocale()).isEqualTo(locale);
        }

        @Test
        @DisplayName("Given locale with en_US then return the same locale")
        void givenLocaleContextWithEnUSThenReturnSameLocaleContext() {
            Locale locale = Locale.US;
            LocaleContextHolder.setLocale(locale);
            assertThat(LocaleContextHolder.getLocale()).isEqualTo(locale);
        }

        @Test
        @DisplayName("Given null locale then not set and return null")
        void givenNullLocaleContextThenReturnNull() {
            LocaleContextHolder.setLocale(null);
            assertThat(LocaleContextHolder.getLocale()).isNull();
        }
    }

    @Nested
    @DisplayName("Test method: clear")
    class TestClear {
        @Test
        @DisplayName("Given existing locale then clear it")
        void givenExistingLocaleContextThenClearIt() {
            Locale locale = Locale.SIMPLIFIED_CHINESE;
            LocaleContextHolder.setLocale(locale);
            assertThat(LocaleContextHolder.getLocale()).isNotNull();

            LocaleContextHolder.clear();
            assertThat(LocaleContextHolder.getLocale()).isNull();
        }
    }
}