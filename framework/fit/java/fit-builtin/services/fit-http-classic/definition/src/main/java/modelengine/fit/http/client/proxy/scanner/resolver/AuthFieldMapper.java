/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.resolver;

import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fit.http.client.proxy.support.authorization.ApiKeyAuthorization;
import modelengine.fit.http.client.proxy.support.authorization.BasicAuthorization;
import modelengine.fit.http.client.proxy.support.authorization.BearerAuthorization;

/**
 * 鉴权字段映射工具类。
 * <p>用于确定参数级别鉴权应该更新 {@link modelengine.fit.http.client.proxy.Authorization} 对象的哪个字段。</p>
 *
 * <p><b>设计背景</b></p>
 * <p>参数级别的鉴权（如 {@code @RequestAuth(type = BEARER) String token}）需要通过
 * {@code authorizationInfo(key, value)} 方法动态更新已存在的 Authorization 对象。
 * 这个 "key" 必须与 Authorization 实现类中 {@code setValue(String key, Object value)}
 * 方法能识别的字段名一致。</p>
 *
 * <p><b>字段映射关系</b></p>
 * <table border="1">
 *   <caption>鉴权类型字段映射表</caption>
 *   <tr>
 *     <th>鉴权类型</th>
 *     <th>Authorization 实现</th>
 *     <th>可更新字段</th>
 *     <th>字段含义</th>
 *     <th>字段常量</th>
 *   </tr>
 *   <tr>
 *     <td>BEARER</td>
 *     <td>BearerAuthorization</td>
 *     <td>"token"</td>
 *     <td>Bearer Token 值</td>
 *     <td>BearerAuthorization.AUTH_TOKEN</td>
 *   </tr>
 *   <tr>
 *     <td>BASIC</td>
 *     <td>BasicAuthorization</td>
 *     <td>"username"<br/>"password"</td>
 *     <td>用户名或密码<br/>（参数级别建议只更新一个）</td>
 *     <td>BasicAuthorization.AUTH_USER_NAME<br/>BasicAuthorization.AUTH_USER_PWD</td>
 *   </tr>
 *   <tr>
 *     <td>API_KEY</td>
 *     <td>ApiKeyAuthorization</td>
 *     <td>"key"<br/>"value"</td>
 *     <td>HTTP Header/Query 名称<br/>实际的 API Key 值</td>
 *     <td>ApiKeyAuthorization.AUTH_KEY<br/>ApiKeyAuthorization.AUTH_VALUE</td>
 *   </tr>
 * </table>
 *
 * <p><b>使用示例</b></p>
 * <pre>{@code
 * // 参数级别 Bearer Token
 * String api(@RequestAuth(type = BEARER) String token);
 * // → 更新 BearerAuthorization.token 字段
 *
 * // 参数级别 API Key（更新值）
 * String search(@RequestAuth(type = API_KEY, name = "X-API-Key") String key);
 * // → 更新 ApiKeyAuthorization.value 字段
 * // → ApiKeyAuthorization.key 从注解的 name 属性获取（"X-API-Key"）
 * }</pre>
 *
 * <p><b>与 FEL Tool 系统的一致性</b></p>
 * <p>此类的设计与 FEL Tool 系统中的 JSON 配置保持一致。例如：</p>
 * <pre>{@code
 * // FEL Tool JSON 配置
 * {
 *   "mappings": {
 *     "people": {
 *       "name": {
 *         "key": "token",           // ← 字段名
 *         "httpSource": "AUTHORIZATION"
 *       }
 *     }
 *   }
 * }
 *
 * // 对应的注解使用
 * String api(@RequestAuth(type = BEARER) String token);
 * // → AuthFieldMapper.getParameterAuthField(BEARER) 返回 "token"
 * // → 与 JSON 中的 "key": "token" 完全一致
 * }</pre>
 *
 * @author 季聿阶
 * @since 2025-10-01
 * @see modelengine.fit.http.client.proxy.Authorization
 * @see modelengine.fit.http.client.proxy.support.setter.AuthorizationDestinationSetter
 * @see BearerAuthorization
 * @see BasicAuthorization
 * @see ApiKeyAuthorization
 */
