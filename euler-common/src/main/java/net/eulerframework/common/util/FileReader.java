/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015-2016 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://github.com/euler-form/web-form
 * http://eulerframework.net
 * http://cfrost.net
 */
package net.eulerframework.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 文件读取器，可读取2GB以下文件，大文件没有测试
 * @author cFrost
 *
 */
public abstract class FileReader {
    private static final Logger log = LogManager.getLogger();
    
    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     * @throws IOException 
     */
    public static byte[] readFileByByte(String path) throws IOException {
        File file = new File(path);
        return readFileByByte(file);
    }
    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     * @throws IOException 
     */
    public static byte[] readFileByByte(File file) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return readInputStreamByByte(inputStream);
        } catch (IOException e) {
            throw e;
        } finally {
            if(inputStream != null ) inputStream.close();
        }
    }
    
    /**
     * 以多个字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     * @throws IOException 
     */
    public static byte[] readFileByMultiBytes(String path, int number) throws IOException {
        File file = new File(path);
        return readFileByMultiBytes(file, number);
    }
    
    /**
     * 以多个字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     * @throws IOException 
     */
    public static byte[] readFileByMultiBytes(File file, int number) throws FileNotFoundException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return readInputStreamByMultiBytes(inputStream, number);
        } catch (FileNotFoundException fileNotFoundException) {
            throw fileNotFoundException;
        } catch (IOException e) {
            throw e;
        } finally {
            if(inputStream != null ) inputStream.close();
        }
    }
    
    public static byte[] readInputStreamByByte(InputStream inputStream) throws IOException {
        byte[] result;
        
        try {
            // 一次读一个字节
            result = new byte[inputStream.available()];
            int count=0;
            int tempInt;
            while ((tempInt = inputStream.read()) != -1) {
                result[count++]= (byte) (tempInt & 0xff);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if(inputStream != null ) inputStream.close();
        }
        
        return result;
    }
    
    public static byte[] readInputStreamByMultiBytes(InputStream inputStream, int number) throws IOException {
        byte[] result;
        try {
            // 一次读多个字节
            byte[] tempbytes = new byte[number];
            result = new byte[inputStream.available()];
            int readCount;
            int count=0;
            while ((readCount = inputStream.read(tempbytes)) != -1) {
                for(int i=0; i<readCount; i++){
                    result[count++]=tempbytes[i];                    
                }
            }
        } catch (IOException e) {
            throw e;
        }
        
        return result;
    }
    
    /**
     * 以字符为单位读取文件，常用于读文本，数字等类型的文件
     * @throws IOException 
     */
    public static String readFileByChar(String path) throws IOException {
        File file = new File(path);
        return readFileByChar(file);
    }

    /**
     * 以字符为单位读取文件，常用于读文本，数字等类型的文件
     * @throws IOException 
     */
    public static String readFileByChar(File file) throws IOException {
        Reader reader = null;
        StringBuffer resultBuffer= new StringBuffer();
        
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            int tempchar;
            while ((tempchar = reader.read()) != -1) {
                resultBuffer.append((char)tempchar);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if(reader != null ) reader.close();
        }

        return resultBuffer.toString();
    }
    
    /**
     * 以字符为单位读取文件，常用于读文本，数字等类型的文件
     * @throws IOException 
     */
    public static String readFileByMultiChars(String path, int number) throws IOException {
        File file = new File(path);
        return readFileByMultiChars(file, number);
    }
    
    /**
     * 以字符为单位读取文件，常用于读文本，数字等类型的文件
     * @throws IOException 
     */
    public static String readFileByMultiChars(File file, int number) throws IOException {
        Reader reader = null;
        StringBuffer resultBuffer= new StringBuffer();
        
        try {
            char[] tempchars = new char[number];
            reader = new InputStreamReader(new FileInputStream(file));
            // 读入多个字符到字符数组中，charread为一次读取字符数
            int charread;
            while ((charread=reader.read(tempchars)) != -1) {
                resultBuffer.append(tempchars, 0, charread);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if(reader != null ) reader.close();
        }

        return resultBuffer.toString();
    }
    
    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     * @throws IOException 
     */
    public static String readFileByLines(String path) throws IOException {
        File file = new File(path);
        return readFileByLines(file);
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     * @throws IOException 
     */
    public static String readFileByLines(File file) throws IOException {
        StringBuffer resultBuffer= new StringBuffer();
        
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader= null;
        BufferedReader reader = null;
        
        try {
            fileInputStream = new FileInputStream(file);
            inputStreamReader=new InputStreamReader(fileInputStream,"UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                resultBuffer.append(tempString);
                resultBuffer.append('\n');
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (fileInputStream != null) fileInputStream.close();
            if (inputStreamReader != null) inputStreamReader.close();
            if (reader != null) reader.close();
        }
        return resultBuffer.toString();
    }

    /**
     * 随机读取文件内容
     */
    public static void readFileByRandomAccess(String fileName) {
        RandomAccessFile randomFile = null;
        try {
            System.out.println("随机读取一段文件内容：");
            // 打开一个随机访问文件流，按只读方式
            randomFile = new RandomAccessFile(fileName, "r");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            // 读文件的起始位置
            int beginIndex = (fileLength > 4) ? 4 : 0;
            // 将读文件的开始位置移到beginIndex位置。
            randomFile.seek(beginIndex);
            byte[] bytes = new byte[10];
            int byteread = 0;
            // 一次读10个字节，如果文件内容不足10个字节，则读剩下的字节。
            // 将一次读取的字节数赋给byteread
            while ((byteread = randomFile.read(bytes)) != -1) {
                System.out.write(bytes, 0, byteread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e1) {
                }
            }
        }
    }
    
    /**
     * 写字符串
     * @param filePath 文件路径
     * @param data 字符串内容
     * @param append 追加模式
     * @throws IOException
     */
    public static void writeFile(String filePath, String data, boolean append) throws IOException{
        
        log.info("Write File: " + filePath);
        
        File file =new File(filePath);
        //FileWriter fileWritter = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferWritter = null;

        try {
        //if file doesnt exists, then create it
        if(!file.exists()){
            file.createNewFile();
        }        
        //true = append file
        //fileWritter = new FileWriter(filePath,true);
        outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file, append),"UTF-8");
        bufferWritter = new BufferedWriter(outputStreamWriter); ;//new BufferedWriter(fileWritter);
        bufferWritter.write(data);
        bufferWritter.close();
        } catch (IOException e) {
            throw e;
        } finally {
            if(bufferWritter != null) bufferWritter.close();  
            if(outputStreamWriter != null) outputStreamWriter.close();   
            //if(fileWritter != null) fileWritter.close();       
        }
    }
    
    /**
     * 写二进制数据
     * @param filePath 文件路径
     * @param data 数据内容
     * @param append 追加模式
     * @throws IOException
     */
    public static void writeFile(String filePath, byte[] data, boolean append) throws IOException{
        
        log.info("Write File: " + filePath);
        
        File file =new File(filePath);
        FileOutputStream fileOutputStream = null;

        try {
            //if file doesnt exists, then create it
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            if(!file.exists()){
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file, append);
            fileOutputStream.write(data);
        } catch (IOException e) {
            throw e;
        } finally {
            if(fileOutputStream != null) fileOutputStream.close();            
        }
    }
    
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);
        return deleteFile(file);
    }
    public static boolean deleteFile(File file) {
        if(!file.exists())
            return true;
        
        if (file.isDirectory()) {
            String[] children = file.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteFile(new File(file, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return delete(file);
    }

    private static boolean delete(File file) {
        log.info("DELETE " + file.getPath());
        return file.delete();
    }
}
