/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.protocol.util;

import modelengine.fitframework.model.MultiValueMap;
import modelengine.fitframework.resource.UrlUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Utility class for parsing HTTP query parameters into a key-value map.
 * <p>
 * This class provides methods to parse query strings into a {@link MultiValueMap},
 * where keys are ordered and values can be multi-valued. The query string format is
 * typically {@code k1=v1&k2=v2}.
 * </p>
 *
 * @author 季聿阶
 * @since 2025-05-26
 */
public class QueryUtils {
    private static final char KEY_VALUE_PAIR_SEPARATOR = '&';
    private static final char KEY_VALUE_SEPARATOR = '=';

    /**
     * Parses the given query string into a key-value map.
     * <p>
     * The query string is split into key-value pairs using the {@code &} separator.
     * Each key-value pair is further split using the {@code =} separator.
     * The resulting map is ordered and supports multi-valued keys.
     * </p>
     *
     * @param keyValues The query string to be parsed.
     * @return A {@link MultiValueMap} containing the parsed key-value pairs.
     * If the input string is blank, an empty map is returned.
     */
    public static MultiValueMap<String, String> parseQuery(String keyValues) {
        return parseQuery(keyValues, UrlUtils::decodePath);
    }

    /**
     * Parses the given query string into a key-value map using a custom decoding method.
     * <p>
     * The query string is split into key-value pairs using the {@code &} separator.
     * Each key-value pair is further split using the {@code =} separator.
     * The resulting map is ordered and supports multi-valued keys.
     * </p>
     *
     * @param keyValues The query string to be parsed.
     * @param decodeMethod A function to decode the keys and values (e.g., URL decoding).
     * @return A {@link MultiValueMap} containing the parsed and decoded key-value pairs.
     * If the input string is blank, an empty map is returned.
     */
    public static MultiValueMap<String, String> parseQuery(String keyValues, Function<String, String> decodeMethod) {
        MultiValueMap<String, String> map = MultiValueMap.create(LinkedHashMap::new);
        if (StringUtils.isBlank(keyValues)) {
            return map;
        }
        List<String> keyValuePairs =
                StringUtils.split(keyValues, KEY_VALUE_PAIR_SEPARATOR, ArrayList::new, StringUtils::isNotBlank);
        for (String keyValuePair : keyValuePairs) {
            int index = keyValuePair.indexOf(KEY_VALUE_SEPARATOR);
            if (index <= 0) {
                continue;
            }
            String key = decodeMethod.apply(keyValuePair.substring(0, index));
            String value = decodeMethod.apply(keyValuePair.substring(index + 1));
            map.add(key, value);
        }
        return map;
    }
}