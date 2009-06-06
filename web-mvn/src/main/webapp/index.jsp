<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@page pageEncoding="UTF-8" import="org.talkingpuffin.twitter.*,org.talkingpuffin.ui.web.*" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>TalkingPuffin Friends and Followers Summary V0.1</title>
    <link href="main.css" rel="stylesheet" type="text/css"/>
</head>

<body>
<h1>TalkingPuffin Friends and Followers Summary V0.1</h1>

<table><tr>
    <td><img src="davehead-small.jpg" alt="Picture of Dave Briccetti"/></td>
    <td valign="top">Thank you for trying this early, experimental web application,
        which shows a summary of the people who follow you and those you follow.
    <p>    
    <a href="http://TalkingPuffin.org">Desktop client and more information</a>
    </p><p>Dave Briccetti, <a href="http://twitter.com/dcbriccetti">dcbriccetti</a></p></td>
</tr></table>

<%
    final String userParm = request.getParameter("user");
    final String passwordParm = request.getParameter("password");
    if (userParm == null || passwordParm == null || 
            userParm.trim().length() == 0 || passwordParm.trim().length() == 0) {
%>

<p>Enter your Twitter user name and password, and push <b>Log In</b>. After some time,
you should see a summary of your friends and followers. Your credentials will be used
only for the interaction with Twitter, and will not be stored. There is no error handling,
including for invalid credentials. Only the first 1,000 of your friends and followers 
will be shown.</p>    

<form id="form" method="post" action="index.jsp">
    <table>
        <tr>
            <td><label for="user">User</label></td>
            <td><input type="text" id="user" name="user"/></td>
        </tr>
        <tr>
            <td><label for="password">Password</label></td>
            <td><input type="password" id="password" name="password"/></td>
        </tr>
    </table>
    
    <input type="submit" id="login" value="Log In" />
</form>

<%
    } else {
%>

<table class="usersTable">
    <tr>
        <th> </th>
        <th>Image</th>
        <th>Name</th>
        <th>Frnds</th>
        <th>Flwrs</th>
        <th>Location</th>
        <th>Description</th>
        <th>Status</th>
    </tr>
<% 

    AuthenticatedSession s = new AuthenticatedSession(userParm,passwordParm);
    Users u = new Users();
    u.setSession(s);
    final TwitterUser[] userRows = u.getUsers();
    for (TwitterUser user: userRows) {
%>
<tr>
    <td><%= u.getArrows(user) %></td>
    <td><img alt="Thumbnail" height="48" width="48" src="<%= user.profileImageURL() %>"/></td>
    <td><%= user.name() %><br/>
    <a href="http://twitter.com/<%=user.screenName()%>"><%= user.screenName() %></a></td>
    <td class="number"><%= user.friendsCount() %></td>
    <td class="number rightmostNumber"><%= user.followersCount() %></td>
    <td><%= user.location() %></td>
    <td><%= user.description() %></td>
    <% if(user.status() != null){ %>
        <td><%= user.status().text()%></td>
    <% } else { %>
        <td>N/A</td>
    <% } %>
</tr>
<%
    }
%>
</table>

<%
    }
%>
<script src='http://davebsoft.com/scripts/analytics.js' type="text/javascript"></script>

</body>
</html>
