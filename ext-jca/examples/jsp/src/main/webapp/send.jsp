<%
     response.setContentType("text/plain");
     String jndiName = request.getParameter("jndi");
     String message = request.getParameter("message");
     String address = request.getParameter("address");
     if (message != null && message.trim().length() > 0 
	   && jndiName != null && jndiName.trim().length() > 0)
     {
	javax.naming.InitialContext ctx = null;
        org.vertx.java.resourceadapter.VertxConnection conn = null;	
	try
	{
	   ctx = new javax.naming.InitialContext();
	   org.vertx.java.resourceadapter.VertxConnectionFactory connFactory = (org.vertx.java.resourceadapter.VertxConnectionFactory)ctx.lookup(jndiName);
	   conn = connFactory.getVertxConnection();
	   conn.eventBus().send(address, message);
           out.println("OK");
           out.flush();
	}
	catch (Exception e)
	{
          out.println(e.getMessage());
          out.flush();
  	}
	finally
	{
	   if (ctx != null)
	   {
	      ctx.close();  
	   }
	   if (conn != null)
	   {
	      conn.close();  
	   }
	}
     }
     else {
       response.sendError(400, "Wrong parameters");
    }
%>
