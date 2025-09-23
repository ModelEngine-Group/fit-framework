/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util.support;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 为数组提供迭代器。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class ArrayIterator<T> implements Iterator<T> {
    private final T[] items;
    private int index;

    /**
     * 使用包含原始数据的数组初始化 {@link ArrayIterator} 类的新实例。
     *
     * @param items 表示作为原始数据的数组。
     */
    public ArrayIterator(T[] items) {
        this.items = items;
        this.index = -1;
    }

    @Override
    public boolean hasNext() {
        if (this.items == null) {
            return false;
        }
        return this.index < this.items.length - 1;
    }

    @Override
    public T next() {
        if (this.hasNext()) {
            return this.items[++this.index];
        } else {
            throw new NoSuchElementException();
        }
    }
}
