import java.io.File;

import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;


public class DashBoard {
	public static final String MYAPP = "/";
	public static void main(final String[] args) {
		try {
			DeploymentInfo servletBuilder = deployment()
				.setClassLoader(DashBoard.class.getClassLoader())
				.setContextPath(MYAPP)
				.setDeploymentName("DashBoard.war")
				.setResourceManager(new FileResourceManager(new File("WebContent"), 1024))
				.addServlets(
						servlet("MessageServlet", MessageServlet.class)
							.addInitParam("message", "test")
							.addMapping("/test"));

			DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
			manager.deploy();
			
			HttpHandler servletHandler = manager.start();
			PathHandler path = Handlers.path(Handlers.redirect(MYAPP)).addPrefixPath(MYAPP, servletHandler);
	
			Undertow server = Undertow.builder()
				.addHttpListener(8080, "0.0.0.0") //For local machine
				.setHandler(path)
				.build();
			server.start();
			
		} catch (ServletException e) {
			throw new RuntimeException(e);
		}
	}
}

