package io.github.ctimetbukii.simpleterminal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 向指定字段注入Properties对象。Simple-Terminal会基于
 * <code>Locale.getDefault().getCountry()</code>
 * 返回的ISO 3166 两位字母代码查找对应的Properties文件，
 * 自动完成国际化，然后加载对应的文件，最后将加载好的Properties对象
 * 注入到被@InjectCommandDescriptionProperties标记的
 * 字段中
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectCommandDescriptionProperties {
}
