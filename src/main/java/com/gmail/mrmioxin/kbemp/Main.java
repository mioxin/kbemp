package com.gmail.mrmioxin.kbemp;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.dbService.DBService;
import com.gmail.mrmioxin.kbemp.servlets.IndexServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by palchuk on 10.01.2018.
 */

public class Main {
    public static Cards cards;
    private static Logger logg;

    public static void main(String... args) throws Exception {
        logg = BaseConst.logg;
        logg.addHandler(new ConsoleHandler());
        logg.setUseParentHandlers(false);
        logg.getHandlers()[0].setLevel(Level.FINE);
        logg.setLevel(Level.FINE);
        DBService dbService = new DBService();
        dbService.printConnectInfo();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new IndexServlet(dbService)), "/*");
//        context.addServlet(new ServletHolder(new SignInServlet(dbService)), "/signin");

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setResourceBase("html");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, context});

        Server server = new Server(8080);
        server.setHandler(handlers);

        try {
            server.start();
            System.out.println("Server started");

            //открываем в браузере главную страничку
            Desktop desktop;
            try {
                desktop = Desktop.getDesktop();
                desktop.browse(new URL("http://localhost:8080").toURI());
            } catch (IOException ex) {
                System.err.println("Failed to browse. " + ex.getLocalizedMessage());
            } catch (Exception ex) {
                System.err.println("Класс Desktop не поддерживается.");
            }
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        cards = new Cards(dbService);
//        cards.load(Paths.get("data.txt"));//первоначальная загрузка из файла
//        cards.load("razd");
//        System.out.println(cards.toString());
    }


}
