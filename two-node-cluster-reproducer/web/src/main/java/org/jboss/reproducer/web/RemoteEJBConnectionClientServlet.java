/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.reproducer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.reproducer.ejb.api.EJBRemote;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.Results;
import org.jboss.reproducer.ejb.api.ServletInfo;
import org.jboss.reproducer.ejb.api.TestConfig.SERVLET;

/**
 * @author bmaxwell
 */
@WebServlet(name = "RemoteEJBConnectionClientServlet", urlPatterns = { "/remoteEJBConnection" }, loadOnStartup = 1)
public class RemoteEJBConnectionClientServlet extends HttpServlet {

    private Logger log = Logger.getLogger(this.getClass().getName());
    private String nodeName = System.getProperty("jboss.node.name");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Brad: " +  " Context Path: " + getServletContext().getContextPath());
        System.out.flush();

        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Brad: " +  " Context Path: " + getServletContext().getContextPath());
        System.out.flush();

        doGet(request, response);
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

//        getServletConfig().getServletContext().getContextPath()
        System.out.println("Brad: " +  " Context Path: " + getServletContext().getContextPath());
        System.out.flush();
        log.info("Brad: processRequest invoked , remoteUser: " + request.getRemoteUser() + " Context Path: " + getServletContext().getContextPath());
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            log.info("ejbRequest: " + request.getParameter("ejbRequest"));
//            EJBRequest ejbRequest = EJBRequest.unmarshall(request.getParameter("ejbRequest"));
            EJBRequest ejbRequest = new EJBRequest();
            Results results = invokeRemoteEJB(request, ejbRequest);
            System.out.println("Results: " + request);
//            out.write(results.marshall());
//            log.debug(results.marshall());
            out.flush();
        } catch (Throwable t) {
//            try {
////                out.write(new Results(t).marshall());
//                t.printStackTrace(System.err);
//            } catch (JAXBException je) {
//                throw new ServletException(je);
//            }
            t.printStackTrace();
        } finally {
            out.close();
        }
    }

    private static ServletInfo getServletInfo(String contextPath) {
        if(contextPath.startsWith("/"))
            contextPath = contextPath.substring(1);
        return SERVLET.fromContextPath(contextPath).info;
    }

    private Results invokeRemoteEJB(HttpServletRequest request, EJBRequest ejbRequest) throws ServletException {
        EJBRequest response = ejbRequest;
        try {
            String lookup = request.getParameter("lookup");
            System.out.println("Lookup: " + lookup);
            Properties env = new Properties();
            env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            Context ctx = new InitialContext(env);
            EJBRemote remote = (EJBRemote) ctx.lookup(lookup);
            response = remote.invoke(response);
            System.out.println(response.getResponseInvocationPath());
        } catch (Exception e) {
            throw new ServletException(e);
        }
        return new Results(request.getRemoteUser(), response);
    }
}