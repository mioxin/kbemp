package com.gmail.mrmioxin.kbemp.wwwService.wwwAccess;

import com.gmail.mrmioxin.kbemp.Card;
import org.apache.http.impl.client.CloseableHttpClient;

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
        Pattern p_host = Pattern.compile("^(http://\\S+?\\.\\S+?\\.\\S+?)");
        if (findPattern(p_host,url,1) == "") {
            base = "http://";
        }
        URI uri = null;
        try {
            uri = new URI( base + url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.httpget.setURI(uri);
        imgFile = Paths.get(uri.getHost() + uri.getPath());

    }


    @Override
    public void run() {
        if (Files.exists(imgFile)) {//если файл уже есть - trow, иначе скачиваем
            logger.fine("Файл " + imgFile.toString() + " уже существует.");
        } else {
            if (Files.isDirectory(imgFile.getParent())) {
            } else {//если нет папки - создаем
                try {
                    Files.createDirectories(imgFile.getParent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                httpClient.execute(httpget, result -> {
                    if (result == null) {
                        logger.warning("Файл \"" + httpget.getURI().toString() + "\" не найден. Скачать невозможно.");
                        return null;
                    } else {
                        logger.fine("Скачиваем файл \"" + httpget.getURI().toString() + "\".");
                        /* todo Download avatar image*/

                        return 1;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print("\rEND [" + thrName + "] thread. Count: " + count++ + ". ");
        }
    }
}
