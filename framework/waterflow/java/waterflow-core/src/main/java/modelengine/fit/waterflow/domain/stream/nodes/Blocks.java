/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.nodes;

import lombok.Getter;
import lombok.Setter;
import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.enums.ProcessType;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.utils.IdGenerator;

import java.util.List;
import java.util.Optional;

/**
 * stream操作过程中需要中断的地方设置block，stream在处理到该节点的时候会停住，等待外部驱动继续执行
 * block的核心是一个validator或者filter，在驱动的时候会选择符合条件的数据进入处理
 * 辉子 2019-11-20
 *
 * @author 高诗意
 * @since 1.0
 */
public class Blocks {
    /**
     * Block
     *
     * @since 1.0
     */
    public abstract static class Block<I> extends IdGenerator {
        /**
         * 需要中断的目标节点
         */
        @Getter
        @Setter
        private To<I, ?> target;

        /**
         * 外部触发执行目标节点中某一个流程实例进行数据处理
         *
         * @param contexts 待处理的contexts列表
         */
        public void process(List<FlowContext<I>> contexts) {
            this.target.accept(ProcessType.PROCESS, contexts);
        }
    }

    /**
     * 用于MAP的block
     * 有能力validate某个item是否满足条件
     * 辉子 2019-11-21
     *
     * @param <I> 输入数据类型
     */
    public static class ValidatorBlock<I> extends Block<I> {
        /**
         * 单项数据的validator，block在被触发的时候调用该validator过滤数据
         */
        private final Operators.Validator<I> validator;

        /**
         * 空validator构造器，其实就是默认全部通过验证
         */
        public ValidatorBlock() {
            this(null);
        }

        /**
         * 用户在设置时将validator传入block，但不会传入相应的节点，只会在调用process的时候传入节点
         *
         * @param validator 验证器
         */
        public ValidatorBlock(Operators.Validator<I> validator) {
            this.validator = validator;
        }

        /**
         * 在外部触发block执行的时候将validator传入对应的节点
         *
         * @param contexts 待处理的contexts列表
         */
        @Override
        public void process(List<FlowContext<I>> contexts) {
            if (this.validator != null) {
                this.getTarget().setValidator(this.validator);
            }
            super.process(contexts);
        }
    }

    /**
     * FilterBlock
     *
     * @param <I> 输入数据类型
     * @since 1.0
     */
    public static class FilterBlock<I> extends Block<I> {
        private final Operators.Filter<I> filter;

        /**
         * 空filter构造器，其实就是默认全部通过验证
         */
        public FilterBlock() {
            this(null);
        }

        /**
         * 用户在设置时将filter传入block，但不会传入相应的节点，只会在调用process的时候传入节点
         *
         * @param filter 过滤器
         */
        public FilterBlock(Operators.Filter<I> filter) {
            this.filter = filter;
        }

        /**
         * 在外部触发block执行的时候将validator传入对应的节点
         *
         * @param contexts 待处理的contexts列表
         */
        @Override
        public void process(List<FlowContext<I>> contexts) {
            Optional.ofNullable(this.filter).ifPresent(filter -> this.getTarget().postFilter(filter));
            super.process(contexts);
        }
    }
}
