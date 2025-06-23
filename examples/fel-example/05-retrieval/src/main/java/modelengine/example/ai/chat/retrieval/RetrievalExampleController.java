/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.example.ai.chat.retrieval;

import static modelengine.fel.engine.operators.patterns.SyncTipper.DEFAULT_HISTORY_KEY;
import static modelengine.fel.engine.operators.patterns.SyncTipper.history;
import static modelengine.fel.engine.operators.patterns.SyncTipper.passThrough;
import static modelengine.fel.engine.operators.patterns.SyncTipper.value;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatModel;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.support.HumanMessage;
import modelengine.fel.core.document.Content;
import modelengine.fel.core.document.Document;
import modelengine.fel.core.document.DocumentEmbedModel;
import modelengine.fel.core.embed.EmbedModel;
import modelengine.fel.core.embed.EmbedOption;
import modelengine.fel.core.embed.support.DefaultDocumentEmbedModel;
import modelengine.fel.core.memory.Memory;
import modelengine.fel.core.memory.support.CacheMemory;
import modelengine.fel.core.source.support.JsonFileSource;
import modelengine.fel.core.template.StringTemplate;
import modelengine.fel.core.util.Tip;
import modelengine.fel.core.vectorstore.SearchOption;
import modelengine.fel.core.vectorstore.VectorStore;
import modelengine.fel.core.vectorstore.support.DefaultVectorRetriever;
import modelengine.fel.core.vectorstore.support.MemoryVectorStore;
import modelengine.fel.engine.flows.AiFlows;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.operators.models.ChatFlowModel;
import modelengine.fel.engine.operators.prompts.Prompts;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.annotation.RequestParam;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.FileUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 样例控制器。
 *
 * @author 易文渊
 * @since 2024-09-02
 */
@Component
@RequestMapping("/ai/example")
public class RetrievalExampleController {
    private static final String REWRITE_PROMPT =
            "作为一个向量检索助手，你的任务是结合历史记录，为”原问题“生成”检索词“，" + "生成的问题要求指向对象清晰明确，并与“原问题语言相同。\n\n"
                    + "历史记录：\n---\n" + DEFAULT_HISTORY_KEY + "---\n原问题：{{query}}\n检索词：";
    private static final String CHAT_PROMPT = "Answer the question based on the context below. "
            + "If the question cannot be answered using the information provided answer with \"I don't know\".\n\n"
            + "Context: {{context}}\n\n" + "Question: {{query}}\n\n" + "Answer: ";
    private final AiProcessFlow<String, ChatMessage> ragFlow;
    private final Memory memory = new CacheMemory();

    public RetrievalExampleController(ChatModel chatModel, EmbedModel embedModel,
            @Fit(alias = "json") ObjectSerializer serializer, @Value("${example.model.chat}") String chatModelName,
            @Value("${example.model.embed}") String embedModelName) {
        DocumentEmbedModel documentEmbedModel =
                new DefaultDocumentEmbedModel(embedModel, EmbedOption.custom().model(embedModelName).build());
        VectorStore vectorStore = new MemoryVectorStore(documentEmbedModel);
        ChatFlowModel chatFlowModel =
                new ChatFlowModel(chatModel, ChatOption.custom().model(chatModelName).stream(false).build());

        AiProcessFlow<Tip, Content> retrieveFlow = AiFlows.<Tip>create()
                .runnableParallel(history(), passThrough())
                .conditions()
                .match(tip -> !tip.freeze().get(DEFAULT_HISTORY_KEY).text().isEmpty(),
                        node -> node.prompt(Prompts.human(REWRITE_PROMPT))
                                .generate(chatFlowModel)
                                .map(ChatMessage::text))
                .others(node -> node.map(tip -> tip.freeze().get("query").text()))
                .retrieve(new DefaultVectorRetriever(vectorStore, SearchOption.custom().topK(1).build()))
                .synthesize(docs -> Content.from(docs.stream().map(Document::text).collect(Collectors.joining("\n\n"))))
                .close();

        AiProcessFlow<File, List<Document>> indexFlow = AiFlows.<File>create()
                .load(new JsonFileSource(serializer, StringTemplate.create("{{question}}: {{answer}}")))
                .index(vectorStore)
                .close();
        File file = FileUtils.file(this.getClass().getClassLoader().getResource("data.json"));
        notNull(file, "The data cannot be null.");
        indexFlow.converse().offer(file);

        this.ragFlow = AiFlows.<String>create()
                .map(query -> Tip.from("query", query))
                .runnableParallel(value("context", retrieveFlow), passThrough())
                .prompt(Prompts.history(), Prompts.human(CHAT_PROMPT))
                .generate(chatFlowModel)
                .close();
    }

    /**
     * 聊天接口。
     *
     * @param query 表示用户输入查询的 {@link String}。
     * @return 表示聊天模型生成的回复的 {@link ChatMessage}。
     */
    @GetMapping("/chat")
    public ChatMessage chat(@RequestParam("query") String query) {
        ChatMessage aiMessage = this.ragFlow.converse().offer(query).await();
        this.memory.add(new HumanMessage(query));
        this.memory.add(aiMessage);
        return aiMessage;
    }
}