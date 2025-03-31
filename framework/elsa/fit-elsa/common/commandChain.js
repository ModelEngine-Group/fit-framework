/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {uuid} from "./util.js";

/**
 * 针对形状的相应键盘/鼠标事件状态机
 * 辉子 2021
 */
const commandChain = (shape) => {
    const self = {};
    const newCommand = action => {
        let actionVal = action !== undefined ? action : varShape => {
      };
      const command = {id: uuid(), action: actionVal, events: []};
        command.add = (action, key) => {
            const next = newCommand(action);
            command.events.push({key, command: next});
            return next;
        };
        /**
         * 如果当前command是异步致性，不等待执行结果
         */
        command.ignoreAwait = () => {
            command.awaitIgnored = true;
            return command;
        }
        command.run = () => self.run();
        return command;
    };

    self.config = initAction => {
        self.starter = newCommand(initAction);
        return self.starter;
    };

    const keyPressed = shape.keyPressed;
    const click = shape.click;
    const init = () => {
        (self.context === undefined) && (self.context = {});
        self.starter.action(shape, self.context);
        self.currentCommand = self.starter;
    };
    const readyAndMoveNext = next => {
        self.running = false;
        self.currentCommand = next.command;
    }
    self.running = false;
    self.run = () => {
        init();
        shape.keyPressed = e => {
            const next = self.currentCommand.events.find(e1 => e1.key === e.key && !self.running);
            if (next) {
                self.running = true;
                const result = next.command.action(shape, self.context);
                if (result && result.then && !next.awaitIgnored) {
                    result.then(() => readyAndMoveNext(next));
                } else {
                    readyAndMoveNext(next);
                    return false;
                }
            } else {
                if (e.key === "r") {
                    init();
                  return undefined;
                }
                if (e.code) {
                    return keyPressed.call(shape, e);
                }
            }
          return undefined;
        }
        shape.click = () => {
            shape.keyPressed({key: "click"});
        };
    };

    self.stop = () => {
        shape.keyPressed = e => keyPressed.call(shape, e);
        shape.click = () => click.call(shape);
    }

    return self;

};

export {commandChain};

