package com.shareniu.flowablemodeler7.resource;

import com.shareniu.flowablemodeler7.util.UserUtil;
import org.flowable.idm.api.User;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.security.SecurityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/hr")
public class ShareniuRemoteAccountResource {

    /**
     * GET /rest/account -> get the current user.
     */
    @RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces = "application/json")
    public UserRepresentation getAccount() {
        User user = UserUtil.getUser();
        SecurityUtils.assumeUser(user);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("hrplusplus");
        userRepresentation.setFirstName("hrplusplus");
        List<String> privileges=new ArrayList<>();
        privileges.add("flowable-idm");
        privileges.add("flowable-modeler");
        privileges.add("flowable-task");
        userRepresentation.setPrivileges(privileges);
        return  userRepresentation;

    }
}
