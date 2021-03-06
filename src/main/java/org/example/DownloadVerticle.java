package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;


public class DownloadVerticle extends AbstractVerticle {


    private WebClient downloadClient;

    private FileSystem fs;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        System.out.println(Thread.currentThread().getName() + " download verticle 部署成功!");

        vertx.eventBus().consumer(Messages.COMIC_DOWNLOAD, this::comicDownloadHandler);

        final WebClientOptions options = new WebClientOptions()
                .setConnectTimeout(60000)
                .setFollowRedirects(false)
                .setIdleTimeout(60000)
                .setKeepAlive(true);

        downloadClient = WebClient.create(vertx, options);
        fs = vertx.fileSystem();

        startPromise.complete();
    }

    private void comicDownloadHandler(Message<JsonObject> message) {
        final JsonObject jsonMessage = message.body();
        final String name = jsonMessage.getString("name");
        final JsonArray urls = jsonMessage.getJsonArray("urls");
        final String dir = config().getString("dir");
        final String targetDir = dir + File.separator + name;

        fs.mkdirs(targetDir, fileRes -> {
            if (fileRes.succeeded()) {
                System.out.printf("创建文件夹:%s成功，开始下载图片...\n", targetDir);
                List<String> urlList = urls.stream()
                        .filter(elem -> !elem.equals("https://one-piece.cn/templets/default/images/logo.png"))
                        .map(elem -> (String) elem)
                        .collect(Collectors.toList());
                int id = 0;
                for (String url : urlList) {
                    final String comicTargetFileName = targetDir + File.separator + (id++) + ".jpg";
                    final OpenOptions openOptions = new OpenOptions()
                            .setCreateNew(true)
                            .setWrite(true);
                    fs.open(comicTargetFileName,
                            openOptions,
                            comicRes -> {
                                final WriteStream<Buffer> asyncFile = comicRes.result();
                                downloadClient.getAbs(url)
                                        .putHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                                        .putHeader("Host", "wx1.sinaimg.cn")
                                        .putHeader("TE", "Trailers")
                                        .putHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:74.0) Gecko/20100101 Firefox/74.0")
                                        .as(BodyCodec.pipe(asyncFile))
                                        .send(res -> {
                                            if (res.failed()) {
                                                System.out.printf("下载图片:%s失败\n", url);
                                            }
                                        });

//                                asyncFile.flush(flush -> {
//                                    if (flush.failed()) {
//                                        System.out.printf("图片保存失败%s...", url);
//                                    }
//                                });
                            });
                }
                System.out.printf("文件夹:%s下所有图片读取成功!\n", targetDir);
            } else {
                System.out.printf("创建文件夹:%s失败.\n", targetDir);
            }
        });
    }

    private void downloadFile(WriteStream<Buffer> writeStream, String url) {
        final String targetUrl = url.substring(22);
        downloadClient.get("wx1.sinaimg.cn", targetUrl)
                .putHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .putHeader("Host", "wx1.sinaimg.cn")
                .putHeader("TE", "Trailers")
                .putHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:74.0) Gecko/20100101 Firefox/74.0")
                .as(BodyCodec.pipe(writeStream))
                .send(res -> {
                    if (res.failed()) {
                        System.out.printf("下载图片:%s失败\n", url);
                    }
                });
    }
}
