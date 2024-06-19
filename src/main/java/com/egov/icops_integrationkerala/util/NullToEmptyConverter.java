package com.egov.icops_integrationkerala.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
@Slf4j
public class NullToEmptyConverter {

    public void convertNullFieldsToEmptyString(Object obj) {
        if (obj == null) {
            return;
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.getType() == String.class && field.get(obj) == null) {
                    field.set(obj, "");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
