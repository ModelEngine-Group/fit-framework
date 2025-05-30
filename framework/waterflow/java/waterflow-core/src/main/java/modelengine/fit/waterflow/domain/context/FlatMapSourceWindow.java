/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

import lombok.Getter;
import modelengine.fit.waterflow.domain.common.Constants;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.context.repo.flowsession.FlowSessionRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FlatMap 操作符对应的特殊 Window 实现。
 * 该窗口管理每个数据对应的新 Session Window。每条数据对应一个 FlatMapWindow。
 * FlatMapWindow 产生的新数据会存放到相应的槽（slot）中，并开始整理序号。
 * FlatMapSourceWindow 会对每个 FlatMapWindow 产生的数据进行排序，排序后的上下文（context）将进入下一步的数据处理。
 * 注意：当前实现中的内存释放需要特别关注（待办事项）
 *
 * @author 宋永坦
 * @since 1.0
 */
public class FlatMapSourceWindow extends Window {
    private final FlowContextRepo repo;

    /**
     * session带下来的原始token
     * FlatMapSourceWindowToken是对token的装饰
     */
    private final List<FlatMapWindow> outWindows = new ArrayList<>();

    /**
     * flatmap后的数据槽
     * 在保序情况下flatmap后的数据必须从第一个数据开始排序
     * 数据槽暂存所有未进入排序的数据，排序成功后清空该数据槽
     */
    private final Map<FlatMapWindow, UnconfirmedIndexSlot> indexSlots = new HashMap<>();

    /**
     * 游标，记录flatmap生产出来新数据的最新index
     */
    AtomicInteger cursor = new AtomicInteger(-1);

    /**
     * 根据输入的原始window，在flatmap节点产生新的flatmap source window
     *
     * @param window 输入window
     * @param repo   持久化repo
     */
    public FlatMapSourceWindow(Window window, FlowContextRepo repo) {
        super();
        this.from = window;
        this.from.addTo(this);
        this.repo = repo;
    }

    /**
     * 根据输入的原始窗口和上下文仓库创建或获取一个 FlatMapSourceWindow 实例。
     *
     * @param <I> 输入类型。
     * @param flowId 流标识。
     * @param window 原始窗口。
     * @param repo 上下文仓库。
     * @return FlatMapSourceWindow 实例
     */
    public static <I> FlatMapSourceWindow from(String flowId, Window window, FlowContextRepo repo) {
        return FlowSessionRepo.getFlatMapSource(flowId, window, repo);
    }

    /**
     * window唯一id，该id与输入window id相同
     *
     * @return 输入的window id
     */
    @Override
    public String id() {
        return from.id();
    }

    /**
     * 接受并处理来自上游的窗口令牌。
     * 所有流操作符在处理数据后，会关联新的会话，并通过窗口令牌确定每条数据的转换。
     * 数据处理完成后，新的会话需要根据处理前的窗口令牌生成对应的新的窗口令牌，
     * 并将上一个窗口令牌的状态改为 consumed，新的窗口令牌状态为 initialized。
     *
     * @param source 数据处理前的window token
     * @return 数据处理后的window token
     */
    @Override
    public synchronized WindowToken acceptToken(WindowToken source) {
        return from.acceptToken(source);
    }

    /**
     * 添加一个新的 FlatMapWindow。
     * 每个 FlatMap 操作生成的新数据都会对应一个新的流，该方法记录每个数据对应的新流会话窗口。
     *
     * @param window 对应的新会话窗口
     */
    public synchronized void addFlatMapWindow(FlatMapWindow window) {
        this.outWindows.add(window);
    }


    /**
     * 生成数据索引。
     * 新的数据生成后会进入数据槽，并根据现有条件进行排序。有效排序的数据将传递到下一节点处理。
     *
     * @param context 生成的新数据上下文
     * @param window  数据对应的窗口
     */
    public synchronized void generateIndex(FlowContext context, FlatMapWindow window) {
        initIndex(context);
        UnconfirmedIndexSlot indexer = insertSlots(context, window);
        if (context.getSession().preserved()) {
            preserveIndexes();
        } else {
            randomIndex(indexer, context);
        }
        complete();
    }

    /**
     * 将初始化的序号放到无限大，节点线程将得不到有效的数据
     * 直到排序后序号从0开始，才会进入数据处理
     *
     * @param context flatmap新生产出来的最新数据
     */
    private void initIndex(FlowContext<Object> context) {
        context.setIndex(Constants.UNCONFIRMED_INDEX);
    }

