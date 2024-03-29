package com.bethibande.web.examples.test.permission;

import com.bethibande.web.annotations.AutoLoad;
import com.bethibande.web.annotations.Path;
import com.bethibande.web.annotations.QueryField;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.examples.test.Message;
import com.bethibande.web.examples.test.SecuredContext;
import com.bethibande.web.examples.test.annotations.SecuredEntry;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;

@SuppressWarnings("unused")
@AutoLoad("secured")
public class SecuredHandler {

    @URI(value = "/set/name/[a-zA-Z0-9\\s]{3,36}", type = URI.URIType.REGEX)
    @SecuredEntry(permission = Permissions.USERNAME, access = Access.WRITE)
    public Object setName(
            SecuredContext context,
            Session session,
            @Path String path
    ) {
        session.getMeta().set("username", path.split("/")[3]);

        return new RequestResponse()
                .withStatusCode(202)
                .withContentData(Message.MessageType.OK.toMessage());
    }

    @URI("/get/name")
    @SecuredEntry(permission = Permissions.USERNAME, access = Access.READ)
    public Object getName(
            SecuredContext context,
            Session session
    ) {
        return new RequestResponse()
                .withStatusCode(202)
                .withContentData(new Message(99, session.getMeta().getString("username")));
    }

    @URI("/grantPermissions")
    public Object grantPermissions(
            SecuredContext context,
            @QueryField("not") boolean not // set not query parameter to revoke permissions instead
    ) {
        if(not) {
            context.loadPermissions();
        } else {
            context.loadPermissions(new PermissionScope()
                    .withPermission(Permissions.USERNAME, (byte)(PermissionScope.ACCESS_READ | PermissionScope.ACCESS_WRITE))
            );
        }

        return new RequestResponse()
                .withStatusCode(202)
                .withContentData(Message.MessageType.OK.toMessage());
    }

}
