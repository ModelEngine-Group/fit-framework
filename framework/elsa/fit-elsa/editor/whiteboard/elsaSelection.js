/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

export const elsaSelection = (root) => {
    const self = {};

    /**
     * 是否处于聚焦状态.
     *
     * @returns {*} true/false.
     */
    self.isFocused = () => {
        const selection = window.getSelection();
        if (selection.rangeCount === 0) {
            return false;
        }
        const range = selection.getRangeAt(0);
        return root.contains(range.startContainer) && root.contains(range.startContainer);
    }

    /**
     * 选举当前root节点的所有文本.
     */
    self.focus = () => {
        const range = document.createRange();
        range.selectNodeContents(root);

        const selection = window.getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
    }

    /**
     * 取消选中.
     */
    self.unFocus = () => {
        const selection = window.getSelection();
        selection.removeAllRanges();

        // 这里要使activeElement失焦，让activeElement变为body，否则选中图形时，无法触发delete、上下左右移动等操作.
        root.blur();
    }

    self.isCollapsed = () => {
        return window.getSelection().isCollapsed;
    }

    /**
     * 获取选中的元素.
     *
     * @param whatToShow {number} 中的类型.
     * @param filter 过滤函数.
     * @returns {*[]} 节点数组。
     */
    self.getSelectNodes = (whatToShow = NodeFilter.SHOW_ELEMENT, filter) => {
        let selectedElements = [];
        let selection = window.getSelection();
        let rangeCount = selection.rangeCount;

        for (let i = 0; i < rangeCount; ++i) {
            const selectedRange = selection.getRangeAt(i);
            let commonAncestorContainer = selectedRange.commonAncestorContainer;
            while (commonAncestorContainer && commonAncestorContainer.nodeType !== 1) {
                commonAncestorContainer = commonAncestorContainer.parentNode;
            }

            if (!commonAncestorContainer) {
                return selectedElements;
            }

            const iterator = document.createNodeIterator(commonAncestorContainer, whatToShow);
            while (iterator.nextNode()) {
                const element = iterator.referenceNode;
                if (!filter(element)) {
                    continue;
                }

                if (element.contains(selectedRange.startContainer) || element === selectedRange.startContainer) {
                    selectedElements.push(element);
                    continue;
                }

                if (element.contains(selectedRange.endContainer) || element === selectedRange.endContainer) {
                    selectedElements.push(element);
                    continue;
                }

                const range = document.createRange();
                range.selectNodeContents(element);
                if (range.compareBoundaryPoints(Range.START_TO_START, selectedRange) > -1 && range.compareBoundaryPoints(Range.END_TO_END, selectedRange) < 1) {
                    selectedElements.push(element);
                }
                range.detach();
            }
        }

        return selectedElements;
    }

    return self;
}