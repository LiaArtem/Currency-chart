/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package currencyсhart;

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.time.format.DateTimeFormatter;
import java.time.*;
import java.util.*;
import java.net.URL;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.swing.JOptionPane;
import java.text.DecimalFormat;
import java.math.RoundingMode;

/**
 *
 * @author lad
 */
public class CurrencyСhart {

    public static String tec_kat = new File("").getAbsolutePath();
    public static String tec_kat_kurs = tec_kat + /*File.separator + "dist" +*/ File.separator + "kurs";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {              
        // Чтение курсов валют в массив                
        Curr_chart frame = new Curr_chart();
        frame.setLocationRelativeTo(null);        
        frame.setResizable(false);               
        frame.setVisible(true);                                         
    }
    
    // Получить курс НБУ
    public static String [][] getKursNbu(String mCurrCode, Date [] mDate1, Date [] mDate2)
    {         
     String mPath = tec_kat_kurs;
     String mPathXml;
     LocalDate localDate;
     String tDate;
     String tDate_xml;
     DateTimeFormatter f;
     LocalDateTime localDateTime;
     File file;     
     int days = 0;
     int daysp = 0;
     Date mDate;
     String [][] mArray;
     
     // идем по периодам определяем размер массива
     for (int iii = 0; iii < mDate1.length; iii++)
     {
         days = days + (int)( (mDate2[iii].getTime() - mDate1[iii].getTime()) / (1000 * 60 * 60 * 24)) + 1;            
     }
     mArray = new String [2][days];
     
     // идем по периодам
     for (int iii = 0; iii < mDate1.length; iii++)
     {
        mDate = mDate1[iii];
        days = (int)( (mDate2[iii].getTime() - mDate1[iii].getTime()) / (1000 * 60 * 60 * 24)) + 1;            
        ////////////////////////////////////////////////////////////////////////////////////
        // сначала ищем в файле search_results.xml
        // https://bank.gov.ua/control/uk/curmetal/currency/search/form/period           

        mPathXml = mPath + File.separator + mCurrCode + File.separator + "search_results.xml";  
        file = new File(mPathXml);
        if (file.exists() == false) {
           MessageBoxError("Файл не найден " + mPathXml, ""); 
        }

        // идем по дням
        for (int ii = 0; ii < days; ii++)
        {  
        // пишем дату с любом случае 
         localDate = mDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();        
         tDate = localDate.format(DateTimeFormatter.ofPattern ( "yyyyMMdd" ));      
         tDate_xml = localDate.format(DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));               
         mArray [0][daysp] = tDate;                              
         // Если нет файла взять его с сайта
         if (file.exists() == true) {
           if (file.isFile()) {
            try {                        
              // чтение XML файла                
              DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
              DocumentBuilder db = dbf.newDocumentBuilder();
              Document doc = db.parse(file);
              doc.getDocumentElement().normalize();        
              NodeList nodeLst = doc.getElementsByTagName("currency");        

              for (int s = 0; s < nodeLst.getLength(); s++) {
                Node fstNode = nodeLst.item(s); 
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) { 
                  Element fstElmnt = (Element) fstNode;
                  Element message_date = (Element) fstElmnt.getElementsByTagName("date").item(0);
                  Element message_curr = (Element) fstElmnt.getElementsByTagName("digital_code").item(0);

                  if (message_date.getTextContent().equals(tDate_xml) && message_curr.getTextContent().equals(mCurrCode))  
                  {
                     Element message_rate = (Element) fstElmnt.getElementsByTagName("exchange_rate").item(0);
                     String text_rate = (message_rate.getTextContent().replace(",", "."));
                     Element message_num = (Element) fstElmnt.getElementsByTagName("number_of_units").item(0);
                     String text_num = (message_num.getTextContent().replace(",", "."));

                     if (checkString_Float(text_rate) == true && checkString_Float(text_num) == true) {
                        //return Float.parseFloat(text);                     
                        DecimalFormat df = new DecimalFormat("#.##");
                        df.setRoundingMode(RoundingMode.CEILING);
                        mArray [1][daysp] = df.format((Float.parseFloat(text_rate) / Float.parseFloat(text_num)));                      
                     } else {
                     }                                                                           
                  }
                }
              }
              } catch (Exception e) {
                   e.printStackTrace();
                   MessageBoxError(e.toString(), "");
              }
            }          
         }      
           // плюс 1 день
           localDateTime = mDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
           localDateTime = localDateTime.plusDays(1);
           mDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
           
           daysp++;
        }

