package com.sky.test;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

@SpringBootTest
public class POITest {
    public static void write() throws Exception {
        // 在内存中创建一个Excel文件
        XSSFWorkbook excel = new XSSFWorkbook();
        // 在Excel文件中创建一个Sheet页
        XSSFSheet sheet = excel.createSheet("info");
        // 在Sheet中创建行对象，rownum编号从0开始
        XSSFRow row = sheet.createRow(1);
        // 创建单元格并且写入文件内容
        row.createCell(1).setCellValue("姓名");
        row.createCell(2).setCellValue("城市");

        // 创建一个新行
        row = sheet.createRow(2);
        row.createCell(1).setCellValue("张三");
        row.createCell(2).setCellValue("北京");

        row = sheet.createRow(3);
        row.createCell(1).setCellValue("李四");
        row.createCell(2).setCellValue("南京");

        // 创建文件输出流
        try (FileOutputStream out = new FileOutputStream(new File("D:\\info.xlsx"))) {
            // 先将工作簿内容写入输出流
            excel.write(out);
        } finally {
            // 最后关闭工作簿
            excel.close();
        }
    }

    public static void read() throws Exception {
        InputStream in = new FileInputStream(new File("D:\\info.xlsx"));

        // 读取磁盘上已经存在的Excel文件
        XSSFWorkbook excel = new XSSFWorkbook(in);
        // 读取Excel文件中的第一个Sheet页
        XSSFSheet sheet = excel.getSheetAt(0);

        // 获取Sheet中最后一行的行号
        int lastRowNum = sheet.getLastRowNum();

        for (int i = 1; i <= lastRowNum; i++) {
            // 获得某一行
            XSSFRow row = sheet.getRow(i);
            // 获得单元格对象
            String cellValue1 = row.getCell(1).getStringCellValue();
            String cellValue2 = row.getCell(2).getStringCellValue();
            System.out.println(cellValue1 + " " + cellValue2);
        }
        // 关闭资源
        in.close();
        excel.close();
    }

    public static void main(String[] args) throws Exception{
        write();
        read();
    }
}

