/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.header;

import modelengine.fit.http.Cookie;
import modelengine.fit.http.support.DefaultCookieCollection;

/**
 * 表示 Http 中的可读可写的 Cookie 集合。
 *
 * @author 季聿阶
 * @since 2022-07-07
 */
public interface ConfigurableCookieCollection extends CookieCollection {
    /**
     * 添加一个指定的 {@link Cookie}。
     *
     * @param cookie 表示待添加的 {@link Cookie}。
     */
    void add(Cookie cookie);

    /**
     * 创建一个新的可读可写的 Cookie 集合。
     *
     * @return 表示创建出来的可读可写的 Cookie 集合的 {@link ConfigurableCookieCollection}。
     */
    static ConfigurableCookieCollection create() {
        return new DefaultCookieCollection();
    }
}
