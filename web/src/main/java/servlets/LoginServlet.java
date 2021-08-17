package servlets;
import engine.Engine;
import engine.traveler.Traveler;
import servlets.utils.ContextServletUtils;
import servlets.utils.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;


@WebServlet(name="LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletUtils servletUtils = new ServletUtils(req);
        Engine engine = ContextServletUtils.getEngine(req);
        User newUser = (User)servletUtils.gsonFromJson(User.class);

        try {
            Traveler user = engine.login(newUser.emailAddress,newUser.password);
            servletUtils.writeJsonResponse(user);
        }catch (SQLException | Traveler.NotFoundException e){
            servletUtils.writeJsonResponse("error", e.getMessage());
        }

        try (PrintWriter out = resp.getWriter()) {
            out.println(servletUtils.createOutResponse());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    static class User {
        String emailAddress;
        String password;

    }


}
