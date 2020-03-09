package com.shareniu.flowablemodeler7.util;

import org.flowable.idm.api.User;
import org.flowable.idm.engine.impl.persistence.entity.UserEntityImpl;

public class UserUtil {
    public static User getUser() {
        User user = new UserEntityImpl();
        user.setId("hrplusplus");
        return user;
    }
}
