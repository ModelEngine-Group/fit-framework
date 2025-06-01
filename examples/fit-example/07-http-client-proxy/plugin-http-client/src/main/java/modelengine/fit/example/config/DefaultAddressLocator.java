/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.config;

import modelengine.fit.http.client.proxy.scanner.AddressLocator;
import modelengine.fit.http.client.proxy.scanner.entity.Address;
import modelengine.fitframework.annotation.Component;

/**
 * Provides a default implementation of the AddressLocator interface.
 * This class is responsible for returning a default address configuration for HTTP requests.
 * The default address includes the protocol (http), host (localhost), and port (8080).
 *
 * @author 季聿阶
 * @since 2025-06-01
 */
@Component
public class DefaultAddressLocator implements AddressLocator {
    @Override
    public Address address() {
        Address address = new Address();
        address.setProtocol("http");
        address.setHost("localhost");
        address.setPort(8080);
        return address;
    }
}