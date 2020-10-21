package com.chloeproject.job.servlet;

import com.chloeproject.job.db.MySQLConnection;
import com.chloeproject.job.entity.HistoryRequestBody;
import com.chloeproject.job.entity.Item;
import com.chloeproject.job.entity.ResultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {
    /**
     * Do Post to set new favorite Item to database.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // pre-processing: check if the session is in the server memory (i.e. check if user logged in)
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        // if session is valid, then proceeds
        response.setContentType("application/json");

        HistoryRequestBody body = mapper.readValue(request.getReader(), HistoryRequestBody.class);

        // connect to database and set favorite items
        MySQLConnection connection = new MySQLConnection();
        connection.setFavoriteItems(body.userId, body.favorite);
        connection.close();

        // write successful saved response to client
        ResultResponse resultResponse = new ResultResponse("SUCCESS");
        mapper.writeValue(response.getWriter(), resultResponse);
    }

    /**
     * Get the favorite items as response to client, so that the front-end code
     * will make a solid heart based on this data.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // check if the session is in the server memory (i.e. check if user logged in)
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        // if session is valid, then proceeds
        response.setContentType("application/json");

        String userId = request.getParameter("user_id");

        // connect to database and get favorite items
        MySQLConnection connection = new MySQLConnection();
        Set<Item> items = connection.getFavoriteItems(userId);
        connection.close();

        // write favorite items got in response to client
        mapper.writeValue(response.getWriter(), items);
        //Fixed Bug: can only write to response one time (write one time and close automatically), because getWriter() will return
    }

    /**
     * Do Delete by calling unsetFavoritedItem() from MySQLConnection, so that the front-end code
     * will change the solid heart to be a empty heart.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // check if the session is in the server memory (i.e. check if user logged in)
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        // if session is valid, then proceeds
        response.setContentType("application/json");

        HistoryRequestBody body = mapper.readValue(request.getReader(), HistoryRequestBody.class);

        // connect to database and delete favorite items
        MySQLConnection connection = new MySQLConnection();
        connection.unsetFavoriteItems(body.userId, body.favorite.getId());
        connection.close();

        // write successful deleted response to client
        ResultResponse resultResponse = new ResultResponse("SUCCESS");
        mapper.writeValue(response.getWriter(), resultResponse);
    }
}
