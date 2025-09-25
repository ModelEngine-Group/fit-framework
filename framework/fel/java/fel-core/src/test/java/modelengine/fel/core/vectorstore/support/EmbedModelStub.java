/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.vectorstore.support;

import modelengine.fel.core.document.Document;
import modelengine.fel.core.embed.EmbedModel;
import modelengine.fel.core.embed.EmbedOption;
import modelengine.fel.core.embed.Embedding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 嵌入模型服务的打桩实现。
 *
 * @author 易文渊
 * @since 2024-08-08
 */
class EmbedModelStub implements EmbedModel {
    static final List<List<Float>> embeddings = Arrays.asList(Arrays.asList(-0.46710945845655893f,
                    -0.6954407380788394f,
                    -0.8645940546707891f,
                    0.9785210304628436f,
                    -0.38582605291132266f),
            Arrays.asList(-0.02982274214112124f,
                    0.30931837132023565f,
                    -0.30617613535485155f,
                    0.1502844479511305f,
                    0.027540763338194996f),
            Arrays.asList(0.7994257882706344f,
                    -0.4525328395876058f,
                    -0.7109598455175881f,
                    -0.010737380614887604f,
                    -0.20469081150028723f));

    private final AtomicInteger idx = new AtomicInteger(0);

    static List<Document> generateTestDocuments() {
        return IntStream.range(0, embeddings.size())
                .mapToObj(seq -> Document.custom().text("test" + seq).metadata(Collections.emptyMap()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Embedding> generate(List<String> inputs, EmbedOption ignored) {
        return inputs.stream().map(input -> getNextEmbedding()).collect(Collectors.toList());
    }

    private Embedding getNextEmbedding() {
        return () -> embeddings.get(idx.getAndIncrement() % embeddings.size());
    }
}