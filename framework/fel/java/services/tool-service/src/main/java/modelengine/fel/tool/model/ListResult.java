/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.model;

import java.util.List;

/**
 * 适配查询工具返回工具总数的包装类。
 *
 * @author 鲁为
 * @since 2024-06-13
 */
public class ListResult<T> {
    private List<T> data;

    private int count;

    public ListResult(List<T> data, int count) {
        this.data = data;
        this.count = count;
    }

    /**
     * 获取数据。
     *
     * @return 表示数据的 {@link List}{@code <}{@link T}{@code >}。
     */
    public List<T> getData() {
        return this.data;
    }

    /**
     * 插入数据。
     *
     * @param data 表示数据的 {@link List}{@code <}{@link T}{@code >}。
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * 获取数据的个数。
     *
     * @return 表示数据个数的 {@code int}。
     */
    public int getCount() {
        return this.count;
    }

    /**
     * 插入数据。
     *
     * @param count 表示数据个数的 {@code int}。
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 创建一个数据对象的包装类。
     *
     * @param data 表示数据对象的 {@link T}。
     * @param count 表示状态码的 {@code int}。
     * @param <T> 表示数据对象的类型的 {@link T}。
     * @return 表示创建出来的数据对象的包装类的 {@link ListResult}{@code <}{@link T}{@code >}。
     */
    public static <T> ListResult<T> create(List<T> data, int count) {
        return new ListResult<>(data, count);
    }

    /**
     * 创建一个数据对象的包装类的空实例。
     *
     * @param <T> 表示数据对象的类型的 {@link T}。
     * @return 表示创建出来的数据对象的包装类的 {@link ListResult}{@code <}{@link T}{@code >}。
     */
    public static <T> ListResult<T> empty() {
        return new ListResult<>(null, 0);
    }
}
