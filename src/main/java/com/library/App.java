package com.library;

import com.sun.net.httpserver.HttpServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

import java.io.IOException;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);
    public static void main(String[] args) throws IOException{
        logger.info("Application start");
        //Получаем название файла из ввода
        String fileName = "data.json";
        try{
            fileName = args[0];
        }catch(ArrayIndexOutOfBoundsException e){
            fileName = "data.json";
        }
        int serverPort = 8080;
        //Создание сервера на порту 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0); 
        //Создание эндпоинтов
        server.createContext("/account", new Account(fileName));
        server.createContext("/market", new Market(fileName));
        server.createContext("/market/deal", new MarketDeal(fileName));
        server.setExecutor(null); // Создание стандартного исполнителя
        logger.info("Server start");
        server.start();
    }
}