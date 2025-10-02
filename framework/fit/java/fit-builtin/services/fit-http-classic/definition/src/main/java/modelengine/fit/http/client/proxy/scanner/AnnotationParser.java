/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner;

import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fit.http.annotation.DeleteMapping;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PatchMapping;
import modelengine.fit.http.annotation.PathVariable;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.PutMapping;
import modelengine.fit.http.annotation.RequestAddress;
import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.annotation.RequestBean;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestCookie;
import modelengine.fit.http.annotation.RequestForm;
import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fit.http.client.proxy.PropertyValueApplier;
import modelengine.fit.http.client.proxy.scanner.entity.Address;
import modelengine.fit.http.client.proxy.scanner.entity.HttpInfo;
import modelengine.fit.http.client.proxy.scanner.resolver.PathVariableResolver;
import modelengine.fit.http.client.proxy.scanner.resolver.RequestAuthResolver;
import modelengine.fit.http.client.proxy.scanner.resolver.RequestBodyResolver;
import modelengine.fit.http.client.proxy.scanner.resolver.RequestCookieResolver;
import modelengine.fit.http.client.proxy.scanner.resolver.RequestFormResolver;
import modelengine.fit.http.client.proxy.scanner.resolver.RequestHeaderResolver;
import modelengine.fit.http.client.proxy.scanner.resolver.RequestQueryResolver;
import modelengine.fit.http.client.proxy.support.applier.MultiDestinationsPropertyValueApplier;
import modelengine.fit.http.client.proxy.support.applier.StaticAuthApplier;
import modelengine.fit.http.client.proxy.support.setter.DestinationSetterInfo;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.util.ArrayUtils;
import modelengine.fitframework.util.ReflectionUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.value.ValueFetcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses annotations on interfaces and methods to extract HTTP-related information.
 * This class is responsible for interpreting annotations such as @RequestMapping, @GetMapping, @PostMapping, etc.,
 * and converting them into structured data that can be used to build HTTP requests.
 *
 * @author 王攀博
 * @since 2025-01-07
 */
public class AnnotationParser {
    private static final int MAX_PATH_SIZE = 1;
    private static final String KEY_OF_METHOD_PATH = "path";
    private static final Set<Class<? extends Annotation>> mappingMethodAnnotations =
            Stream.of(PostMapping.class, PutMapping.class, GetMapping.class, DeleteMapping.class, PatchMapping.class)
                    .collect(Collectors.toSet());
    private static final Map<Class<? extends Annotation>, ParamResolver<?>> annotationParsers = new HashMap<>();

    static {
        annotationParsers.put(RequestQuery.class, new RequestQueryResolver());
        annotationParsers.put(RequestHeader.class, new RequestHeaderResolver());
        annotationParsers.put(RequestCookie.class, new RequestCookieResolver());
        annotationParsers.put(RequestBody.class, new RequestBodyResolver());
        annotationParsers.put(RequestForm.class, new RequestFormResolver());
        annotationParsers.put(PathVariable.class, new PathVariableResolver());
        annotationParsers.put(RequestAuth.class, new RequestAuthResolver());
    }

    private final ValueFetcher valueFetcher;
    private final BeanContainer beanContainer;

    /**
     * Constructs an AnnotationParser with the specified ValueFetcher.
     *
     * @param valueFetcher The {@link ValueFetcher} used to fetch values for property setters.
     * @param beanContainer The {@link BeanContainer} used to retrieve beans.
     */
    public AnnotationParser(ValueFetcher valueFetcher, BeanContainer beanContainer) {
        this.valueFetcher = notNull(valueFetcher, "The value fetcher cannot be null.");
        this.beanContainer = notNull(beanContainer, "The bean container cannot be null.");
    }

    /**
     * Parses the given interface class to extract HTTP information for each method.
     *
     * @param clazz The interface class to parse.
     * @return A map of methods to their corresponding HTTP information.
     */
    public Map<Method, HttpInfo> parseInterface(Class<?> clazz) {
        Map<Method, HttpInfo> httpInfoMap = new HashMap<>();
        if (clazz.isInterface()) {
            String pathPatternPrefix = this.getPathPatternPrefix(clazz);
            Address address = this.getAddress(clazz);
            List<PropertyValueApplier> classLevelAuthAppliers = this.getClassLevelAuthAppliers(clazz);
            Arrays.stream(clazz.getMethods()).forEach(method -> {
                HttpInfo httpInfo = this.parseMethod(method, pathPatternPrefix);
                httpInfo.setAddress(address);

                // 构建静态应用器列表（类级别鉴权 + 方法级别鉴权）
                List<PropertyValueApplier> staticAppliers = new ArrayList<>(classLevelAuthAppliers);
                staticAppliers.addAll(this.getMethodLevelAuthAppliers(method));
                httpInfo.setStaticAppliers(staticAppliers);

                httpInfoMap.put(method, httpInfo);
            });
        }
        return httpInfoMap;
    }

