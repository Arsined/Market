package com.library;
//для создания сервера
import com.sun.net.httpserver.HttpServer;
//для логгинга
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//для выбора порта
import java.net.InetSocketAddress;
//Для получения имени файла
import java.io.File;
//для обнаружения ошибок
import java.io.IOException;

public class App {
    //Создание логгера
    private static final Logger logger = LogManager.getLogger(App.class);
    public static void main(String[] args) throws IOException{
        logger.info("Application start");
        //Получаем название файла из ввода
        File fileName;
        //проверка передачи аргумента в виде названия файла
        try{
            fileName = new File(args[0]);
        }catch(ArrayIndexOutOfBoundsException e){
            fileName = new File("data.json");
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