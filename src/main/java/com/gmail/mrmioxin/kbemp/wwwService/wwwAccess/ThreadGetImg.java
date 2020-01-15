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
import java.util.regex.Pattern;

/**
 * Created by palchuk on 19.04.2018.
 */
public class ThreadGetImg extends  ThreadGet {
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
        logger.info("Создали поток "+thrName+" для скачивания imgFile: " + imgFile.toString());

    }


    @Override
    public void run() {
        if (Files.exists(imgFile)) {//если файл уже есть - trow, иначе скачиваем
            logger.info("Файл " + imgFile.toString() + " уже существует.");
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
                        logger.warning("Файл \"" + httpget.getURI().toString() + "\" не найден. Скачать невозможно.");
                        return null;
                    } else {
                        logger.info("Скачиваем файл \"" + httpget.getURI().toString() + "\".");
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
