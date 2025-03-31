/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {sleep} from "./util.js";

const loginInterceptor = () => {
    const self = {};

    /**
     * 判断是否需要拦截.
     *
     * @param status
     * @return {boolean}
     */
    self.match = (status) => {
        return status === 401;
    }

    /**
     * 具体拦截操作.
     *
     * @param xhr XMLHttpRequest对象.
     */
    self.intercept = (xhr) => {
        const loginHeader = xhr.getResponseHeader("fit-redirect-to-prefix");
        if (loginHeader) {
            window.location.href = loginHeader + window.location.href;
        } else {
            throw new Error("登录失效且headers里面没有跳转登录的url");
        }
    }
    return self;
}

const doInterceptors = (interceptors, xhr) => {
    for (let i = 0; i < interceptors.length; i++) {
        const interceptor = interceptors[i];
        if (interceptor.match(xhr.status)) {
            interceptor.intercept(xhr);
        }
    }
}

/**
 * ajax服务，用于与服务器通信.
 */
export const ajax = {
    interceptors: [loginInterceptor()],
    get: async function (url, callback) {
        let success;
        // XMLHttpRequest对象用于在后台与服务器交换数据
        let xhr = new XMLHttpRequest();
        xhr.withCredentials = true // 跨域时自动带上cookie信息
        xhr.open('GET', url, true);
        xhr.onreadystatechange = function () {
            // readyState == 4说明请求已完成
            if (xhr.readyState === 4 && xhr.status === 200 || xhr.status === 304) {
                success = true;
                // 从服务器获得数据
                if (callback) {
                    callback.call(this, xhr.responseText)
                }
            } else {
                doInterceptors(ajax.interceptors, xhr);
            }
        };
        xhr.send();
        if (!callback) {
            while (!success) {
                await sleep(5);
            }
            return JSON.parse(xhr.responseText);
        }
      return undefined;
    }, // datat应为'a=a1&b=b1'这种字符串格式，在jq里如果data为对象会自动将对象转成这种字符串格式
    post: async function (url, data, callback) {
        let xhr = new XMLHttpRequest();
        let success;
        xhr.withCredentials = true // 跨域时自动带上cookie信息
        xhr.open("POST", url, true);
        xhr.setRequestHeader("Content-type", "application/json;charset-UTF-8");
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4 && (xhr.status === 200 || xhr.status === 304)) {
                success = true;
                callback && callback.call(this, JSON.parse(xhr.responseText))
            } else {
                doInterceptors(ajax.interceptors, xhr);
            }
        };
        try {
            xhr.send(JSON.stringify(data));

            if (!callback) {
                while (!success) {
                    await sleep(5);
                }
                return JSON.parse(xhr.responseText);
            }
          return undefined;
        } catch (e) {
            return undefined;
        }
    },
    addInterceptor: function (interceptor) {
        this.interceptors.push(interceptor);
    }
};