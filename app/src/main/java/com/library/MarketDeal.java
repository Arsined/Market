package com.library;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;

class MarketDeal implements HttpHandler{
    public String fileName;
    public MarketDeal(String fileName){
        this.fileName = fileName;
    }
    private static final Logger logger = LogManager.getLogger(MarketDeal.class);
    public static void parseQuery(String query, Map<String,
            Object> parameters) throws UnsupportedEncodingException {
        if (query != null) {
            query = query.substring(1,query.length()-1);
            String pairs[] = query.split(",");
            for (String pair : pairs) {
                String param[] = pair.split(":");
                String key = null;
                String value = null;
                if (param.length > 0) {

                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Endpoint POST /market/deal");
        if ("POST".equals(exchange.getRequestMethod())) { //При вводе эндпоинта GET /account
            Map<String, Object> parameters = new HashMap<String, Object>();
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
            parseQuery(query, parameters);
            String respText = readJson(parameters, fileName);
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes()); //Отправка ответа об успехе
            output.flush();
            System.exit (1);
        } else {
            logger.error("Incorrect request");
            exchange.sendResponseHeaders(400, -1);
        }
        exchange.close();
    }
    /**
     * Открывает файл "data.json", считывает информацию, 
     * а затем отправляет массив информации в метод записи:
     * @throws IOException
     */
    public static String readJson(Map<String, Object> parameters, String fileName) throws IOException{
        logger.info("Reading JSON file");
        //Получаем введенную пользователем информацию
        int id = Integer.parseInt(parameters.get("id").toString());
        int amount = Integer.parseInt(parameters.get("amount").toString());
        String resourceName = fileName;
        InputStream is = App.class.getResourceAsStream(resourceName);
        if (is == null) {
            logger.error("Cannot find resource file"+ resourceName);
            return "Error! Сan't find a file to read to";
        }
        JSONTokener tokener = new JSONTokener(is);
        JSONObject object = new JSONObject(tokener);
        //Запись в массив информации о магазине
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
        //Запись в массив информации о аккаунте
        JSONObject account = object.getJSONObject("account");
        JSONArray accountBooks = account.getJSONArray("accountBooks");
        String[][] accountArray = new String[accountBooks.length()+1][4];
        accountArray[0][0] = account.get("money").toString();
        int boughtBook = -1;
        for (int i = 0; i < account.length(); i++) {
            JSONObject accountBook = accountBooks.getJSONObject(i);
            accountArray[i+1][1] = (accountBook.getJSONObject("book").getString("name"));
            accountArray[i+1][2] = (accountBook.getJSONObject("book").getString("author"));
            accountArray[i+1][3] = (Integer.toString(accountBook.getInt("amount")));
            if(accountArray[i+1][1].equals(booksArray[id][1]) && accountArray[i+1][2].equals(booksArray[id][0])){
                boughtBook = i;
            }
        }
        //Проверки возможности покупки
        if(id >= 0 && id < books.length()){
            if(amount <= Integer.parseInt(booksArray[id][3])){
                if(Integer.parseInt(accountArray[0][0]) >= 
                        Integer.parseInt(booksArray[id][2])*amount){
                    accountArray[0][0] = Integer.toString(Integer.parseInt(accountArray[0][0])-
                            Integer.parseInt(booksArray[id][2])*amount);
                    booksArray[id][3] = Integer.toString(Integer.parseInt(booksArray[id][3])-amount);
                    return writeJson(accountArray, booksArray, boughtBook, amount, id, fileName);
                }else{
                    logger.error("Not enough money");
                    return "Error! Not enough money";
                }
            }else{
                logger.error("Not enough books");
                return "Error! Not enough books";
            }
        }else{
            logger.error("There are no books with that id");
            return "Error! There are no books with that id";
        }
    }
    /**
     * Открытие json файла "data.json" и запись в него массива, переведенного в json формат
     * @param booksArray массив информации о книгах магазина
     * @throws IOException
     */
    public static String writeJson(String[][] accountArray, String[][] booksArray, int boughtBook, int amount, int id, String fileName) throws IOException {
        logger.info("Writing JSON file");
        //Перевод массива в json объект
        JSONObject account = new JSONObject();
        JSONObject accountBooks = new JSONObject();
        JSONArray books = new JSONArray();
        //Сортировка массива магазина
        for(int i = 0; i < accountArray.length-1; i++){
            if(i == boughtBook){ //добавляем колличество купленных книг, если они есть у аккаунта
                accountArray[i+1][3]=Integer.toString(Integer.parseInt(accountArray[i+1][3])+amount);
            }
            JSONObject object = new JSONObject();
            JSONObject book = new JSONObject();
            book.put("name", accountArray[i+1][1]);
            book.put("author",accountArray[i+1][2]);
            object.put("book", book);
            object.put("amount", Integer.parseInt(accountArray[i+1][3]));
            books.put(object);
        }
        if(boughtBook == -1){ //если у аккаунта нет этой книги
            JSONObject object = new JSONObject();
            JSONObject book = new JSONObject();
            book.put("name", booksArray[id][1]);
            book.put("author",booksArray[id][0]);
            object.put("book", book);
            object.put("amount", amount);
            books.put(object);
        }
        //Сортировка массива аккаунта
        accountBooks.put("accountBooks", books);
        accountBooks.put("money", Integer.parseInt(accountArray[0][0]));
        account.put("account", accountBooks);
        JSONArray products = new JSONArray();
        for(int i = 0; i < booksArray.length; i++){
            if(Integer.parseInt(booksArray[i][3]) > 0){
                JSONObject object = new JSONObject();
                object.put("id", Integer.parseInt(booksArray[i][4]));
                object.put("name", booksArray[i][1]);
                object.put("author",booksArray[i][0]);
                object.put("price", Integer.parseInt(booksArray[i][2]));
                object.put("amount", Integer.parseInt(booksArray[i][3]));
                products.put(object);
            }
        }
        account.put("books", products);
        //Запись в Json файл
        try (FileWriter file = new FileWriter("src/main/java/com/library/"+fileName)) {
            file.write(account.toString(4)); 
            file.close();
            return "Success!";
        } catch (IOException e) {
            logger.error("Сan't find a file to write to");
            e.printStackTrace();
            return "Error! Сan't find a file to write to";
        }
    }
}