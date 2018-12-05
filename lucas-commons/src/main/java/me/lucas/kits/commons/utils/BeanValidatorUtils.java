package me.lucas.kits.commons.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * Created by zhangxin on 2018/12/4-4:30 PM.
 *
 * @author zhangxin
 * @version 1.0
 */
public class BeanValidatorUtils {

    /**
     * 验证某个bean的参数, 若校验通过则输出null对象.
     *
     * @param object 被校验的参数
     */
    public static <T> String validate(T object, Class<?>... groups) {
        //获得验证器
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        //执行验证
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object, groups);
        //如果有验证信息，则取出来包装成异常返回
        if (CollectionUtils.isEmpty(constraintViolations)) {
            return null;
        } else {
            return convertErrorMsg(constraintViolations);
        }
    }

    /**
     * 转换异常信息
     */
    private static <T> String convertErrorMsg(Set<ConstraintViolation<T>> cvs) {
        Map<String, StringBuilder> errorMap = new HashMap<>();
        String property;
        for (ConstraintViolation<T> cv : cvs) {
            //这里循环获取错误信息，可以自定义格式
            property = cv.getPropertyPath().toString();
            if (errorMap.get(property) != null) {
                errorMap.get(property).append("," + cv.getMessage());
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(cv.getMessage());
                errorMap.put(property, sb);
            }
        }
        return errorMap.toString();
    }
}
