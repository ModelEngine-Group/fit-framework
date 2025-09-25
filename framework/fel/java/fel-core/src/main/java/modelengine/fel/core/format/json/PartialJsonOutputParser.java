/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.format.json;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fitframework.log.Logger;
import modelengine.fitframework.serialization.SerializationException;
import modelengine.fitframework.util.MapBuilder;

import java.util.Map;

/**
 * 表示实现解析 json 片段的 {@link JsonOutputParser}
 *
 * @param <O> 表示输出对象类型。
 * @author 易文渊
 * @since 2024-04-28
 */
public final class PartialJsonOutputParser<O> implements JsonOutputParser<O> {
    private static final Logger log = Logger.get(PartialJsonOutputParser.class);

    private static final Map<Character, Character> PARENTHESIS =
            MapBuilder.<Character, Character>get().put('{', '}').put('[', ']').build();

    private final JsonOutputParser<O> jsonOutputParser;

    /**
     * 使用 json 输出解析器创建 {@link PartialJsonOutputParser} 的实例。
     *
     * @param jsonOutputParser 表示 json 解析器的 {@link JsonOutputParser}。
     * @throws IllegalArgumentException 当 {@code jsonOutputParser} 为 {@code null} 时。
     */
    public PartialJsonOutputParser(JsonOutputParser<O> jsonOutputParser) {
        this.jsonOutputParser = notNull(jsonOutputParser, "The json output parser cannot be null.");
    }

    @Override
    public String instruction() {
        return this.jsonOutputParser.instruction();
    }

    @Override
    public O parse(String input) {
        try {
            return this.jsonOutputParser.parse(input);
        } catch (SerializationException ignored) {
            log.debug("Failed attempt to parse the input '{}'.", input);
        }
        try {
            return this.parse0(input);
        } catch (IllegalStateException e) {
            log.warn("Parse '{}' error, msg: {}.", input, e.getMessage());
        }
        return this.jsonOutputParser.parse("{}");
    }

    private O parse0(String input) {
        StringBuilder dst = new StringBuilder();
        StringBuilder stack = new StringBuilder();
        boolean isInsideString = false;
        boolean isEscaped = false;
        for (char c : input.toCharArray()) {
            if (isInsideString) {
                if (c == '"' && !isEscaped) {
                    isInsideString = false;
                } else if (c == '\n' && !isEscaped) {
                    dst.append("\\n");
                    continue;
                } else if (c == '\\') {
                    isEscaped = !isEscaped;
                } else {
                    isEscaped = false;
                }
            } else {
                if (c == '"') {
                    isInsideString = true;
                } else {
                    dealParenthesis(stack, c);
                }
            }
            dst.append(c);
        }
        if (isInsideString) {
            dst.append('"');
        }
        return this.parseCompleteJson(dst, stack.reverse().toString());
    }

    private static void dealParenthesis(StringBuilder stack, char ch) {
        if (PARENTHESIS.containsKey(ch)) {
            stack.append(PARENTHESIS.get(ch));
            return;
        }
        if (PARENTHESIS.containsValue(ch)) {
            if (stack.length() == 0 || stack.charAt(stack.length() - 1) != ch) {
                throw new IllegalStateException("The input is malformed.");
            }
            stack.deleteCharAt(stack.length() - 1);
        }
    }

    private O parseCompleteJson(StringBuilder json, String tail) {
        while (json.length() != 0) {
            try {
                return this.jsonOutputParser.parse(json + tail);
            } catch (SerializationException e) {
                json.deleteCharAt(json.length() - 1);
            }
        }
        throw new IllegalStateException("Ran out of characters.");
    }
}