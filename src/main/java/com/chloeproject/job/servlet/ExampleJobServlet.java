package com.chloeproject.job.servlet;

import com.chloeproject.job.entity.ExampleCoordinates;
import com.chloeproject.job.entity.ExampleJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xml.internal.utils.res.XResources_es;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Bug1: if not adding urlPatterns will have 404 error ("Not Found")
@WebServlet(name = "ExampleJobServlet", urlPatterns = "/example_job")
public class ExampleJobServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // let client knows that the content type of response is "application/json"
        response.setContentType("application/json");

        ObjectMapper mapper = new ObjectMapper();
        ExampleCoordinates coordinates = new ExampleCoordinates(37.485130, -122.148316);
        ExampleJob job = new ExampleJob("Software Engineer", 123456, "Aug 1, 2020", false, coordinates);

        // mapper will map the job entity-class object to be JSON object and write it out as string
        String mapped = mapper.writeValueAsString(job);
//        System.out.println("mapped is: " + mapped);
        response.getWriter().print(mapped);
//        response.getWriter().print("This is a example job servlet");
    }
}
