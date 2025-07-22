/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.heartbeat;

import modelengine.fitframework.annotation.Genericable;

import java.util.List;

/**
 * 远端心跳服务的客户端。
 *
 * @author 季聿阶
 * @since 2023-05-06
 */
public interface HeartbeatService {
    /**
     * 发送心跳信息。
     *
     * @param heartbeatInfo 表示心跳信息列表的 {@link List}{@code <}{@link HeartbeatInfo}{@code >}。
     * @param address 表示本地地址的 {@link Address}。
     * @return 表示发送结果的 {@link Boolean}。
     */
    @Genericable(id = "modelengine.fit.heartbeat.send-heartbeat")
    Boolean sendHeartbeat(List<HeartbeatInfo> heartbeatInfo, Address address);

    /**
     * 发送停止心跳信息。
     *
     * @param heartbeatInfo 表示待停止心跳信息列表的 {@link List}{@code <}{@link HeartbeatInfo}{@code >}。
     * @param address 表示本地地址的 {@link Address}。
     * @return 表示发送结果的 {@link Boolean}。
     */
    @Genericable(id = "modelengine.fit.heartbeat.stop-heartbeat")
    Boolean stopHeartbeat(List<HeartbeatInfo> heartbeatInfo, Address address);

    /**
     * 表示心跳信息。
     */
    class HeartbeatInfo {
        private String sceneType;
        private Long aliveTime;
        private Long initDelay;

        /**
         * 获取心跳场景。
         *
         * @return 表示心跳场景的 {@link String}。
         */
        public String getSceneType() {
            return this.sceneType;
        }

        /**
         * 设置心跳场景。
         *
         * @param sceneType 表示心跳场景的 {@link String}。
         */
        public void setSceneType(String sceneType) {
            this.sceneType = sceneType;
        }

        /**
         * 获取存活时间，单位为毫秒。
         *
         * @return 表示存活时间的 {@link Long}。
         */
        public Long getAliveTime() {
            return this.aliveTime;
        }

        /**
         * 设置存活时间，单位为毫秒。
         *
         * @param aliveTime 表示存活时间的 {@link Long}。
         */
        public void setAliveTime(Long aliveTime) {
            this.aliveTime = aliveTime;
        }

        /**
         * 表示初始延迟时间，单位为毫秒。
         *
         * @return 表示初始延迟时间的 {@link Long}。
         */
        public Long getInitDelay() {
            return this.initDelay;
        }

        /**
         * 设置初始延迟时间，单位为毫秒。
         *
         * @param initDelay 表示初始延迟时间的 {@link Long}。
         */
        public void setInitDelay(Long initDelay) {
            this.initDelay = initDelay;
        }
    }

    /**
     * 表示地址信息。
     *
     * @author 季聿阶
     * @since 2023-05-06
     */
    class Address {
        private String id;

        /**
         * 获取地址的唯一标识。
         *
         * @return 表示地址的唯一标识的 {@link String}。
         */
        public String getId() {
            return this.id;
        }

        /**
         * 设置地址的唯一标识。
         *
         * @param id 表示地址的唯一标识的 {@link String}。
         */
        public void setId(String id) {
            this.id = id;
        }
    }
}
