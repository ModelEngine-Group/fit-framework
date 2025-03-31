/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * value容器，用于避免value为null或 undefined，参考JAVA - Optional.
 */
export default class Optional {
    _value;

    constructor(value) {
        this._value = value;
    }

    /**
     * 通过value构造一个Optional对象.
     *
     * @param value 值.
     * @return {Optional}
     */
    static of(value) {
        return new Optional(value);
    }

    /**
     * 是否present.
     *
     * @return {boolean} 若为false，表明是null或undefined.
     */
    isPresent() {
        return this._value !== undefined && this._value !== null;
    }

    /**
     * 如果value存在，则调用consumer进行调用，否则，不进行任何操作.
     *
     * @param consumer value存在时的消费者.
     */
    ifPresent(consumer) {
        Optional._requireExists(consumer);
        if (!this.isPresent()) {
            return;
        }
        consumer(this._value);
    }

    /**
     * 如果value存在，则调用consumer进行调用，否则，不进行任何操作.
     *
     * @param presentAction value存在时的处理逻辑.
     * @param elseAction 不存在时的处理逻辑.
     */
    ifPresentOrElse(presentAction, elseAction) {
        Optional._requireExists(presentAction);
        if (this.isPresent()) {
            presentAction(this._value);
        } else {
            elseAction();
        }
    }

    /**
     * 获取value，若不存在，则抛出异常.
     *
     * @return {*}
     */
    get() {
        if (!this.isPresent()) {
            throw new Error("No value present.");
        }
        return this._value;
    }

    /**
     * 若value不存在，则返回other.
     *
     * @param other value不存在时需要返回的值.
     * @return {*}
     */
    orElse(other) {
        return this.isPresent() ? this._value : other;
    }

    /**
     * 提供一个supplier，如果非present的话，就返回supplier提供的值.
     *
     * @param supplier 供应方法.
     * @return {*}
     */
    orElseGet(supplier) {
        return this.isPresent() ? this._value : supplier();
    }

    /**
     * 如果值存在，则返回值；否则，执行传入的异常逻辑.
     *
     * @param exceptionSupplier 异常提供者.
     * @return {*}
     */
    orElseThrow(exceptionSupplier) {
        if (this.isPresent()) {
            return this._value;
        }
        throw exceptionSupplier();
    }

    /**
     * 转换操作.
     *
     * @param mapper 转换函数.
     * @return {Optional}
     */
    map(mapper) {
        Optional._requireExists(mapper);
        return this.isPresent() ? Optional.of(mapper(this._value)) : Optional.of(null);
    }

    static _requireExists(obj) {
        if (obj === undefined || obj === null) {
            throw new Error("Object is null or undefined.");
        }
        return obj;
    }
}