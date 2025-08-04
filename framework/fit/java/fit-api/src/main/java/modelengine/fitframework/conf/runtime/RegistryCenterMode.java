package modelengine.fitframework.conf.runtime;

import static modelengine.fitframework.inspection.Validation.notBlank;

import modelengine.fitframework.util.StringUtils;

import java.util.Arrays;

/**
 * 注册中心地址监听模式枚举。
 *
 * @author 董智豪
 * @since 2025-08-04
 */
public enum RegistryCenterMode {
    /** 支持本地内存实现的注册中心。 */
    MEMORY("MEMORY"),

    /** 支持 Nacos 实现的注册中心。 */
    NACOS("NACOS"),
    ;

    /** 注册中心模式标识字符串。 */
    private final String mode;

    /**
     * 构造方法，校验并设置注册中心模式标识。
     *
     * @param mode 注册中心模式标识字符串
     */
    RegistryCenterMode(String mode) {
        this.mode = notBlank(mode, "The registry center mode cannot be blank.");
    }

    /**
     * 根据字符串获取对应的注册中心模式枚举。
     *
     * @param mode 注册中心监听模式标识字符串
     * @return 匹配的 {@link RegistryCenterMode} 枚举常量，未匹配返回 null
     */
    public static RegistryCenterMode fromMode(String mode) {
        return Arrays.stream(RegistryCenterMode.values())
                .filter(registryCenterMode -> StringUtils.equals(registryCenterMode.mode, mode))
                .findFirst()
                .orElse(null);
    }
}
