<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Error!</title>
<body>
<style type= "text/css"><%@include file="/resources/css/styles.css"%></style>
<% switch (response.getStatus()) {
    case 404: %>
        <div class = "error"> Error 404: Not found :(</div>
        <div class = "errorMessage"> Sorry ;( </div>
        <div id = "content404">
            <embed class = "crying" src="/resources/images/crying0.gif"></embed>
            <embed class = "crying" src="/resources/images/crying1.gif"></embed>
            <embed class = "crying" src="/resources/images/crying2.gif"></embed>
            <embed class = "crying" src="/resources/images/crying3.gif"></embed>
            <embed class = "crying" src="/resources/images/crying4.gif"></embed>
            <embed class = "crying" src="/resources/images/crying6.gif"></embed>
            <embed class = "crying" src="/resources/images/crying7.gif"></embed>
            <embed class = "crying" src="/resources/images/crying8.gif"></embed>
        </div>
    <%  break;
    case 400: %>
        <div class = "error"> Error 400: Bad request</div>
        <div class = "errorMessage"> Before <a href="/mainPage.html">trying again</a>, relax and listen to good music! :) </div>
        <div id = "content400">
            <embed src="/resources/images/the-beatles.png" width = "130%" align = "center"></embed>
        </div>
        <embed src="/resources/music/Come together.mp3" hidden = "true"></embed>
    <%  break;
    case 500: %>
        <div class = "error"> Error 500: Internal Server Error</div>
        <div class = "errorMessage"> You'd better travel, really! But don't forget to come back to our <a href="/mainPage.html">chatty <3</a> :) </div>
        <div id = "content500">
            <embed src="/resources/images/travel.png" width = "140%" align = "center"></embed>
        </div>
        <script>setTimeout(function() {window.open("http://www.letstravelsomewhere.com/travel/travel-inspiration/","_blank")}, 4000);</script>
    <%  break;
}%>
</body>
</html>