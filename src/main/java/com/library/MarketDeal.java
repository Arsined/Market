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
//для получения аргументов через curl - X POST
import java.io.BufferedReader;
import java.io.InputStreamReader;
//для хранения аргументов полученных через curl - X POST
import java.util.HashMap;
import java.util.Map;
//для работы с файлами
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class MarketDeal implements HttpHandler{
    public String fileName;
    /**
     * получение названия json файла содержащего информацию
     * @param fileName название json файла
     */
    public MarketDeal(String fileName){
        this.fileName = fileName;
    }
    //Создание логгера
    private static final Logger logger = LogManager.getLogger(MarketDeal.class);
    /**
     * Приводит пользовательский ввод к единому стилю:
     * id:"значение", amount:"значение"
     * @param query пользовательский ввод 
     * @param parameters словарь для хранения ввода
     */
    public static void parseQuery(String query, Map<String,
            Object> parameters){
        //Проверки наличия ввода
        if (query != null) {
            //Очищаем ввод от фигурных скобок
            query = query.substring(1,query.length()-1);
            //Разделяем ввод на id и amount
            String pairs[] = query.split(",");
            for (String pair : pairs) {
                //Разделяем ввод на ключ и значение
                String param[] = pair.split(":");
                String key = null;
                String value = null;
                //Проверки наличия значений
                if (param.length > 0) {
                    key = param[0];
                }
                if (param.length > 1) {
                    value =param[1];
                }
                parameters.put(key, value);
            }
        }
    }
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Endpoint POST /market/deal");
        //При вводе эндпоинта GET /account
        if ("POST".equals(exchange.getRequestMethod())) { 
            //Обьявление словаря для ввода пользователя
            Map<String, Object> parameters = new HashMap<String, Object>();
            //Чтение пользовательского ввода
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
            //Обработка пользовательского ввода
            parseQuery(query, parameters);
            //Вывод равен возврату из функции
            String respText = readJson(parameters, fileName);
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            //Отправка ответа
            output.write(respText.getBytes()); 
            output.flush();
            //Завершение работы приложения для обновления файла с входной информацией
            System.exit (1);
        //При вводе неверного эндпоинта
        } else {
            logger.error("Incorrect request");
            //Отправка ответа с HTTP статусом 400
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
        //Получаем пользовтелский ввод в вид переменыых
        int id = Integer.parseInt(parameters.get("id").toString());
        int amount = Integer.parseInt(parameters.get("amount").toString());
        String resourceName = fileName;
        //Пытаемся найти файл
        InputStream is = App.class.getResourceAsStream(resourceName);
        if (is == null) {
            logger.error("Cannot find resource file"+ resourceName);
            return "Error! Сan't find a file to read to";
        }
        JSONTokener tokener = new JSONTokener(is);
        JSONObject object = new JSONObject(tokener);
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
        JSONObject account = object.getJSONObject("account");
        JSONArray accountBooks = account.getJSONArray("accountBooks");
        String[][] accountArray = new String[accountBooks.length()+1][4];
        accountArray[0][0] = account.get("money").toString();
        //Переменная для хранения информации о наличии купленной книги в аккаунте
        int boughtBook = -1;
        //Запись в массив информации о аккаунте
        for (int i = 0; i < account.length(); i++) {
            JSONObject accountBook = accountBooks.getJSONObject(i);
            accountArray[i+1][1] = (accountBook.getJSONObject("book").getString("name"));
            accountArray[i+1][2] = (accountBook.getJSONObject("book").getString("author"));
            accountArray[i+1][3] = (Integer.toString(accountBook.getInt("amount")));
            //Если купленная книга уже есть в аккаунте
            if(accountArray[i+1][1].equals(booksArray[id][1]) && accountArray[i+1][2].equals(booksArray[id][0])){
                boughtBook = i;
            }
        }
        //Проверки возможности покупки
        if(id >= 0 && id < books.length()){
            if(amount <= Integer.parseInt(booksArray[id][3])){
                if(Integer.parseInt(accountArray[0][0]) >= 
                        Integer.parseInt(booksArray[id][2])*amount){
                    //Вычитание денег у аккаунта
                    accountArray[0][0] = Integer.toString(Integer.parseInt(accountArray[0][0])-
                            Integer.parseInt(booksArray[id][2])*amount);
                    //Удаление книги из магазина
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
        JSONObject changedData = new JSONObject();
        JSONObject accountBooks = new JSONObject();
        JSONArray books = new JSONArray();
        //Сортировка массива магазина
        for(int i = 0; i < accountArray.length-1; i++){
            //добавляем колличество купленных книг, если они есть у аккаунта
            if(i == boughtBook){ 
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
        //если у аккаунта нет этой книги
        if(boughtBook == -1){ 
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
        changedData.put("account", accountBooks);
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
        changedData.put("books", products);
        //Запись в Json файл
        try (FileWriter file = new FileWriter("src/main/java/com/library/"+fileName)) {
            file.write(changedData.toString(4)); 
            file.close();
            return changedData.toString(4);
        } catch (IOException e) {
            logger.error("Сan't find a file to write to");
            e.printStackTrace();
            return "Error! Сan't find a file to write to";
        }
    }
}