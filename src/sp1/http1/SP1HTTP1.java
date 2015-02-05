package sp1.http1;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Lars Mortensen && Martin Høigaard Rasmussen
 */
public class SP1HTTP1 {

    static int port = 8080;
    static String ip = "127.0.0.1";
    static String contentFolder = "public/";

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            port = Integer.parseInt(args[0]);
            ip = args[0];
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(ip, port), 0);
        server.createContext("/welcome", new RequestHandler());
        server.createContext("/headers", new RequestHandlerHeaders());
        server.createContext("/pages", new RequestHandlerPages());
        server.createContext("/parameters", new RequestHandlerParameters());
        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("Server started, listening on port: " + port);
    }

    static class RequestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response;
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>My fancy Web Site</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");
            sb.append("<h2>Welcome to my very first home made Web Server :-)</h2>\n");
            sb.append("</body>\n");
            sb.append("</html>\n");
            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response); //What happens if we use a println instead of print --> Explain
            }
        }
    }

    static class RequestHandlerHeaders implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {

            String response;
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>Headers</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");

            sb.append("<table border='1'>"
                    + "<tr>"
                    + "<th>Header</th>"
                    + "<th>Value</th>"
                    + "</tr>"
                    + "");

            Headers rqh = he.getRequestHeaders();
            Set keySet = rqh.keySet();
            Iterator it = keySet.iterator();
            System.out.println("keyset Size: " + keySet.size());
            Object[] array = keySet.toArray();

            for (Object key : array) {
                List<String> value = rqh.get(key);
                sb.append("<tr>\n<td>");
                sb.append(key);
                sb.append("</td>\n<td>");
                sb.append(value);
                sb.append("</td>\n</tr>\n");
            }
            sb.append("</table>\n");

            sb.append("</body>\n");
            sb.append("</html>\n");
            response = sb.toString();
            Headers rsh = he.getResponseHeaders();
            rsh.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response);
            }
        }
    }

    static class RequestHandlerPages implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String str = he.getRequestURI().toString();
            System.out.println(str);
            String[] split = str.split("/", 3);
            System.out.println("Length of split uri string: " + split.length);

            File file = new File(contentFolder + "index.html");

            if (split.length == 3) {
                String path = split[2];
                file = new File(contentFolder + path);
            }

            byte[] bytesToSend = new byte[(int) file.length()];
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytesToSend, 0, bytesToSend.length);
            } catch (IOException ie) {
                ie.printStackTrace();
                //error handling if file is not found, shows a 404 message
                String response = "<h1>404 Not Found</h1>No context found for request";
                Headers h = he.getResponseHeaders();
                h.add("Content-Type", "text/html");
                he.sendResponseHeaders(200, response.length());
                try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                    pw.print(response);
                }
            }

            if (split.length > 2) {
                String[] content = split[2].split("\\.");
                if (content[1].equalsIgnoreCase("jpg")) {
                    Headers h = he.getResponseHeaders();
                    h.add("Content-Type", "image/jpeg");
                }
                if (content[1].equalsIgnoreCase("pdf")) {
                    Headers h = he.getResponseHeaders();
                    h.add("Content-Type", "application/pdf");
                }
                if (content[1].equalsIgnoreCase("jar")) {
                    Headers h = he.getResponseHeaders();
                    h.add("Content-Type", "application/java-archive");
                }
                if (content[1].equalsIgnoreCase("html")) {
                    Headers h = he.getResponseHeaders();
                    h.add("Content-Type", "text/html");
                }
            }
            if (split.length == 2) {
                Headers h = he.getResponseHeaders();
                h.add("Content-Type", "text/html");
            }

            he.sendResponseHeaders(200, bytesToSend.length);

            try (OutputStream os = he.getResponseBody()) {
                os.write(bytesToSend, 0, bytesToSend.length);
            }
        }
    }

    static class RequestHandlerParameters implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response;
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>Parameters</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");

            String str = he.getRequestMethod();

            sb.append("Method is: ");
            sb.append(str);

            if (str.equalsIgnoreCase("get")) {
                str = he.getRequestURI().getQuery();
                sb.append("<br>Get-Parameters: ");
                sb.append(str);
            }
            if (str.equalsIgnoreCase("post")) {
                Scanner scan = new Scanner(he.getRequestBody());
                while (scan.hasNext()) {
                    sb.append("<br>Request body, with Post-parameters: " + scan.nextLine());
                    sb.append("</br>");
                }
            }

            sb.append("\n</body>\n");
            sb.append("</html>\n");
            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response);
            }
        }
    }
}
