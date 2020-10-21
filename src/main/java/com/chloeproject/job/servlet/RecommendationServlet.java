package com.chloeproject.job.servlet;

import com.chloeproject.job.entity.Item;
import com.chloeproject.job.entity.ResultResponse;
import com.chloeproject.job.recommendation.Recommendation;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "RecommendationServlet", urlPatterns = "/recommendation")
public class RecommendationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // check if the session is in the server memory (i.e. check if user logged in)
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403); //access to the resources if forbidden
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        // if session is valid, then proceeds tp recommend items
        String userId = request.getParameter("user_id");
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));

        Recommendation recommendation = new Recommendation();
        List<Item> items = recommendation.recommendItems(userId, lat, lon);
        mapper.writeValue(response.getWriter(), items);

//        response.getWriter().write("This is RecommendationServlet");
    }
}
