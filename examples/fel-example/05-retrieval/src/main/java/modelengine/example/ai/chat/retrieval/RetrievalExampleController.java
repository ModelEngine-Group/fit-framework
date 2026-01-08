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
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.serialization.ObjectSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private static final Logger log = Logger.get(RetrievalExampleController.class);
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
                .close(__ -> log.info("Retrieve flow completed."));

        AiProcessFlow<File, List<Document>> indexFlow = AiFlows.<File>create()
                .load(new JsonFileSource(serializer, StringTemplate.create("{{question}}: {{answer}}")))
                .index(vectorStore)
                .close();
        File file = getData();
        notNull(file, "The data cannot be null.");
        indexFlow.converse()
                .doOnError(e -> log.info("Index build error. [error={}]", e.getMessage(), e))
                .doOnFinally(() -> log.info("Index build successfully."))
                .offer(file);

        this.ragFlow = AiFlows.<String>create()
                .just(query -> log.info("RAG flow start. [query={}]", query))
                .map(query -> Tip.from("query", query))
                .runnableParallel(value("context", retrieveFlow), passThrough())
                .prompt(Prompts.history(), Prompts.human(CHAT_PROMPT))
                .just(__ -> log.info("LLM start generation."))
                .generate(chatFlowModel)
                .close(__ -> log.info("RAG flow completed."));
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

    private static File getData() {
        String jsonData = "[\n" +
                "  {\n" +
                "    \"question\": \"黑神话悟空简介\",\n" +
                "    \"answer\": \"《黑神话：悟空》是由杭州游科互动科技有限公司开发，浙江出版集团数字传媒有限公司出版的西游题材单机动作角色扮演游戏。\\n该作以中国四大名著之一、吴承恩所著长篇小说《西游记》为背景设定，借用《西游记》自身的故事作为前传，讲述取经之后孙悟空因放弃佛位引发天庭对其再次征伐之后的故事。在游戏中，玩家将扮演一位\\u201c天命人\\u201d，为了探寻昔日传说的真相，踏上一条充满危险与惊奇的西游之路。\\n2024年8月20日，该作正式登陆PC、PS5平台。该作通常被媒体称为\\u201c中国首款3A游戏\\u201d。在发售三天后，该作的全平台销量已超过1000万套，打破中国游戏历史记录。\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"黑神话悟空故事情节\",\n" +
                "    \"answer\": \"《黑神话：悟空》的故事情节发生在《西游记》故事之后。在老猴子的说书故事中，相传据说孙悟空成佛后依旧不愿受到拘束，待唐僧东归后便辞去佛位，欲回到花果山逍遥快活，未料遭到天庭猜忌，二郎神、四大天王、巨灵神率领天兵天将再次杀奔花果山。孙悟空与二郎神激战时，本已消失的紧箍咒再次出现，孙悟空不敌二郎神，就此殒命，肉身残躯化为巨石，魂魄分为六件根器，名为六根，隐藏世间。多年后，玩家扮演的花果山猿猴\\u201c天命人\\u201d踏上了寻找遗失根器、解救和复活孙悟空的旅程。\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"黑神话悟空结局\",\n" +
                "    \"answer\": \"1. 一般结局：\\u201c天命人\\u201d未能继承孙悟空的遗产，自愿选择戴上曾束缚孙悟空的紧箍儿，与孙悟空的残躯融合，圆满完成了\\u201c天命人\\u201d的任务，但孙悟空并未真正复活，\\u201c天命人\\u201d彻底沦为天庭执行意志的新工具。\\n2. 真正结局：\\u201c天命人\\u201d在\\u201c未来佛\\u201d弥勒菩萨的帮助下前往梅山探寻真相，杀死四大天王并打败了二郎神杨戬，通过了杨戬的考验。杨戬的\\u201c第三眼\\u201d释放出孙悟空的神识，这里面有孙悟空的记忆，杨戬将这些封存的记忆交给\\u201c天命人\\u201d，\\u201c天命人\\u201d成功继承孙悟空的意志，并在花果山的顶峰击败孙悟空的残躯，六根齐聚，孙悟空的\\u201c意\\u201d获得真正的觉醒，在此刻，\\u201c天命人\\u201d与孙悟空合二为一，孙悟空成功复活，拒绝戴上紧箍儿，从而避免天庭的束缚。\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"黑神话悟空玩法\",\n" +
                "    \"answer\": \"游戏的设计灵感源于中国古典神怪小说《西游记》，玩家将操控一位名为\\u201c天命人\\u201d的花果山猿猴。主角的主要武器是如意金箍棒，能在战斗中伸缩自如。游戏的战斗系统包括三种主要棍法：劈棍、戳棍和立棍，为战斗提供多样化选择。战斗还涉及资源管理，当计量表充满时，玩家可以积累\\u201c专注点\\u201d，用于连招或蓄力重击。冲刺、闪避和攻击都会消耗气力值。\\n\\n除了物理战斗，玩家还可以使用\\u201c奇术\\u201d、\\u201c身法\\u201d、\\u201c毫毛\\u201d和\\u201c变化\\u201d等四类法术。这些法术有冷却时间并消耗法力值。各类法术按技能可再细分，例如：\\u201c定身\\u201d奇术可以在短时间内冻结敌人；\\u201c聚形散气\\u201d身法可留下假身以便散气遁走，聚形时再行突袭；\\u201c身外身法\\u201d的毫毛法术能召唤多个分身协助战斗，而各种变化法术允许主角变形为不同的生物，并拥有独特的招式和生命值。此外，玩家可以通过击败强大的敌人获得\\u201c精魄\\u201d技能，这些技能类似于一次性的变身，例如，\\u201c幽魂\\u201d精魄可以进行头锤攻击。\\n\\n游戏的进程大多是线性的，亦有较为宽阔的区域供玩家探索。玩家会遇到各种妖怪头目作为敌人。玩家的检查点是世界各地的神龛（土地庙）。游戏地图无法由玩家调整，但会随游戏进展而变化。\"\n" +
                "  }\n" +
                "]";

        try {
            File tempFile = Files.createTempFile("data", ".json").toFile();
            tempFile.deleteOnExit();
            Files.write(tempFile.toPath(), jsonData.getBytes(StandardCharsets.UTF_8));
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp data file", e);
        }
    }
}