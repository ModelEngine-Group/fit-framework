/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

/**
 * 用于计数session window中处理的数据
 * 至于session window中素有的数据都consumed，该session window才算结束
 * session window的解释，业务表达为某session数据在某节点处理完毕
 *
 * @author songyongtan
 * @since 1.0
 */
public class WindowToken {
    /**
     * 状态枚举
     */
    enum Status {
        /**
         * 初始化
         */
        INITIALIZED,

        /**
         * 正在处理
         */
        CONSUMING,

        /**
         * 已处理完成
         */
        CONSUMED
    }

    private final Window window;

    /**
     * 状态
     *
     * @return 状态
     */
    public Status getStatus() {
        return this.status;
    }

    private Status status = Status.INITIALIZED;

    private boolean reduced;

    /**
     * 构造函数
     *
     * @param window 窗口
     */
    public WindowToken(Window window) {
        this.window = window;
    }

    /**
     * 结束消费
     */
    public void finishConsume() {
        if (this.status == Status.CONSUMED) {
            return;
        }
        this.status = Status.CONSUMED;
    }

    /**
     * 开始消费
     */
    public void beginConsume() {
        this.status = Status.CONSUMING;
    }

    /**
     * 是否为初始化状态
     *
     * @return 是
     */
    public boolean initialized() {
        return this.status == Status.INITIALIZED;
    }

    /**
     * 是否正在消费
     *
     * @return 是
     */
    public boolean isConsuming() {
        return this.status == Status.CONSUMING;
    }

    /**
     * 是否被消费过了
     *
     * @return 是
     */
    public boolean isConsumed() {
        return this.status == Status.CONSUMED;
    }

    /**
     * 是否reduce过了
     *
     * @return true，是
     */
    public boolean isReduced() {
        return this.reduced;
    }

    /**
     * 聚合
     */
    public void reduce() {
        if (this.status != Status.INITIALIZED && !this.reduced) {
            this.reduced = true;
        }
    }

    /**
     * token结束
     */
    public void accepted() {
        this.window.tryFinish();
    }
}
