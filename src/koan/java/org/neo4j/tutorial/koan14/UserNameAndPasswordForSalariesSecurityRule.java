package org.neo4j.tutorial.koan14;

import javax.servlet.http.HttpServletRequest;

import org.neo4j.server.rest.security.SecurityRule;

public class UserNameAndPasswordForSalariesSecurityRule implements SecurityRule
{
    public boolean isAuthorized( HttpServletRequest httpServletRequest )
    {
        boolean loggedIn = false;

        // YOUR CODE GOES HERE

        return loggedIn;
    }

    public String forUriPath()
    {
        String uriPath = null;

        // YOUR CODE GOES HERE

        return uriPath;
    }

    public String wwwAuthenticateHeader()
    {
        String message = null;

        // YOUR CODE GOES HERE

        return message;
    }
}
