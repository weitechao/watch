<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ page import="com.godoing.rose.lang.*" %>

<!DOCTYPE HTML>
<html>
  <head>
       <title>复制成功</title>
    <meta name="">
    <meta name="" content="">
    <style type="text/css">
    body{background-image:url(<%=request.getContextPath()%>/images/login/blue.jpg);
    	
    }
    
    a{text-decoration:none;}
    
    #success{width:800px;
    		 height:200px;
    		 margin:0 auto;
    		 text-align:center;
    		 vertical-align:middle;
    		 }
    			
    </style>
  </head>
  
  <body>
  	<div id="success">
  		<a href="<%=request.getContextPath()%>/copyFile.jsp">复制成功，点击跳转到文件复制页面</a>
  	</div>
	
  </body>
</html>
