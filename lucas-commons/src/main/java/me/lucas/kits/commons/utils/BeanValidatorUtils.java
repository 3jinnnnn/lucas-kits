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

    enum OutputType {
        /** 参数,错误信息 MAP */MAP,
        /** 所有错误信息 */ALL_STRING,
        /** 单一错误信息 */SIMPLE_STRING,
    }

    /**
     * 验证某个bean的参数, 若校验通过则输出null对象.
     *
     * @param object 被校验的参数
     */
    public static <T> String validate(T object, Class<?>... groups) {
        return validate(object, OutputType.SIMPLE_STRING, groups);
    }

    public static <T> String validate(T object, OutputType outputType, Class<?>... groups) {
        if (outputType == null) {
            outputType = OutputType.SIMPLE_STRING;
        }
        //获得验证器
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        //执行验证
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object, groups);
        //如果有验证信息，则取出来包装成异常返回
        if (CollectionUtils.isEmpty(constraintViolations)) {
            return null;
        } else {
            switch (outputType) {
            case MAP:
                return convertErrorMsg(constraintViolations);
            case ALL_STRING:
                return convertErrorMsgAllString(constraintViolations);
            case SIMPLE_STRING:
                return convertErrorMsgSimpleString(constraintViolations);
            default:
                return convertErrorMsgSimpleString(constraintViolations);
            }
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

    /**
     * 转换异常信息
     */
    private static <T> String convertErrorMsgAllString(Set<ConstraintViolation<T>> cvs) {
        StringBuilder sb = new StringBuilder();
        String property;
        for (ConstraintViolation<T> cv : cvs) {
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append(cv.getMessage());
        }
        return sb.toString();
    }

    /**
     * 转换异常信息
     */
    private static <T> String convertErrorMsgSimpleString(Set<ConstraintViolation<T>> cvs) {
        StringBuilder sb = new StringBuilder();
        String property;
        for (ConstraintViolation<T> cv : cvs) {
            sb.append(cv.getMessage());
            break;
        }
        return sb.toString();
    }
}
