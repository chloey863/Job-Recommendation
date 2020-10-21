package com.chloeproject.job.servlet;

import org.json.JSONObject;
//import sun.nio.ch.IOUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

@WebServlet(name = "ExampleBookServlet", urlPatterns = {"/example_book"})
public class ExampleBookServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonRequest  = new JSONObject(IOUtils.toString(request.getReader()));

        // the following is manually parsing JSON, which is not efficient, long code and error-prone.
        // therefore, it's better to use Jackson library that perform the parsing (parse them to be my entity class)
        String title = jsonRequest.getString("title");
        String author = jsonRequest.getString("author");
        String date = jsonRequest.getString("date");
        float price = jsonRequest.getFloat("price");
        String currency = jsonRequest.getString("currency");
        int pages = jsonRequest.getInt("pages");
        String series = jsonRequest.getString("series");
        String language = jsonRequest.getString("language");
        String isbn = jsonRequest.getString("isbn");

        System.out.println("Title is: " + title);
        System.out.println("Author is: " + author);
        System.out.println("Date is: " + date);
        System.out.println("Price is: " + price);
        System.out.println("Currency is: " + currency);
        System.out.println("Pages is: " + pages);
        System.out.println("Series is: " + series);
        System.out.println("Language is: " + language);
        System.out.println("ISBN is: " + isbn);

        // This is the process of content negotiation
        response.setContentType("application/json");

        // create a response object and to tell client we've successfully POST
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "ok");

        // write the response body to the Writer, and
        // print the jsonResponse JSONObject as Body (shown in postman)
        response.getWriter().print(jsonResponse);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.getWriter().print("This is the example book servlet");
        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");
        String year = request.getParameter("year");

        System.out.println("Keyword is: " + keyword);
        System.out.println("Category is: " + category);
        System.out.println("Year is: " + year);

        // try an example:
        response.setContentType("application/json");
        JSONObject json = new JSONObject();
        json.put("title", "Harry Potter and the Sorcerer's Stone");
        json.put("author", "JK Rowling");
        json.put("date", "October 1, 1998");
        json.put("price", 11.99);
        json.put("currency", "USD");
        json.put("pages", 309);
        json.put("series", "Harry Potter");
        json.put("language", "en_US");
        json.put("isbn", "0590353403");

        // write the response body to the Writer, and
        // print the JSON object as Body (shown in postman)
        response.getWriter().print(json);
    }
}
