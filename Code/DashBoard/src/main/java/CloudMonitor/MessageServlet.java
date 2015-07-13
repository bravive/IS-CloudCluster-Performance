import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.google.gson.Gson;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import java.sql.*;
import java.util.*;
//this class is used to load the home html 
class BasePageServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		RequestDispatcher view = request.getRequestDispatcher("tryjs.html");
		view.forward(request, response);
	}
}
//this class is used to return performance value of the instances selected
class getPerformanceServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get parameter from the request -- the selected instance
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		final String JDBC_DRIVER="com.mysql.jdbc.Driver";     //set jdbc driver
	    final String DB_URL="jdbc:mysql://localhost/cloudMonitorDB";     //set db url
	    final String USER = "root";
	    final String PASS = "";
	    Connection conn = null;
	    Statement stmt = null;
		Gson gson = new Gson();
		SelectedInstance selectedInstance = gson.fromJson(request.getParameter("selected"), SelectedInstance.class);
		String[] selectedFromClient = selectedInstance.selected;
		Performances[] performances = new Performances[selectedFromClient.length];
		for (int i = 0; i < selectedFromClient.length; i++) {        //loop for every selected instance
			//access the database and get the performance value of the selected instances
			Performances performance = new Performances();
			performance.setId(selectedFromClient[i]);
			try{
		         // register jdbc driver
		         Class.forName("com.mysql.jdbc.Driver");
		         // open the db
		         conn = DriverManager.getConnection(DB_URL,USER,PASS);
		         stmt = conn.createStatement();
		         String sql;
		         sql = "SELECT * FROM " + "`" + selectedFromClient[i] + "_CPU`";    //get all the cpu data from table
		         ResultSet rs= stmt.executeQuery(sql);
		         int cpusize= 0;    										 //get the number of the rows
		         if (rs != null){  
		        	 rs.beforeFirst();  
		        	 rs.last();  
		        	 cpusize = rs.getRow();  
		         }
		         //System.out.println(cpusize);
		         rs.beforeFirst();
		         CPU[] cpu = new CPU[cpusize];    
		         int cpuindex = 0;
		         while(rs.next()) {		        	 
		        	 String timestamp = rs.getString("timestamp");
		        	 String value = rs.getString("value");
		        	 //set a instance of CPU
		        	 CPU tmp = new CPU();
		        	 tmp.setDate(timestamp);
		        	 tmp.setValue(value);
		        	 //put the instance into the array
		        	 cpu[cpuindex++] = tmp;
		         }
		         performance.setCPU(cpu);
		         rs.close();
		         
		         sql = "SELECT * FROM " + "`" + selectedFromClient[i] + "_MEMR`";    //get all the memr data from table
		         rs= stmt.executeQuery(sql);
		         int memrsize= 0;    										  //get the number of the rows
		         if (rs != null){  
		        	 rs.beforeFirst();  
		        	 rs.last();  
		        	 memrsize = rs.getRow();  
		         }
		         //System.out.println(memrsize);
		         rs.beforeFirst();
		         MEMR[] memr = new MEMR[memrsize];    
		         int memrindex = 0;
		         while(rs.next()) {		        	 
		        	 String timestamp = rs.getString("timestamp");
		        	 String value = rs.getString("value");
		        	 //set a instance of MEMR
		        	 MEMR tmp = new MEMR();
		        	 tmp.setDate(timestamp);
		        	 tmp.setValue(value);
		        	 //put the instance into the array
		        	 memr[memrindex++] = tmp;
		         }
		         performance.setMEMR(memr);
		         rs.close();
		         
		         sql = "SELECT * FROM " + "`" + selectedFromClient[i] + "_MEMW`";    //get all the memw data from table
		         rs= stmt.executeQuery(sql);
		         int memwsize= 0;    										  //get the number of the rows
		         if (rs != null){  
		        	 rs.beforeFirst();  
		        	 rs.last();  
		        	 memwsize = rs.getRow();  
		         }
		         //System.out.println(memwsize);
		         rs.beforeFirst();
		         MEMW[] memw = new MEMW[memwsize];    
		         int memwindex = 0;
		         while(rs.next()) {		        	 
		        	 String timestamp = rs.getString("timestamp");
		        	 String value = rs.getString("value");
		        	 //set a instance of MEMW
		        	 MEMW tmp = new MEMW();
		        	 tmp.setDate(timestamp);
		        	 tmp.setValue(value);
		        	 //put the instance into the array
		        	 memw[memwindex++] = tmp;
		         }
		         performance.setMEMW(memw);
		         rs.close();

		         sql = "SELECT * FROM " + "`" + selectedFromClient[i] + "_DIO`";    //get all the dio data from table
		         rs= stmt.executeQuery(sql);
		         int diosize= 0;    										 //get the number of the rows
		         if (rs != null){  
		        	 rs.beforeFirst();  
		        	 rs.last();  
		        	 diosize = rs.getRow();  
		         }
		         //System.out.println(diosize);
		         rs.beforeFirst();
		         DIO[] dio = new DIO[diosize];    
		         int dioindex = 0;
		         while(rs.next()) {		        	 
		        	 String timestamp = rs.getString("timestamp");
		        	 String value = rs.getString("value");
		        	 //set a instance of DIO
		        	 DIO tmp = new DIO();
		        	 tmp.setDate(timestamp);
		        	 tmp.setValue(value);
		        	 //put the instance into the array
		        	 dio[dioindex++] = tmp;
		         }  
		         performance.setDIO(dio);
		         rs.close();
		         // clear
		         stmt.close();
		         conn.close();
		    }
			catch(SQLException se){
		         // handle JDBC error
				//se.printStackTrace();
		    }
			catch(Exception e){
		         // handle Class.forName error
		         //e.printStackTrace();
		    }
			finally{
		         // close
		         try{
		        	 if(stmt!=null)
		             stmt.close();
		         }
		         catch(SQLException se2){
		         }
		         try{
		            if(conn!=null)
		            conn.close();
		         }
		         catch(SQLException se){
		            //se.printStackTrace();
		         }//end finally try
		    } //end try
			performances[i] = performance;
		}
		response.getWriter().write(getPerformance(performances));
	}
	
	public String getPerformance(Performances[] performances) {
		Gson gson = new Gson();
		String json = gson.toJson(performances);
		//[{"id":"1","cpu":[{"date":"1","value":"1"},{"date":"2","value":"2"}],"memr":[{"date":"1","value":"1"},{"date":"2","value":"2"}],"memw":[{"date":"1","value":"1"},{"date":"2","value":"2"}],"dio":[{"date":"1","value":"1"},{"date":"2","value":"2"}]},{"id":"2",...}]
		return json;
	}
}

