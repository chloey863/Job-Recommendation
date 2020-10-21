package com.chloeproject.job.servlet;

import com.chloeproject.job.entity.ResultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
    // normally logout use POST method 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get the existed session id
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate(); // delete the session id from server memory when user log out
        }

        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");
        ResultResponse resultResponse = new ResultResponse("OK");
        mapper.writeValue(response.getWriter(), resultResponse);
    }

    // but currently implement doGet for front-end
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get the existed session id
        HttpSession session = request.getSession(false);
        // delete the session id from server memory when user log out
        if (session != null) {
            session.invalidate();
        }

        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");
        ResultResponse resultResponse = new ResultResponse("OK");
        mapper.writeValue(response.getWriter(), resultResponse);
    }
}
