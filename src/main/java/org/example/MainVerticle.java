package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

public class MainVerticle extends AbstractVerticle {


    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        config()
                .put("user_agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15")
                .put("accept", "text/html")
                .put("dir", "/Users/lwolvej/Desktop/one-piece-download")
                .put("host", "one-piece.cn")
                .put("dnt", "1")
                .put("connection", "keep-alive")
                .put("accept_language", "zh-CN,zh;")
                .put("base_url", "https://one-piece.cn")
                .put("cookie", "Hm_lvt_536e798a6b4ff16f87e8fbebde347f50=1584113423; Hm_lpvt_536e798a6b4ff16f87e8fbebde347f50=1584193793; sid=26724835.103760160.1584194117909.1584194161164");

        final DeploymentOptions homeDeployOptions = new DeploymentOptions()
                .setInstances(1)
                .setConfig(config())
                .setWorker(true);

        final DeploymentOptions comicDeployOptions = new DeploymentOptions()
                .setInstances(4)
                .setConfig(config())
                .setWorker(true);

        final DeploymentOptions downloadDeployOptions = new DeploymentOptions()
                .setInstances(4)
                .setConfig(config())
                .setWorker(true);

        vertx.deployVerticle(DownloadVerticle.class.getName(), downloadDeployOptions, downloadRes -> {
            if (downloadRes.succeeded()) {
                vertx.deployVerticle(ComicVerticle.class.getName(), comicDeployOptions, comicRes -> {
                    if (comicRes.succeeded()) {
//                        final AtomicInteger num = new AtomicInteger(800);
//                        vertx.setPeriodic(500, periodicRes -> {
//                            if (num.get() < 975) {
//                                final JsonObject sendObject = new JsonObject()
//                                        .put("url", "/post/" + (10000 + num.get()))
//                                        .put("name", "" + num.getAndIncrement());
//                                vertx.eventBus().send(Messages.COMIC_REQUEST, sendObject);
//                            }
//                        });
                        vertx.deployVerticle(HomeVerticle.class.getName(), homeDeployOptions, homeRes -> {
                            if (homeRes.succeeded()) {
                                vertx.eventBus().send(Messages.HOME_REQUEST, new JsonObject());
                            }
                        });
                    }
                });
            }
        });


    }
}
