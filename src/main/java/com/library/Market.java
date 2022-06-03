package com.library;
//для работы с сервером
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
//для работы с json файлами
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
//для логгинга
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
//для работы с файлами
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

public class Market implements HttpHandler{
    public File fileName;
    /**
     * получение названия json файла содержащего информацию
     * @param fileName название json файла
     */
    public Market(File fileName){
        this.fileName = fileName;
    }
    //Создание логгера
    private static final Logger logger = LogManager.getLogger(Market.class);
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Endpoint GET /market");
        //При вводе эндпоинта GET /market
        if ("GET".equals(exchange.getRequestMethod())) { 
            //Вывод равен возврату из функции
            String respText = readJson(fileName);
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            //Отправка ответа
            output.write(respText.getBytes()); 
            output.flush();
        //При вводе неверного эндпоинта
        } else {
            logger.error("Incorrect request");
            //Отправка ответа с HTTP статусом 400
            exchange.sendResponseHeaders(400, -1);
        }
        exchange.close();
    }
    /**
     * Открывает файл "data.json", считывает информацию о магазине,
     * а затем отправляет массив информации в метод записи:
     * @throws IOException
     */
    public static String readJson(File fileName) throws IOException{
        logger.info("Reading JSON file");
        //Пытаемся найти файл
        BufferedReader reader;
        if (fileName.exists()) {
            reader = new BufferedReader(new FileReader(fileName));
        }else{
            logger.error("Cannot find resource file "+ fileName);
            return "Error! Сan't find a file to read to";
        }
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject object = new JSONObject(tokener);
        //Завершаем чтение файла для освобождения ресурсов
        reader.close();
        JSONArray books = object.getJSONArray("books");
        String[][] booksArray = new String[books.length()][5];
        //Запись в массив информации о магазине
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
            return market.toString(4);
        } catch (IOException e) {
            logger.error("Cannot find resource file");
            e.printStackTrace();
            return "Error! Сan't find a file to write to";
        }
    }
}
