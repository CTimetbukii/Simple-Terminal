package io.github.ctimetbukii.simpleterminal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectCommandDescriptionProperties {
    /**
     * 是否启用I18n。默认为true。在启用I18n的情况下，Simple-Terminal会基于
     * <code>Locale.getDefault().getCountry()</code>
     * 返回的ISO 3166 两位字母代码查找对应的Properties。
     */
    boolean enableI18n() default true;
}
