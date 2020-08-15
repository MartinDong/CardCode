package gui.utils;

import java.lang.reflect.Field;

public class MathUtils {
    public static void swap(Double a, Double b) {
        if (a == null || b == null) {
            return;
        }
        //获得a的class对象
        Class<Double> integerClass = (Class<Double>) a.getClass();
        try {
            //获得value属性
            Field value=integerClass.getDeclaredField("value");
            //设置权限为可访问
            value.setAccessible(true);
            //交换
            double temp=a;
            value.setDouble(a,b);
            value.setDouble(b,temp);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