        // по дням с сайта НБУ
        if (1 == 0) {

        // идем по дням
        for (int ii = 0; ii < days; ii++)
        {      
         // проверяем есть ли значение в массиве
         if (mArray [1][ii] == null)   
         {                  
           localDate = mDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();        
           f = DateTimeFormatter.ofPattern ( "yyyyMMdd" );
           tDate = localDate.format(f);      
           mPathXml = mPath + File.separator + tDate + ".xml";  

           // действия, если папка не существует, создаем                        
           if (new File(mPath).exists() == false) {      
              new File(mPath).mkdirs();
           }

           file = new File(mPathXml);      

           // Если нет файла взять его с сайта
           if (file.exists() == false) {
               // чтение файла с НБУ       
               try {
                   URL xmlURL = new URL("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?&date=" + tDate);
                   InputStream xml = xmlURL.openStream();
                   DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                   DocumentBuilder db = dbf.newDocumentBuilder();
                   Document doc = db.parse(xml);
                   // сохранение на локальном диске
                   Transformer transformer = TransformerFactory.newInstance().newTransformer();
                   Source source = new DOMSource(doc);
                   Result result = new StreamResult(new FileOutputStream(mPathXml));
                   transformer.transform(source, result);                          
                   xml.close(); 

                   // перечитать созданный файл
                   file = new File(mPathXml);             
               }
               catch (Exception e) {
                   e.printStackTrace();
                   MessageBoxError(e.toString(), "");
               }         
           }

           if (file.isFile()) {
            try {
              // чтение XML файла                
              DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
              DocumentBuilder db = dbf.newDocumentBuilder();
              Document doc = db.parse(file);
              doc.getDocumentElement().normalize();        
              NodeList nodeLst = doc.getElementsByTagName("currency");        

              for (int s = 0; s < nodeLst.getLength(); s++) {
                Node fstNode = nodeLst.item(s); 
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) { 
                  Element fstElmnt = (Element) fstNode;
                  Element message = (Element) fstElmnt.getElementsByTagName("cc").item(0);
                  if (message.getTextContent().equals(mCurrCode)) {
                    //
                     message = (Element) fstElmnt.getElementsByTagName("rate").item(0);
                     String text = (message.getTextContent().replace(",", "."));
                     if (checkString_Float(text) == true) {
                        //return Float.parseFloat(text);
                        mArray [0][ii] = tDate;
                        mArray [1][ii] = text;
                     } else {
                        //return 1;                  
                     }                                                                           
                  }
                }
              }
              } catch (Exception e) {
                   e.printStackTrace();
                   MessageBoxError(e.toString(), "");
              }
            }
         }     
           // плюс 1 день
           localDateTime = mDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
           localDateTime = localDateTime.plusDays(1);
           mDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());                                 
        }
       }
     }
     
     return mArray;
    }
     
    // вывод диалогового окна
    public static void MessageBoxError(String infoMessage, String titleBar)
    {
       JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.ERROR_MESSAGE);
    } 
    
    // проверка значения - float
    public static boolean checkString_Float(String string) 
    {
       String string_new  = string.replace(",", ".");
       if (string_new.isEmpty() == true) { return false; }
       try {               
           Float.parseFloat(string_new);
       } catch (Exception e) {
       return false;
       }
       return true;
    }    
    
}
