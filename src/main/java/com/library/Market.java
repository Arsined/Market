package com.library;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Market implements HttpHandler{
    public String fileName;
    public Market(String fileName){
        this.fileName = fileName;
    }
    private static final Logger logger = LogManager.getLogger(Market.class);
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Endpoint GET /market");
        if ("GET".equals(exchange.getRequestMethod())) { //При вводе эндпоинта GET /market
            String respText = readJson(fileName);
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes()); //Отправка ответа об успехе
            output.flush();
        } else {
            logger.error("Incorrect request");
            exchange.sendResponseHeaders(400, -1);
        }
        exchange.close();
    }
    /**
     * Открывает файл "data.json", считывает информацию о магазине,
     * а затем отправляет массив информации в метод записи:
     * @throws IOException
     */
    public static String readJson(String fileName) throws IOException{
        logger.info("Reading JSON file");
        String resourceName = fileName;
        InputStream is = App.class.getResourceAsStream(resourceName);
        if (is == null) {
            logger.error("Cannot find resource file"+ resourceName);
            return "Error! Сan't find a file to read to";
        }
        JSONTokener tokener = new JSONTokener(is);
        JSONObject object = new JSONObject(tokener);
        JSONArray books = object.getJSONArray("books");
        String[][] booksArray = new String[books.length()][5];
        for (int i = 0; i < books.length(); i++) {
            JSONObject book = books.getJSONObject(i);
            booksArray[i][0] = (book.getString("author"));
            booksArray[i][1] = (book.getString("name"));
            booksArray[i][2] = (Integer.toString(book.getInt("price")));
            booksArray[i][3] = (Integer.toString(book.getInt("amount")));
            booksArray[i][4] = (Integer.toString(book.getInt("id")));
        }
        return writeJsonMarket(booksArray);
    }
    /**
     * Открытие json файла "output.json" и запись в него массива, переведенного в json формат
     * @param booksArray массив информации о книгах магазина
     * @throws IOException
     */
    public static String writeJsonMarket(String[][] booksArray) throws IOException {
        logger.info("Writing JSON file");
        //Перевод массива в json объект
        JSONObject market = new JSONObject();
        JSONArray products = new JSONArray();
        for(int i = 0; i < booksArray.length; i++){
            if(Integer.parseInt(booksArray[i][3]) > 0){
                JSONObject object = new JSONObject();
                object.put("id", Integer.parseInt(booksArray[i][4]));
                JSONObject book = new JSONObject();
                book.put("name", booksArray[i][1]);
                book.put("author",booksArray[i][0]);
                object.put("book", book);
                object.put("price", Integer.parseInt(booksArray[i][2]));
                object.put("amount", Integer.parseInt(booksArray[i][3]));
                products.put(object);
            }
        }
        market.put("products", products);
        //Запись в Json файл
        try (FileWriter file = new FileWriter("output.json")) {
            file.write(market.toString(4)); 
            file.flush();
            return "Success!";
        } catch (IOException e) {
            logger.error("Cannot find resource file");
            e.printStackTrace();
            return "Error! Сan't find a file to write to";
        }
    }
}
