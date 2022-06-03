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

public class Account implements HttpHandler{
    public File fileName;
    /**
     * получение названия json файла содержащего информацию
     * @param fileName название json файла
     */
    public Account(File fileName){
        this.fileName = fileName;
    }
    //Создание логгера
    private static final Logger logger = LogManager.getLogger(Account.class);
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Endpoint GET /account");
        //При вводе эндпоинта GET /account
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
     * Открывает файл "data.json", считывает информацию об аккаунте, 
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
        JSONObject account = object.getJSONObject("account");
        JSONArray accountBooks = account.getJSONArray("accountBooks");
        String[][] accountArray = new String[accountBooks.length()+1][4];
        accountArray[0][0] = account.get("money").toString();
        //Запись в массив информации о аккаунте
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
            return account.toString(4);
        } catch (IOException e) {
            logger.error("Cannot find resource file");
            e.printStackTrace();
            return "Error! Сan't find a file to write to";
        }
    }
}
