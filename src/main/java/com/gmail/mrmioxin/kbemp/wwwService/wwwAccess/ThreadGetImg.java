package com.gmail.mrmioxin.kbemp.wwwService.wwwAccess;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import org.apache.http.impl.client.CloseableHttpClient;

//import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Created by palchuk on 19.04.2018.
 */
public class ThreadGetImg extends  ThreadGet {
    private static final String DOWNLOAD_F = "Скачиваем файл \"{0}\".";
    private static final String FILE_NOT_FAUND = "Файл \"{0}\" не найден. Скачать невозможно.";
    private static final String FILE_EXIST = "Файл {0} уже существует.";
    private static final String CREATE_THR = "Создали поток {0} для скачивания imgFile: {1}";
    public static List<ThreadGetImg> threads = new ArrayList<>();
    private Path imgFile;

    public ThreadGetImg(CloseableHttpClient site, Card c, String nameThread){
        super(site,c,nameThread);
        String base="";
        String url = c.getAvatar();

        Pattern p_host = Pattern.compile("^(\\S+?\\.\\S+?\\.\\S+?\\/)");
        if (findPattern(p_host,url,1) == "") {
            base = BaseConst.WWWINT;
        }else {
            base = "https://";
        }
        URI uri = null;
        try {
            uri = new URI( base + url);
        } catch (URISyntaxException e) {
            logger.severe("Get URI fot Img error: " +e );
            e.printStackTrace();
        }
        this.httpget.setURI(uri);
        imgFile = Paths.get(uri.getHost() + uri.getPath());
        logger.log(Level.INFO,CREATE_THR, new String[] {thrName, imgFile.toString()});

    }


    @Override
    public void run() {
        if (Files.exists(imgFile)) {//если файл уже есть - trow, иначе скачиваем
            logger.log(Level.INFO, FILE_EXIST, imgFile.toString());
        } else {
            if (Files.isDirectory(imgFile.getParent())) {//папка уже есть
            } else {//если нет папки - создаем
                try {
                    Files.createDirectories(imgFile.getParent());
                } catch (IOException e) {
                    logger.severe("Create folder error: " +e );
                    e.printStackTrace();
                }
                logger.info("Create folder: " + imgFile.toString());
            }
            try {
                httpClient.execute(httpget, result -> {
                    if (result == null) {
                        logger.log(Level.WARNING, FILE_NOT_FAUND, httpget.getURI().toString());
                        return null;
                    } else {
                        logger.log(Level.INFO, DOWNLOAD_F, httpget.getURI().toString());
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imgFile.toFile()));
                        result.getEntity().writeTo(bos);
                        bos.close();
                        return 1;
                    }
                });
            } catch (IOException e) {
                logger.severe("HTTPclient ThreadGetImg ["+thrName+"] error: " +e );
                e.printStackTrace();
            }
        }
        logger.info("END [" + thrName + "] thread. Count: " + count++);
    }
}