    private HttpInfo parseMethod(Method method, String pathPatternPrefix) {
        HttpInfo httpInfo = new HttpInfo();
        this.parseHttpMethod(method, httpInfo, pathPatternPrefix);

        // 构建参数应用器列表
        List<PropertyValueApplier> paramAppliers = new ArrayList<>();
        Arrays.stream(method.getParameters()).forEach(parameter -> paramAppliers.add(this.parseParam(parameter)));
        httpInfo.setParamAppliers(paramAppliers);

        return httpInfo;
    }

    private void parseHttpMethod(Method method, HttpInfo httpInfo, String pathPatternPrefix) {
        Arrays.stream(method.getAnnotations())
                .filter(annotation -> mappingMethodAnnotations.contains(annotation.annotationType()))
                .forEach(annotation -> {
                    Method pathMethod =
                            ReflectionUtils.getDeclaredMethod(annotation.annotationType(), KEY_OF_METHOD_PATH);
                    String[] paths =
                            (String[]) ReflectionUtils.invoke(method.getAnnotation(annotation.annotationType()),
                                    pathMethod);
                    if (ArrayUtils.isEmpty(paths)) {
                        return;
                    }
                    if (paths.length > MAX_PATH_SIZE) {
                        throw new IllegalArgumentException("The path size cannot be more than one.");
                    }
                    httpInfo.setPathPattern(pathPatternPrefix + paths[0]);
                    httpInfo.setMethod(annotation.annotationType()
                            .getDeclaredAnnotation(RequestMapping.class)
                            .method()[0]);
                });
    }

    private String getPathPatternPrefix(Class<?> clazz) {
        String pathPatternPrefix = StringUtils.EMPTY;
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
            String[] paths = requestMapping.path();
            if (ArrayUtils.isNotEmpty(paths)) {
                pathPatternPrefix = paths[0];
            }
        }
        return pathPatternPrefix;
    }

    private Address getAddress(Class<?> requestAddressClazz) {
        Address address = null;
        if (requestAddressClazz.isAnnotationPresent(RequestAddress.class)) {
            address = new Address();
            RequestAddress requestAddress = requestAddressClazz.getAnnotation(RequestAddress.class);
            address.setProtocol(requestAddress.protocol());
            address.setHost(requestAddress.host());
            if (StringUtils.isNotEmpty(requestAddress.port())) {
                address.setPort(Integer.parseInt(requestAddress.port()));
            }
            address.setLocator(requestAddress.address());
        }
        return address;
    }

    private PropertyValueApplier parseParam(Parameter parameter) {
        Annotation[] annotations = parameter.getAnnotations();
        Class<?> type = parameter.getType();
        List<DestinationSetterInfo> setterInfos = this.getSetterInfos(annotations, type.getDeclaredFields(), "$");
        return new MultiDestinationsPropertyValueApplier(setterInfos, this.valueFetcher);
    }

    private List<DestinationSetterInfo> getSetterInfos(Annotation[] annotations, Field[] fields, String prefix) {
        List<DestinationSetterInfo> setterInfos = new ArrayList<>();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == RequestBean.class) {
                Arrays.stream(fields)
                        .forEach(field -> setterInfos.addAll(this.getSetterInfos(field.getAnnotations(),
                                field.getType().getDeclaredFields(),
                                prefix + "." + field.getName())));
                return setterInfos;
            } else {
                ParamResolver<?> resolver = annotationParsers.get(annotation.annotationType());
                if (resolver != null) {
                    setterInfos.add(resolver.resolve(cast(annotation), prefix));
                    return setterInfos;
                }
            }
        }
        return setterInfos;
    }

    private List<PropertyValueApplier> getClassLevelAuthAppliers(Class<?> clazz) {
        List<PropertyValueApplier> appliers = new ArrayList<>();
        RequestAuth[] authAnnotations = clazz.getAnnotationsByType(RequestAuth.class);
        for (RequestAuth auth : authAnnotations) {
            appliers.add(new StaticAuthApplier(auth, this.beanContainer));
        }
        return appliers;
    }

    private List<PropertyValueApplier> getMethodLevelAuthAppliers(Method method) {
        List<PropertyValueApplier> appliers = new ArrayList<>();
        RequestAuth[] authAnnotations = method.getAnnotationsByType(RequestAuth.class);
        for (RequestAuth auth : authAnnotations) {
            appliers.add(new StaticAuthApplier(auth, this.beanContainer));
        }
        return appliers;
    }
}