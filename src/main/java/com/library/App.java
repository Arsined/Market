package com.library;
//для создания сервера
import com.sun.net.httpserver.HttpServer;
//для логгинга
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
//для выбора порта
import java.net.InetSocketAddress;
//Для получения имени файла
import java.io.File;
//для обнаружения ошибок
import java.io.IOException;

class App{
    public File fileName;
    /**
     * получение названия json файла содержащего информацию
     * @param fileName название json файла
     */
    public App(File fileName){
        this.fileName = fileName;
    }
    //Создание логгера
    private static final Logger logger = LogManager.getLogger(App.class);
    public void start(File fileName) throws IOException{
        File file = new File("log4j2.xml");
        LoggerContext ctx = (LoggerContext) LogManager.getContext(LogManager.class.getClassLoader(), false);
        ctx.setConfigLocation(file.toURI());
        logger.info("Application start");
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