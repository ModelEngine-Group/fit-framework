/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 创建处理器.
 *
 * @param shape 图形对象.
 * @return {{}} 处理器对象.
 */
export const createProcessor = (shape) => {
    if (shape.isTypeof('jadeEvent')) {
        return eventProcessor(shape);
    }

    // 评估节点和新增的普通节点逻辑一致.
    if (shape.page.isEvaluationNode(shape)) {
        return evaluationProcessor(shape);
    }

    if (shape.isTypeof('conditionNodeCondition')) {
        return conditionProcessor(shape);
    }

    return normalProcessor(shape);
};

/**
 * 条件节点处理器.
 *
 * @param shape 图形节点.
 * @return {{}}
 */
const conditionProcessor = (shape) => {
    const self = {};
    self.isUpdated = false;
    const updatedBranches = [];

    /**
     * 检查runnable状态.
     *
     * @param runnableFlow runnable流.
     */
    self.setRunnable = (runnableFlow) => {
        const runnable = runnableFlow.has(shape.id);
        shape.statusManager.setRunnable(runnable);
        shape.statusManager.setEnableMask(!runnable);
        shape.statusManager.setReferenceDisabled(!runnable);
        shape.getBranches().forEach((b, index) => {
            const events = shape.getEventsByBranchId(b.id);
            const prevRunnable = b.runnable;
            const realRunnable = events.length > 0 && events.some(e => runnableFlow.has(e.id));
            if (prevRunnable !== realRunnable) {
                self.isUpdated = true;
                updatedBranches.push({id: b.id, index: index, runnable: realRunnable});
            }
        });
    };

    /**
     * runnable状态发生变化时的处理逻辑.
     *
     * @param page 页面对象.
     */
    self.process = (page) => {
        const flowMeta = shape.getFlowMeta();
        flowMeta.conditionParams.branches = flowMeta.conditionParams.branches.map((b, index) => {
            const updatedBranch = updatedBranches.find(ub => ub.index === index);
            if (updatedBranch) {
                return branchProcessor(shape, b, updatedBranch.runnable, index).process(page);
            } else {
                return b;
            }
        });
        onShapeRunnableSwitch(shape, flowMeta);
    };

    return self;
};

/**
 * 分支处理器.
 */
const branchProcessor = (shape, branch, runnable, index) => {
    const self = {};

    /**
     * runnable状态发生变化时的处理逻辑.
     *
     * @param page 页面对象.
     */
    self.process = (page) => {
        return runnable ? self.modify(page) : self.revert(page);
    };

    /**
     * runnable false --> true.
     *
     * @param page 页面.
     */
    self.modify = (page) => {
        const b = page.latestFlowMetas[shape.id].conditionParams.branches[index];
        b.runnable = runnable;
        b.disabled = false;
        return b;
    };

    /**
     * runnable true --> false.
     *
     * @param page 页面.
     */
    self.revert = (page) => {
        page.latestFlowMetas[shape.id].conditionParams.branches[index] = branch;
        const b = {...page.flowMetas[shape.id].conditionParams.branches[index]};
        b.disabled = true;
        return b;
    };

    return self;
};

/**
 * 普通节点处理器.
 *
 * @param shape 图形节点.
 * @return {{}}
 */
const normalProcessor = (shape) => {
    const self = {};
    self.isUpdated = false;
    self.runnable = shape.runnable;

    /**
     * 检查runnable状态.
     *
     * @param runnableFlow runnable流.
     */
    self.setRunnable = (runnableFlow) => {
        const prevRunnable = self.runnable;
        const runnable = runnableFlow.has(shape.id);
        shape.statusManager.setRunnable(runnable);
        shape.statusManager.setEnableMask(!runnable);
        shape.statusManager.setReferenceDisabled(!runnable);
        self.isUpdated = prevRunnable !== runnable;
    };

    /**
     * runnable状态发生变化时的处理逻辑.
     *
     * @param page 页面对象.
     */
    self.process = (page) => {
        onShapeRunnableSwitch(shape, shape.runnable ? self.modify(page) : self.revert(page));
    };

    /**
     * runnable false --> true.
     *
     * @param page 页面.
     */
    self.modify = (page) => {
        return page.latestFlowMetas[shape.id];
    };

    /**
     * runnable true --> false.
     *
     * @param page 页面.
     */
    self.revert = (page) => {
        // 这里必须深拷贝，否则会导致进入流程-修改引用-出流程-再进流程，修改的引用失效.
        page.latestFlowMetas[shape.id] = JSON.parse(JSON.stringify(shape.getFlowMeta()));
        return page.flowMetas[shape.id];
    };

    return self;
};

/**
 * 线处理器.
 *
 * @param shape 图形节点.
 * @return {{}}
 */
const eventProcessor = (shape) => {
    const self = {};

    /**
     * 检查runnable状态.
     *
     * @param runnableFlow runnable流.
     */
    self.setRunnable = (runnableFlow) => {
        shape.runnable = runnableFlow.has(shape.id);
    };

    /**
     * 空实现.
     */
    self.process = () => {
    };

    return self;
};

/**
 * 评估节点处理器.
 *
 * @param shape 图形节点.
 * @return {{}}
 */
const evaluationProcessor = (shape) => {
    const self = {};

    /**
     * 检查runnable状态.
     *
     * @param runnableFlow runnable流.
     */
    self.setRunnable = (runnableFlow) => {
        shape.statusManager.setRunnable(runnableFlow.has(shape.id));
    };

    /**
     * 空实现.
     */
    self.process = () => {
    };

    return self;
};

const onShapeRunnableSwitch = (s, flowMeta) => {
    if (flowMeta) {
        s.cleanObserved();
        s.setFlowMeta(flowMeta);
    } else {
        // flowMeta不存在，说明是第一次从非runnable状态切换到runnable状态，此时disable所有reference即可.
        s.disableObserved();
    }
};