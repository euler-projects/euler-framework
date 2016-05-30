package net.eulerform.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;

public abstract class FileReader {
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
        
        byte[] result;
        try {
            // 一次读一个字节
            inputStream = new FileInputStream(file);
            result = new byte[inputStream.available()];
            int count=0;
            byte tempByte;
            while ((tempByte = (byte) inputStream.read()) != -1) {
                result[count++]=tempByte;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if(inputStream != null ) inputStream.close();
        }
        
        return result;
    }
    
    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     * @throws IOException 
     */
    public static byte[] readFileByMultiBytes(String path, int number) throws IOException {
        File file = new File(path);
        return readFileByMultiBytes(file, number);
    }
    
    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     * @throws IOException 
     */
    public static byte[] readFileByMultiBytes(File file, int number) throws IOException {
        InputStream inputStream = null;
        
        byte[] result;
        try {
            // 一次读多个字节
            byte[] tempbytes = new byte[number];
            inputStream = new FileInputStream(file);
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
        } finally {
            if(inputStream != null ) inputStream.close();
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
    
    public static void writeFile(String filePath, String data) throws IOException{
        File file =new File(filePath);
        FileWriter fileWritter = null;
        BufferedWriter bufferWritter = null;

        try {
        //if file doesnt exists, then create it
        if(!file.exists()){
            file.createNewFile();
        }        
        //true = append file
        fileWritter = new FileWriter(filePath,true);
        bufferWritter = new BufferedWriter(fileWritter);
        bufferWritter.write(data);
        bufferWritter.close();
        } catch (IOException e) {
            if(fileWritter != null) fileWritter.close();
            if(bufferWritter != null) bufferWritter.close();
            throw e;
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
        System.out.println("DELETE " + file.getPath());
        return file.delete();
    }
}
