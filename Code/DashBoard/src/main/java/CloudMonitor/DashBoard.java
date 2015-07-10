
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
	public static void main(final String[] args) throws ServletException {
		DeploymentInfo servletBuilder = deployment()
        .setClassLoader(DashBoard.class.getClassLoader())
        .setContextPath("/app")
        .setDeploymentName("DashBoard.war")
        .setResourceManager(new FileResourceManager(new File("myapp"), 1024))
        .addServlets(
                servlet("BasePageServlet", BasePageServlet.class)
                        .addMapping(""),
                servlet("GetStatusServlet", getStatusServlet.class)
                        .addMapping("/status"),
				servlet("GetPerformanceServlet", getPerformanceServlet.class)
		        		.addMapping("/performance"));
                       
                

		DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
		manager.deploy();
		PathHandler path = Handlers.path(Handlers.redirect("/app"))
		        .addPrefixPath("/app", manager.start());

		Undertow server = Undertow.builder()
		        .addHttpListener(8080, "0.0.0.0")
		        .setHandler(path)
		        .build();
		server.start();
	}
}

