/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import axios from 'axios';

/**
 * 基于axios创建一个AxiosInstance。
 *
 * @param headers 请求头。
 * @returns {AxiosInstance} 创建的AxiosInstance。
 */
const createInstance = (headers = {}) => {
  // 如果 headers 是 Map 类型，转换为普通对象
  const headersObject = headers instanceof Map ? Object.fromEntries(headers) : headers;
  const instance = axios.create({
    withCredentials: true, // 设置 withCredentials 为 true
    timeout: 15000, // 设置请求超时时间（毫秒）
    headers: {
      'Content-Type': 'application/json', // 设置默认请求头
      ...headersObject, // 合并自定义的请求头
    },
    // 这里可以添加拦截器,更多关于拦截器的内容可以参考 axios 文档：https://axios-http.com/docs/interceptors
  });

  // 添加拦截器
  instance.interceptors.request.use(config => {
    // 在请求发送之前执行的逻辑
    return config;
  }, error => {
    // 对请求错误做些什么
    return Promise.reject(error);
  });

  instance.interceptors.response.use(response => {
    // 在响应返回之后执行的逻辑
    return response.data;
  }, error => {
    // 对响应错误做些什么
    return Promise.reject(error);
  });

  return instance;
};

/**
 * 创建一个基于Axios实现的httpRequest实例。
 *
 * @param method http调用的方法。 e.g. GET/POST/PUT/DELETE
 * @param url 调用的目标url。
 * @param headers 调用时使用的请求头。
 * @param data body中请求的数据。
 * @param callback 请求成功后的回调。
 * @param errorCallback 请求失败的回调
 */
export const httpRequest = ({method, url, headers, data, callback = () => {
}, errorCallback = () => {
}}) => {
  // 如果 headers 不是 Map 类型，创建一个新的 Map
  if (!(headers instanceof Map)) {
    throw new TypeError('Expected headers to be an instance of Map');
  }
  headers.set('X-Auth-Token', getCookie('__Host-X-Auth-Token'));
  headers.set('X-Csrf-Token', getCookie('__Host-X-Csrf-Token'));

  const instance = createInstance(headers);
  instance.request({
    method: method,
    url: url,
    data: data,
  }).then(response => {
    callback(response);
  }).catch(error => {
    errorCallback(error);
  });
};

const getCookie = (cname) => {
  const name = `${cname}=`;
  const ca = document.cookie.split(';');
  for (let i = 0; i < ca.length; i++) {
    const c = ca[i].trim();
    if (c.indexOf(name) === 0) {
      return c.substring(name.length, c.length);
    }
  }
  return '';
};

/**
 * get请求。
 *
 * @param url 请求的url地址。
 * @param headers 请求头。
 * @param callback 回调。
 * @param errorCallback 请求失败的回调
 */
export const get = (url, headers = new Map(), callback, errorCallback) => {
  httpRequest({method: 'Get', url, headers, data: {}, callback, errorCallback});
};

/**
 * post请求。
 *
 * @param url 请求的url地址。
 * @param data 请求的数据。
 * @param headers 请求头。
 * @param callback 回调。
 * @param errorCallback 请求失败的回调
 */
export const post = (url, data, headers = new Map(), callback, errorCallback) => {
  httpRequest({method: 'Post', url, headers, data, callback, errorCallback});
};

/**
 * put请求。
 *
 * @param url 请求的url地址。
 * @param data 请求的数据。
 * @param headers 请求头。
 * @param callback 回调。
 * @param errorCallback 请求失败的回调
 */
export const put = (url, data, headers = new Map(), callback, errorCallback) => {
  httpRequest({method: 'Put', url, headers, data, callback, errorCallback});
};

/**
 * delete请求。delete为关键字，因此这里使用del作为方法名。
 *
 * @param url 请求的url地址。
 * @param headers 请求头。
 * @param callback 回调。
 * @param errorCallback 请求失败的回调
 */
export const del = (url, headers = new Map(), callback, errorCallback) => {
  httpRequest({method: 'Delete', url, headers, data: {}, callback, errorCallback});
};

// 添加默认导出
const httpUtil = {
  get,
  post,
  put,
  del,
};

export default httpUtil;