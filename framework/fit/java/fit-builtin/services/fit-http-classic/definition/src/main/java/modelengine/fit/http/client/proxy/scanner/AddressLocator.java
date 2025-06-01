/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner;

import modelengine.fit.http.client.proxy.scanner.entity.Address;

/**
 * Defines a contract for locating and providing address information for HTTP requests.
 * Implementations of this interface are responsible for returning an {@link Address} object
 * that contains details such as protocol, host, port, and locator class.
 *
 * @author 王攀博
 * @since 2025-01-24
 */
public interface AddressLocator {
    /**
     * Retrieves the address information for an HTTP request.
     *
     * @return The address information as an {@link Address} object.
     */
    Address address();
}