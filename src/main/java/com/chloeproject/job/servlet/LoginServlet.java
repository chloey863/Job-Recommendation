package com.chloeproject.job.servlet;

import com.chloeproject.job.db.MySQLConnection;
import com.chloeproject.job.entity.LoginRequestBody;
import com.chloeproject.job.entity.LoginResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    /**
     *
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        LoginRequestBody body = mapper.readValue(request.getReader(), LoginRequestBody.class);
        MySQLConnection connection = new MySQLConnection();
        LoginResponseBody loginResponseBody;

        // verify if userId and password matches what's stored in the database
        if (connection.verifyLogin(body.userId, body.password)) {
            // if verify successfully, create session for first time login and store the session in server memory
            HttpSession session = request.getSession();
            // set attributes to the session id
            session.setAttribute("user_id", body.userId);
            // response the success message and user full name to client
            loginResponseBody = new LoginResponseBody("OK", body.userId, connection.getFullname(body.userId));
        } else {
            loginResponseBody = new LoginResponseBody("Login failed, user id and passcode do not exist.", null, null);
            response.setStatus(401);
        }
        connection.close();

        response.setContentType("application/json");
        mapper.writeValue(response.getWriter(), loginResponseBody);
    }
}
