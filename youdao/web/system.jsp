<%--
  Created by IntelliJ IDEA.
  User: YW
  Date: 2017/4/1
  Time: 17:22
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>后台管理</title>
  </head>
  <body>
  <form method="post" action="/system" >
    <label>操作:</label>
    <select name="type">
      <option name="clear" value="clear" >清除临时文件</option>
      <option name="clearCache" value="clearCache" >清除缓存</option>
    </select>
    <input type="submit" value="执行" >
  </form>
  ${message}
  </body>
</html>
