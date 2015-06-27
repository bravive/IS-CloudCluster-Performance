import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.google.gson.Gson;

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
		System.out.println(request.getParameterNames().nextElement());
		System.out.println(request.getParameter("selected"));
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(getPerformance());
	}
	
	public String getPerformance() {
		Performance testPerf11 = new Performance();
		testPerf11.setDate("1");
		testPerf11.setValue("1");
		Performance testPerf12 = new Performance();
		testPerf12.setDate("2");
		testPerf12.setValue("2");
		Performances testPerfs1 = new Performances();
		testPerfs1.setId("1");
		testPerfs1.setPerformance(new Performance[] {testPerf11, testPerf12});
		Performance testPerf21 = new Performance();
		testPerf21.setDate("1");
		testPerf21.setValue("2");
		Performance testPerf22 = new Performance();
		testPerf22.setDate("2");
		testPerf22.setValue("1");
		Performances testPerfs2 = new Performances();
		testPerfs2.setId("2");
		testPerfs2.setPerformance(new Performance[] {testPerf21, testPerf22});
		Performances[] test = new Performances[2];
		test[0] = testPerfs1;
		test[1] = testPerfs2;
		Gson gson = new Gson();
		String json = gson.toJson(test);
		//[{"id":"1","performance":[{"date":"1","value":"1"},{"date":"2","value":"2"}]},{"id":"2","performance":[{"date":"1","value":"2"},{"date":"2","value":"1"}]}]
		return json;
	}
}
//these two classes is used to generate the fade performance value for debug
class Performance {
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
	private Performance[] performance;
	
	public void setId(String id) {
		this.id = id;
	}
	public void setPerformance(Performance[] performance) {
		this.performance = performance;
	}
}

//this class is used to return the status value of all the instance in the database 
class getStatusServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(getValue());
	}
	
	public String getValue() {
		Data testData = new Data();
		testData.setName("Mengye Gong");
		testData.setPosition("System Architect");
		testData.setSalary("$3,120");
		testData.setDate("2011/04/25");
		testData.setOffice("Edinburgh");
		Data testData2 = new Data();
		testData2.setName("Wei Li");
		testData2.setPosition("Software Developer");
		testData2.setSalary("$5,120");
		testData2.setDate("2013/04/4");
		testData2.setOffice("Fremond");
		//testData.setExtn("5421");
		Datas testDatas = new Datas();
		testDatas.setData(new Data[] {testData, testData2});
		Gson gson = new Gson();
		String json = gson.toJson(testDatas);
		return json;
	}
}

//these two classes is used to generate the fade status value for debug
class Datas {
	private Data[] data;
	public void setData(Data[] data) {
		this.data = data;
	}
}
class Data {
	private String name;
	private String position;
	private String salary;
	private String start_date;
	private String office;
//	private String extn;
	
	public void setName(String name) {
		this.name = name;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public void setSalary(String salary) {
		this.salary = salary;
	}
	public void setDate(String start_date) {
		this.start_date = start_date;
	}
	public void setOffice(String office) {
		this.office = office;
	}
//	public void setExtn(String extn) {
//		this.extn = extn;
//	}
}