public final class AuthFieldMapper {

    private AuthFieldMapper() {
        // 工具类，禁止实例化
    }

    /**
     * 获取参数级别鉴权应该更新的 Authorization 字段名。
     *
     * <p>此方法返回的字段名将用于 {@code requestBuilder.authorizationInfo(key, value)} 调用，
     * 进而调用 {@code authorization.set(key, value)} 来更新对应字段。</p>
     *
     * <p><b>重要说明：</b></p>
     * <ul>
     * <li><b>BEARER</b>: 返回 {@code "token"}，更新 Bearer Token 值。
     *     <br/>示例：{@code @RequestAuth(type = BEARER) String token}
     *     <br/>效果：更新 {@code BearerAuthorization.token} 字段</li>
     *
     * <li><b>BASIC</b>: 返回 {@code "username"}，只能更新用户名。
     *     <br/>注意：如需同时设置用户名和密码，建议使用静态配置（方法或类级别的 @RequestAuth）
     *     <br/>示例：{@code @RequestAuth(type = BASIC) String username}
     *     <br/>效果：更新 {@code BasicAuthorization.username} 字段</li>
     *
     * <li><b>API_KEY</b>: 返回 {@code "value"}，更新 API Key 的值（而非名称）。
     *     <br/>关键理解：API Key 有两个概念：
     *     <ul>
     *       <li>API Key 的<b>名称</b>：HTTP Header/Query 的 key（如 "X-API-Key"），
     *           对应 {@code ApiKeyAuthorization.key} 字段，通过注解的 {@code name} 属性指定</li>
     *       <li>API Key 的<b>值</b>：实际的密钥字符串，
     *           对应 {@code ApiKeyAuthorization.value} 字段，通过参数传入</li>
     *     </ul>
     *     示例：{@code @RequestAuth(type = API_KEY, name = "X-API-Key") String apiKeyValue}
     *     <br/>效果：
     *     <ul>
     *       <li>{@code ApiKeyAuthorization.key} = "X-API-Key" （从注解的 name 属性）</li>
     *       <li>{@code ApiKeyAuthorization.value} = apiKeyValue （从参数，本方法返回的字段）</li>
     *       <li>最终 HTTP Header: {@code X-API-Key: apiKeyValue}</li>
     *     </ul>
     * </li>
     * </ul>
     *
     * @param type 鉴权类型
     * @return Authorization 对象的字段名，用于 {@code authorization.set(fieldName, value)} 调用
     * @throws IllegalArgumentException 如果鉴权类型不支持参数级别动态更新
     */
    public static String getParameterAuthField(AuthType type) {
        switch (type) {
            case BEARER:
                // 参考 BearerAuthorization.AUTH_TOKEN = "token"
                // setValue() 方法: if (key.equals("token")) { this.token = value; }
                return "token";

            case BASIC:
                // 参考 BasicAuthorization.AUTH_USER_NAME = "username"
                // setValue() 方法: if (key.equals("username")) { this.username = value; }
                // 注意：只返回 username，password 需要静态配置或单独处理
                return "username";

            case API_KEY:
                // 参考 ApiKeyAuthorization.AUTH_VALUE = "value"
                // setValue() 方法: if (key.equals("value")) { this.value = value; }
                //
                // 重要：返回 "value" 而不是 "key"
                // - ApiKeyAuthorization.key 字段存储的是 HTTP Header/Query 的名称（如 "X-API-Key"）
                //   这个值来自注解的 name 属性，在静态鉴权时设置
                // - ApiKeyAuthorization.value 字段存储的是实际的 API Key 值
                //   这个值来自参数传入，是参数级别需要动态更新的字段
                return "value";

            case CUSTOM:
                throw new IllegalArgumentException(
                    "CUSTOM auth type must use AuthProvider, not supported for parameter-level auth");

            default:
                throw new IllegalArgumentException(
                    "Unsupported auth type for parameter-level auth: " + type);
        }
    }
}
