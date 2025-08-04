/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.conf.runtime;

import modelengine.fitframework.util.StringUtils;

/**
 * Represents the default implementation of {@link MatataConfig.Registry.Nacos}.
 *
 * @author 董智豪
 * @since 2025-08-01
 */
public class DefaultNacos implements MatataConfig.Registry.Nacos {
    private String username;
    private String password;
    private String accessKey;
    private String secretKey;
    private Boolean isEphemeral = true;
    private Float weight = 1.0F;
    private Long heartbeatInterval;
    private Long heartbeatTimeout;

    /**
     * Sets the username configuration for Nacos authentication.
     *
     * @param username The {@link String} representing the username to be set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the password configuration for Nacos authentication.
     *
     * @param password The {@link String} representing the password to be set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the access key configuration for Nacos authentication.
     *
     * @param accessKey The {@link String} representing the access key to be set.
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * Sets the secret key configuration for Nacos authentication.
     *
     * @param secretKey The {@link String} representing the secret key to be set.
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Sets whether the instance is ephemeral.
     *
     * @param isEphemeral The {@link Boolean} representing whether the instance is ephemeral.
     */
    public void setIsEphemeral(Boolean isEphemeral) {
        this.isEphemeral = isEphemeral;
    }

    /**
     * Sets the service weight configuration.
     *
     * @param weight The {@link Float} representing the service weight to be set.
     */
    public void setWeight(Float weight) {
        this.weight = weight;
    }

    /**
     * Sets the heartbeat interval configuration.
     *
     * @param heartbeatInterval The {@link Long} representing the heartbeat interval to be set.
     */
    public void setHeartbeatInterval(Long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    /**
     * Sets the heartbeat timeout configuration.
     *
     * @param heartbeatTimeout The {@link Long} representing the heartbeat timeout to be set.
     */
    public void setHeartbeatTimeout(Long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public String password() {
        return this.password;
    }

    @Override
    public String accessKey() {
        return this.accessKey;
    }

    @Override
    public String secretKey() {
        return this.secretKey;
    }

    @Override
    public Boolean isEphemeral() {
        return this.isEphemeral;
    }

    @Override
    public Float weight() {
        return this.weight;
    }

    @Override
    public Long heartbeatInterval() {
        return this.heartbeatInterval;
    }

    @Override
    public Long heartbeatTimeout() {
        return this.heartbeatTimeout;
    }

    @Override
    public String toString() {
        return StringUtils.format("/{\"username\": \"{0}\", \"password\": \"{1}\", "
                        + "\"access-key\": \"{2}\", \"secret-key\": \"{3}\", "
                        + "\"is-ephemeral\": {4}, \"weight\": {5}, \"heartbeat-interval\": {6}, "
                        + "\"heartbeat-timeout\": {7}/}",
                this.username,
                this.password,
                this.accessKey,
                this.secretKey,
                this.isEphemeral,
                this.weight,
                this.heartbeatInterval,
                this.heartbeatTimeout);
    }
}
