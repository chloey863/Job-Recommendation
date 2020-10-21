package com.chloeproject.job.servlet;

import com.chloeproject.job.db.MySQLConnection;
import com.chloeproject.job.entity.Item;
import com.chloeproject.job.entity.ResultResponse;
import com.chloeproject.job.external.GitHubClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@WebServlet(name = "SearchServlet", urlPatterns = "/search")
public class SearchServlet extends HttpServlet {
    /**
     * GitHub Client performs the search (for a logged-in user) based on the client's keyword and geolocation,
     * the results are then stored as a list of Items. A Jaskson ObjectMapper is then used to
     * map the list of Items into a JSON payload and forward it to the client as response
     *
     * @param request a search job request from http client (HttpServletRequest)
     * @param response a response to be sent to http client (HttpServletResponse)
     *
     * @throws ServletException in case when the servlet encounters difficulty
     * @throws IOException in case of a problem or the connection was aborted
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // pre-processing: check if the session is in the server memory (i.e. check if user logged in)
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        // if session is valid, then proceeds
        String userId = request.getParameter("user_id");
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));

        // Make search result aware of favorite items so that the heart(solid/empty) can be display on front-end webpage:
        // getFavoriteItemIds based on the userId, so that the frontend code
        // can decide whether to show an empty or solid heart.
        MySQLConnection connection = new MySQLConnection();
        Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
        connection.close();

        GitHubClient client = new GitHubClient();
        List<Item> items = client.search(lat, lon, null);

        for (Item item : items) {
            item.setFavorite(favoritedItemIds.contains(item.getId()));
        }

        response.setContentType("application/json");
        mapper.writeValue(response.getWriter(), items); // or: response.getWriter().print(mapper.writeValueAsString(items));

//        response.getWriter().print(itemsString);
//        response.getWriter().write("This is SearchServlet");
    }
}