class SelectedInstance {
	String[] selected;
}

class CPU {
	private String date;
	private String value;
	
	public void setDate(String date) {
		this.date = date;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
class MEMR {
	private String date;
	private String value;
	
	public void setDate(String date) {
		this.date = date;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
class MEMW {
	private String date;
	private String value;
	
	public void setDate(String date) {
		this.date = date;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
class DIO {
	private String date;
	private String value;
	
	public void setDate(String date) {
		this.date = date;
	}
	public void setValue(String value) {
		this.value = value;
	}
}

class Performances {
	private String id;
	private CPU[] cpu;
	private MEMR[] memr;
	private MEMW[] memw;
	private DIO[] dio;
	
	public void setId(String id) {
		this.id = id;
	}
	public void setCPU(CPU[] cpu) {
		this.cpu = cpu;
	}
	public void setMEMR(MEMR[] memr) {
		this.memr = memr;
	}
	public void setMEMW(MEMW[] memw) {
		this.memw = memw;
	}
	public void setDIO(DIO[] dio) {
		this.dio = dio;
	}
}

//this class is used to return the status value of all the instance in the database 
class getStatusServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		final String JDBC_DRIVER="com.mysql.jdbc.Driver";     //set jdbc driver
	    final String DB_URL="jdbc:mysql://localhost/cloudMonitorDB";     //set db url
	    final String USER = "root";
	    final String PASS = "";
	    Connection conn = null;
	    Statement stmt = null;
	    
	    try{
	         // register jdbc driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // open the db
	         conn = DriverManager.getConnection(DB_URL,USER,PASS);
	         stmt = conn.createStatement();
	         String sql;
	         sql = "SELECT * FROM instanceInfo";    //get all the data from table instanceInfo
	         ResultSet rs= stmt.executeQuery(sql);
	         int size= 0;    						//get the number of the rows
	         if (rs != null){  
	        	 rs.beforeFirst();  
	        	 rs.last();  
	        	 size = rs.getRow();  
	         }
	         
	         rs.beforeFirst();
	         InstanceStatus[] instances = new InstanceStatus[size];    
	         int i = 0;
	         while(rs.next()) { 	 
	        	 String instanceDNS = rs.getString("instanceDNS");
	        	 String status = rs.getString("status");
	        	 //set a instance of InstanceStatus
	        	 InstanceStatus tmp = new InstanceStatus();
	        	 tmp.setInstanceDNS(instanceDNS);
	        	 tmp.setStatus(status);
	        	 //put the instance into the array
	        	 instances[i++] = tmp;
	         }
	         
	         response.getWriter().write(setInstanceStatus(instances));

	         // clear
	         rs.close();
	         stmt.close();
	         conn.close();
	    }
	    catch(SQLException se){
	        // handle JDBC error
	    	//se.printStackTrace();
	    }
	    catch(Exception e){
	        // handle Class.forName error
	    	//e.printStackTrace();
	    }
	    finally{
	        // close
	        try{
	        	if(stmt!=null)
	        	stmt.close();
	        }
	        catch(SQLException se2){
	        }
	        try{
	            if(conn!=null)
	            conn.close();
	        }
	        catch(SQLException se){
	            //se.printStackTrace();
	        }//end finally try
	    } //end try
	}
	//use the array to generate the json
	public String setInstanceStatus(InstanceStatus[] instances) {
		Instances instancesAll = new Instances();
		instancesAll.setInstances(instances);
		Gson gson = new Gson();
		String json = gson.toJson(instancesAll);
		return json;
	}
}

class Instances {
	private InstanceStatus[] data;
	public void setInstances(InstanceStatus[] instances) {
		this.data = instances;
	}
}

class InstanceStatus {
	private String instanceDNS;
	private String status;
	
	public void setInstanceDNS(String instanceDNS) {
		this.instanceDNS = instanceDNS;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}