    /**
     * 将flatmap后的新数据插入对应的数据槽
     *
     * @param context flatmap后最新的一条生产后数据
     * @param window  该数据对应的window,通过该window找到对应的数据槽
     * @return
     */
    private UnconfirmedIndexSlot insertSlots(FlowContext context, FlatMapWindow window) {
        UnconfirmedIndexSlot indexer = this.indexSlots.get(window);
        if (indexer == null) {
            indexer = new UnconfirmedIndexSlot(window);
            this.indexSlots.put(window, indexer);
        }
        indexer.addContext(context);
        return indexer;
    }

    /**
     * 对已经可确定的数据进行排序
     * 并不是所有完成的数据都可以排序，比如slot1的数据没有完全接收完，slot2里的数据不参与排序，知道slot1数据结束
     */
    private void preserveIndexes() {
        Integer count = 0;
        for (FlatMapWindow outToken : this.outWindows) {
            if (outToken.tokenCount() == 0 && outToken.fulfilled()) {
                // 此flatmap没有发出数据
                continue;
            }
            UnconfirmedIndexSlot slot = this.indexSlots.get(outToken);
            if (slot == null) {
                // 靠前的数据没到
                break;
            }
            count += slot.confirmOrder(this.cursor, repo);
            if (!slot.isDone()) {
                // 靠前数据没有到够
                break;
            }
        }
        for (int i = 0; i < count; i++) {
            createToken();
        }
    }

    /**
     * 该次流操作没有保序要求，将新生成的数据index设置为-1,
     * index为-1的数据在节点守护线程下直接消费
     *
     * @param indexer 对应的数据槽
     * @param context 要设置无序处理的数据
     */
    private void randomIndex(UnconfirmedIndexSlot indexer, FlowContext context) {
        indexer.confirmRandom(context, repo);
        createToken();
    }

    /**
     * 整个flatmap结束，源数据完全处理完毕
     * 进行最后一次排序，并标志该节点的该session完成了所有flatmap操作
     */
    @Override
    public void complete() {
        if (this.from.isDone() && this.indexSlots.values().size() == this.from.tokenCount()
                && this.indexSlots.values().stream().allMatch(UnconfirmedIndexSlot::isDone)) {
            if (this.getSession().preserved()) {
                preserveIndexes();
            }
            super.complete();
        }
    }

    /**
     * 数据槽
     * 未排序的数据按照不同的source data暂存在对应的数据槽
     * 通过confirm函数进行数据排序
     *
     * @author huizi
     */
    static class UnconfirmedIndexSlot {
        @Getter
        private final FlatMapWindow window;

        /**
         * 该数据槽中还未排序数据
         */
        private final List<FlowContext<Object>> todo = new ArrayList<>();

        /**
         * 该数据槽已经完成多少数据的排序
         */
        private final AtomicLong done = new AtomicLong(0);

        /**
         * 构造函数
         * 一个flatmap window对应一个数据槽
         *
         * @param window 对应的flatmap window
         */
        UnconfirmedIndexSlot(FlatMapWindow window) {
            this.window = window;
        }

        /**
         * 数据槽加数据
         *
         * @param context 新生产的数据
         */
        synchronized void addContext(FlowContext context) {
            this.todo.add(context);
        }

        /**
         * 为数据槽里已有数据加序号
         *
         * @param cursor 当前序号
         * @param repo   对应的repo
         * @return 序号
         */
        public Integer confirmOrder(AtomicInteger cursor, FlowContextRepo repo) {
            if (todo.size() == 0) return 0;
            Integer old = cursor.get();
            todo.forEach(context -> {
                context.setIndex(cursor.incrementAndGet());
            });
            repo.updateIndex(todo);
            this.done.addAndGet(todo.size());
            todo.clear();
            return cursor.get() - old;
        }

        /**
         * 为该数据槽的数据确定乱序
         *
         * @param context 乱序的数据
         * @param repo    对应的repo
         */
        public void confirmRandom(FlowContext context, FlowContextRepo repo) {
            context.setIndex(Constants.NOT_PRESERVED_INDEX);
            repo.update(this.todo);
            this.done.incrementAndGet();
            this.todo.clear();
        }

        /**
         * 该数据槽数据是否都处理完了
         * 通过flatmap window对应的flatmap数据源的状态判断
         *
         * @return 是否处理完成
         */
        public boolean isDone() {
            return this.done.get() == window.getSource().tokenCount() && window.getSource().isOngoing();
        }
    }
}


