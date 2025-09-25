/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.util;

import modelengine.fitframework.inspection.Validation;

import java.util.List;

/**
 * 提供数学相关的工具方法。
 *
 * @author 易文渊
 * @since 2024-08-06
 */
public final class MathUtils {
    private MathUtils() {}

    /**
     * 计算两个向量的余弦相似度。
     *
     * @param x 表示第一个向量的 {@link List}{@code <}{@link Float}{@code >}。
     * @param y 表示第二个向量的 {@link List}{@code <}{@link Float}{@code >}。
     * @return 表示两个向量余弦相似度的 {@code double}。
     */
    public static double cosineSimilarity(List<Float> x, List<Float> y) {
        Validation.isTrue(x != null && y != null, "The vector cannot be null.");
        Validation.equals(x.size(), y.size(), "The vector sharpe cannot be equal.");
        double dotProduct = 0.0d;
        double normX = 0.0d;
        double normY = 0.0d;
        for (int i = 0; i < x.size(); i++) {
            dotProduct += x.get(i) * y.get(i);
            normX += x.get(i) * x.get(i);
            normY += y.get(i) * y.get(i);
        }
        double result = dotProduct / (Math.sqrt(normX) * Math.sqrt(normY));
        Validation.isFalse(Double.isNaN(result), "The result is NaN.");
        return result;
    }
}