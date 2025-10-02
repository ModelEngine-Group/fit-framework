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
import modelengine.fitframework.util.StringUtils;

/**
 * 鉴权字段映射工具类。
 * <p>用于确定参数级别鉴权应该更新 {@link modelengine.fit.http.client.proxy.Authorization}
 * 对象的哪个字段。</p>
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
 * <p><b>与 Tool 系统的一致性</b></p>
 * <p>此类的设计与 Tool 系统中的 JSON 配置保持一致。例如：</p>
 * <pre>{@code
 * // Tool JSON 配置
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
 * @see modelengine.fit.http.client.proxy.Authorization
 * @see modelengine.fit.http.client.proxy.support.setter.AuthorizationDestinationSetter
 * @see BearerAuthorization
 * @see BasicAuthorization
 * @see ApiKeyAuthorization
 * @since 2025-10-01
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
     *     <br/>效果：更新 {@code BearerAuthorization.token} 字段
     *     <br/>注意：{@code name} 属性对 BEARER 无效</li>
     *
     * <li><b>BASIC</b>: 根据 {@code name} 属性决定更新哪个字段。
     *     <br/><b>name 属性行为：</b>
     *     <ul>
     *       <li>{@code name = "username"}: 更新 {@code BasicAuthorization.username} 字段</li>
     *       <li>{@code name = "password"}: 更新 {@code BasicAuthorization.password} 字段</li>
     *       <li>{@code name} 未指定或为空: 默认更新 {@code username} 字段（向后兼容）</li>
     *     </ul>
     *     <br/>单字段示例：{@code @RequestAuth(type = BASIC) String username}
     *     <br/>效果：更新 {@code BasicAuthorization.username} 字段
     *     <br/><br/>双字段示例（推荐）：
     *     <pre>{@code
     * String login(
     *     @RequestAuth(type = BASIC, name = "username") String user,
     *     @RequestAuth(type = BASIC, name = "password") String pwd
     * );
     *     }</pre>
     *     效果：同时更新 {@code username} 和 {@code password} 字段</li>
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
     *     注意：对于 API_KEY，{@code name} 属性必须指定 HTTP Header/Query 名称，
     *     不能用于字段选择</li>
     * </ul>
     *
     * <p><b>name 属性的语义重载：</b></p>
     * <p>{@code name} 属性在不同鉴权类型下有不同含义：</p>
     * <ul>
     *   <li><b>BASIC</b>: 指定要更新的字段名（"username" 或 "password"）</li>
     *   <li><b>API_KEY</b>: 指定 HTTP Header/Query 的名称（如 "X-API-Key"）</li>
     *   <li><b>BEARER</b>: 无效，被忽略</li>
     * </ul>
     *
     * @param type 表示鉴权类型的 {@link AuthType}。
     * @param nameAttribute 注解的 {@code name} 属性值，可能为 {@code null} 或空字符串。
     * @return Authorization 对象的字段名，用于 {@code authorization.set(fieldName, value)} 调用。
     * @throws IllegalArgumentException 如果鉴权类型不支持参数级别动态更新，或 BASIC 类型的
     * {@code name} 属性值无效（不是 "username" 或 "password"）。
     */
    public static String getParameterAuthField(AuthType type, String nameAttribute) {
        return switch (type) {
            case BEARER ->
                // 参考 BearerAuthorization.AUTH_TOKEN = "token"
                // setValue() 方法: if (key.equals("token")) { this.token = value; }
                // name 属性对 BEARER 无效，直接忽略
                    "token";
            case BASIC -> {
                // 参考 BasicAuthorization.AUTH_USER_NAME = "username", AUTH_USER_PWD = "password"
                // setValue() 方法:
                //   if (key.equals("username")) { this.username = value; }
                //   if (key.equals("password")) { this.password = value; }
                //
                // name 属性用于指定要更新的字段：
                // - name = "username": 更新 username 字段
                // - name = "password": 更新 password 字段
                // - name 未指定或为空: 默认更新 username（向后兼容）
                if (StringUtils.isNotBlank(nameAttribute)) {
                    if (StringUtils.equals("username", nameAttribute)) {
                        yield "username";
                    } else if (StringUtils.equals("password", nameAttribute)) {
                        yield "password";
                    } else {
                        throw new IllegalArgumentException(
                                "For BASIC auth, name attribute must be 'username' or 'password', got: "
                                        + nameAttribute);
                    }
                }
                // 默认行为：更新 username（向后兼容）
                yield "username";
                // 默认行为：更新 username（向后兼容）
            }
            case API_KEY ->
                // 参考 ApiKeyAuthorization.AUTH_VALUE = "value"
                // setValue() 方法: if (key.equals("value")) { this.value = value; }
                //
                // 重要：返回 "value" 而不是 "key"
                // - ApiKeyAuthorization.key 字段存储的是 HTTP Header/Query 的名称（如 "X-API-Key"）
                //   这个值来自注解的 name 属性，在静态鉴权时设置
                // - ApiKeyAuthorization.value 字段存储的是实际的 API Key 值
                //   这个值来自参数传入，是参数级别需要动态更新的字段
                //
                // 注意：对于 API_KEY，name 属性的含义与 BASIC 不同
                // - API_KEY 的 name: HTTP Header/Query 的名称（不影响此方法返回值）
                // - BASIC 的 name: 要更新的字段名（影响此方法返回值）
                    "value";
            case CUSTOM -> throw new IllegalArgumentException(
                    "CUSTOM auth type must use AuthProvider, not supported for parameter-level auth");
        };
    }
}
