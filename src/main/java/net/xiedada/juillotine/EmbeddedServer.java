/*
 * Copyright 2025 Xie Dada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.xiedada.juillotine;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

public class EmbeddedServer {

    // 默认端口
    private static final int DEFAULT_PORT = 9090;
    // 环境变量名
    private static final String PORT_ENV_VAR = "PORT";

    @Provider
    public static class DebugFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            System.out.println("DEBUG: Request URI: " + requestContext.getUriInfo().getRequestUri());
            System.out.println("DEBUG: Path: " + requestContext.getUriInfo().getPath());
        }
    }

    public static void main(String[] args) throws Exception {
        int port = getPort();

        System.out.println("========================================");
        System.out.println("Juillotine URL Shortener Service");
        System.out.println("========================================");
        System.out.println("Starting embedded Jetty server on port " + port);
        System.out.println("API endpoint: http://localhost:" + port + "/*");
        // System.out.println("Health check: http://localhost:" + port + "/health");
        System.out.println("========================================");

        Server server = new Server(port);

        // 创建Servlet上下文
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        // 配置Jersey Servlet programmatically
        ResourceConfig config = new ResourceConfig();
        config.register(net.xiedada.juillotine.res.Resource.class);
        config.register(DebugFilter.class);
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
        context.addServlet(jerseyServlet, "/*");
        jerseyServlet.setInitOrder(0);

//        // 添加健康检查Servlet
//        ServletHolder healthServlet = context.addServlet(
//            HealthCheck.class,
//            "/health"
//        );
//        healthServlet.setInitOrder(1);

        // 启动服务器
        server.start();
        server.join();
    }

    /**
     * 从环境变量获取端口，默认为8080
     */
    private static int getPort() {
        String portStr = System.getenv(PORT_ENV_VAR);
        if (portStr != null && !portStr.isEmpty()) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid PORT value '" + portStr +
                    "', using default " + DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }
}
