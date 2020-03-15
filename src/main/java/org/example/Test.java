package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

import java.util.concurrent.atomic.AtomicInteger;


public class Test {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
//        FileSystem fileSystem = vertx.fileSystem();
//        WebClient webClient = WebClient.create(vertx);
//
//        OpenOptions openOptions = new OpenOptions()
//                .setWrite(true)
//                .setCreateNew(true);
//        fileSystem.open("/Users/lwolvej/Desktop/test1.jpg", openOptions, res -> {
//            WriteStream<Buffer> writeStream = res.result();
//            webClient.getAbs("https://wx1.sinaimg.cn/large/006EvmGWgy1g50kzhp4vuj31b812wqhu.jpg")
//                    .as(BodyCodec.pipe(writeStream))
//                    .send(sendRes -> {
//                        if (sendRes.succeeded()) {
//                            System.out.println("请求成功");
//                        }
//                    });
//        });
        final AtomicInteger integer = new AtomicInteger(0);
        vertx.setPeriodic(200, periodicRes -> {
            if(integer.get() > 100) {
                vertx.close();
            }
            System.out.println(integer.getAndIncrement());
        });
    }
}
