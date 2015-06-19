import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MessageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String MESSAGE = "message";
    private String message;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        message = config.getInitParameter(MESSAGE);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
//        PrintWriter writer = response.getWriter();
//        writer.write(message);
//        writer.close();
        String query = request.getParameter("para");
		if (query==null) {
			System.out.println("here");
			response.setContentType("text/html");
			RequestDispatcher d = request.getRequestDispatcher("WEB-INF/index.html");
			d.forward(request, response);
		} else if (query.equals("")) {
			response.setContentType("text/html");
			RequestDispatcher d = request.getRequestDispatcher("index.html");
			d.forward(request, response);
		}
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
       doGet(req, resp);
    }
}
