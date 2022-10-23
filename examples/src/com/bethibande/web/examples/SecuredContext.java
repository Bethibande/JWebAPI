package com.bethibande.web.examples;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.examples.permission.PermissionScope;
import com.bethibande.web.examples.permission.Permissions;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.sun.net.httpserver.HttpExchange;

public class SecuredContext extends ServerContext {

    public SecuredContext(JWebServer server, Session session, HttpExchange exchange, WebRequest request) {
        super(server, session, exchange, request);
    }

    public void loadPermissions(PermissionScope scope) {
        session().getMeta().set("permissionContext", scope);
    }

    public void loadPermissions() {
        // if the user is logged in, load the users permissions from your database right here

        // or load default permissions if the user is not logged in
        loadPermissions(new PermissionScope()
                .withPermission(Permissions.USERNAME, PermissionScope.ACCESS_READ)
        );
    }

    public PermissionScope getPermissions() {
        if(!session().getMeta().hasMeta("permissionContext")) loadPermissions();

        return session().getMeta().getAsType("permissionContext", PermissionScope.class);
    }

    private void sendAccessDeniedResponse() {
        request().setResponse(new RequestResponse()
                .withStatusCode(403)
                .withContentData(Message.MessageType.ACCESS_DENIED.toMessage())
        );
    }

    public boolean canRead(Permissions permission) {
        boolean b = getPermissions().canRead(permission);
        if(!b) sendAccessDeniedResponse();

        return b;
    }

    public boolean canWrite(Permissions permissions) {
        boolean b = getPermissions().canWrite(permissions);
        if(!b) sendAccessDeniedResponse();

        return b;
    }

}
