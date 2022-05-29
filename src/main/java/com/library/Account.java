package com.library;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Account implements HttpHandler{
    public String fileName;
    public Account(String fileName){
        this.fileName = fileName;
    }
    private static final Logger logger = LogManager.getLogger(Account.class);
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Endpoint GET /account");
        if ("GET".equals(exchange.getRequestMethod())) { //При вводе эндпоинта GET /account
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
     * Открывает файл "data.json", считывает информацию об аккаунте, 
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
        JSONObject account = object.getJSONObject("account");
        JSONArray accountBooks = account.getJSONArray("accountBooks");
        String[][] accountArray = new String[accountBooks.length()+1][4];
        accountArray[0][0] = account.get("money").toString();
        for (int i = 0; i < account.length(); i++) {
            JSONObject accountBook = accountBooks.getJSONObject(i);
            accountArray[i+1][1] = (accountBook.getJSONObject("book").getString("name"));
            accountArray[i+1][2] = (accountBook.getJSONObject("book").getString("author"));
            accountArray[i+1][3] = (Integer.toString(accountBook.getInt("amount")));
        }
        return writeJsonAccount(accountArray);
    }
    /**
     * Открытие json файла "output.json" и запись в него массива, переведенного в json формат
     * @param booksArray массив информации о книгах магазина
     * @throws IOException
     */
    public static String writeJsonAccount(String[][] accountArray) throws IOException {
        logger.info("Writing JSON file");
        //Перевод массива в json объект
        JSONObject account = new JSONObject();
        JSONArray books = new JSONArray();
        for(int i = 0; i < accountArray.length-1; i++){
            JSONObject object = new JSONObject();
            JSONObject book = new JSONObject();
            book.put("name", accountArray[i+1][1]);
            book.put("author",accountArray[i+1][2]);
            object.put("book", book);
            object.put("amount", Integer.parseInt(accountArray[i+1][3]));
            books.put(object);
        }
        account.put("accountBooks", books);
        account.put("money", Integer.parseInt(accountArray[0][0]));
        //Запись в Json файл
        try (FileWriter file = new FileWriter("output.json")) {
            file.write(account.toString(4)); 
            file.flush();
            return "Success!";
        } catch (IOException e) {
            logger.error("Cannot find resource file");
            e.printStackTrace();
            return "Error! Сan't find a file to write to";
        }
    }
}
