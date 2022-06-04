package com.library;
//для перезаписи xml 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
//для обнаружения ошибок
import java.io.IOException;

public class PreStartSettings {
    public static void main(String[] args) throws IOException{
        File fileName = null;
        String logFilePath = null;
        //проверка передачи аргумента в виде названия файлов
        try{
            switch (args.length){
                case 0:
                    fileName = new File("data.json");
                    logFilePath = "logs/log4j2.log";
                    break;
                case 1:
                    fileName = new File(args[0]);
                    logFilePath = "logs/log4j2.log";
                    break;
                case 2:
                    fileName = new File(args[0]);
                    logFilePath = args[1];
                break;
            }
        }
        catch(Exception e){
            throw e;
        }
        String xmlFilePath = "log4j2.xml";
        File xmlFile = new File(xmlFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            //Обновляем значение атрибута
            NodeList employees = doc.getElementsByTagName("RollingFile");
            Element emp = null;
            emp = (Element) employees.item(0);
            emp.setAttribute("fileName", logFilePath);
            //Перезапись обновленного файла
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(xmlFilePath));
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.transform(source, result);
            //Запуск основного класса
            App a = new App(fileName);
            a.start(fileName);
        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) {
            e1.printStackTrace();
        }
    }
}
