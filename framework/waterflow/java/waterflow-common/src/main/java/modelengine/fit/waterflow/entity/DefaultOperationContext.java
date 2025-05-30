/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.entity;

import modelengine.fitframework.util.StringUtils;

import java.util.Arrays;

import static modelengine.fitframework.util.ObjectUtils.nullIf;

/**
 * 操作上下文。
 *
 * @author 梁济时
 * @since 2023-10-30
 */
class DefaultOperationContext implements OperationContext {
    static final DefaultOperationContext EMPTY = new DefaultOperationContext("", "",
            "0.0.0.0", "", "");

    private final String tenantId;

    private final String operator;

    private final String operatorIp;

    private final String language;

    private final String sourcePlatform;

    DefaultOperationContext(String tenantId, String operator, String operatorIp, String language,
                            String sourcePlatform) {
        this.tenantId = nullIf(tenantId, "");
        this.operator = nullIf(operator, "");
        this.operatorIp = nullIf(operatorIp, "");
        this.language = nullIf(language, "");
        this.sourcePlatform = nullIf(sourcePlatform, "");
    }

    @Override
    public String tenantId() {
        return this.tenantId;
    }

    @Override
    public String operator() {
        return this.operator;
    }

    @Override
    public String operatorIp() {
        return this.operatorIp;
    }

    @Override
    public String language() {
        return this.language;
    }

    @Override
    public String sourcePlatform() {
        return this.sourcePlatform;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof DefaultOperationContext) {
            DefaultOperationContext another = (DefaultOperationContext) obj;
            return this.tenantId.equals(another.tenantId) && this.operator.equals(another.operator)
                    && this.operatorIp.equals(another.operatorIp);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {this.getClass(), this.tenantId, this.operator, this.operatorIp});
    }

    @Override
    public String toString() {
        return StringUtils.format("[tenantId={0}, operator={1}, operatorIp={2}]", this.tenantId, this.operator,
                this.operatorIp);
    }

    static class Builder implements OperationContext.Builder {
        private String tenantId;

        private String operator;

        private String operatorIp;

        private String language;

        private String sourcePlatform;

        @Override
        public OperationContext.Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        @Override
        public OperationContext.Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        @Override
        public OperationContext.Builder operatorIp(String operatorIp) {
            this.operatorIp = operatorIp;
            return this;
        }

        @Override
        public OperationContext.Builder langage(String language) {
            this.language = language;
            return this;
        }

        @Override
        public OperationContext.Builder sourcePlatform(String sourcePlatform) {
            this.sourcePlatform = sourcePlatform;
            return this;
        }

        @Override
        public OperationContext build() {
            return new DefaultOperationContext(this.tenantId, this.operator, this.operatorIp, this.language,
                    this.sourcePlatform);
        }
    }
}
