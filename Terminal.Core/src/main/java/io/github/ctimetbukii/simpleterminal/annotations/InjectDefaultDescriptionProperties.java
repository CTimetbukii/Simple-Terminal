package io.github.ctimetbukii.simpleterminal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectDefaultDescriptionProperties {
//    boolean enableI18n() default true;  //不需要这个。因为I18n在挂载Properties时已经自动完成
}
