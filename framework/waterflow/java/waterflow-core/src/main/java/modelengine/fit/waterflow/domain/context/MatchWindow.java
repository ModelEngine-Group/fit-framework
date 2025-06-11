/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 条件节点的Match window
 *
 * @author 宋永坦
 * @since 1.0
 */
public class MatchWindow extends Window {
    private static final Map<String, MatchWindow> all = new ConcurrentHashMap<>();

    private final Set<MatchWindow> arms = new HashSet<>();

    /**
     * 创建一个MatchWindow
     *
     * @param source 源窗口
     * @param id 窗口ID
     * @param data 窗口数据
     */
    public MatchWindow(Window source, UUID id, Object data) {
        super(inputs -> false, id);
        this.from = source;
        source.addTo(this);
    }

    /**
     * 创建一个MatchWindow
     *
     * @param source 源窗口
     * @param id 窗口ID
     * @param data 窗口数据
     * @return 返回创建的MatchWindow对象
     */
    public static synchronized MatchWindow from(Window source, UUID id, Object data) {
        MatchWindow window = all.get(id.toString());
        if (window == null) {
            window = new MatchWindow(source, id, data);
            FlowSession session = new FlowSession(source.getSession());
            session.setWindow(window);
            all.put(id.toString(), window);
        }
        WindowToken token = window.createToken();
        token.beginConsume();
        List<MatchWindow> arms = all.values().stream().filter(t -> t.from == source).collect(Collectors.toList());
        for (MatchWindow a : arms) {
            a.setArms(arms);
        }
        if (source.isOngoing()) {
            window.arms.forEach(w -> w.complete());
        }
        token.finishConsume();
        return window;
    }

    private void setArms(List<MatchWindow> arms) {
        this.arms.addAll(arms);
    }

    @Override
    public void complete() {
        super.complete();
    }

    @Override
    public boolean fulfilled() {
        return this.from.isComplete() && this.from.isOngoing();
    }
}
