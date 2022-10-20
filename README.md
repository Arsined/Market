# Список команд
    curl -X GET http://localhost:8080/account -H 'Content-Type:application/json' выдает информацию об аккаунте  
    curl -X GET http://localhost:8080/market -H 'Content-Type:application/json' выдает информацию о магазине  
    curl -X POST http://localhost:8080/market/deal -H 'Content-Type:application/json' -d {"id":0,"amount":2}  
    С помощью этого эндпоинта происходит сделка между пользователем и магазином.  
    В теле этого POST HTTP запроса отправляется JSON с указанием идентификатора (id) товара,  
    который хочется купить в желаемом количестве (amount).  
# Пути важных файлов
    logs\log4j2.log файл с логами  
    data.json файл с входной информацией  
    output.json файл с выходной информацией  
    app.jar основной файл, запускается через java -jar app.jar data.json logs/log4j2.log  
# Как это рабоает
    После эндпоинтов account или market идет запись в output.json,  
    После market/deal перезаписывается информация в data.json.  
