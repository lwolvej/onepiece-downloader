package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ComicVerticle extends AbstractVerticle {

    private WebClient comicClient;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        System.out.println(Thread.currentThread().getName() + " comic verticle 部署成功!");

        vertx.eventBus().consumer(Messages.COMIC_REQUEST, this::doComicHandler);

        final WebClientOptions options = new WebClientOptions()
                .setConnectTimeout(60000)
                .setIdleTimeout(60000);


        comicClient = WebClient.create(vertx, options);
        startPromise.complete();
    }

    private void doComicHandler(Message<JsonObject> message) {

        final JsonObject jsonMessage = message.body();
        final String url = jsonMessage.getString("url");
        final String name = jsonMessage.getString("name");

        final String userAgent = config().getString("user_agent");
        final String accept = config().getString("accept");
        final String dnt = config().getString("dnt");
        final String acceptLanguage = config().getString("accept_language");
        final String host = config().getString("host");
        final String cookie = config().getString("cookie");

        System.out.println("开始请求comic数据...");

        comicClient.get("one-piece.cn", url)
                .putHeader("User-Agent", userAgent)
                .putHeader("Accept", accept)
                .putHeader("DNT", dnt)
                .putHeader("cookie", cookie)
                .putHeader("Accept-Language", acceptLanguage)
                .putHeader("Host", host)
                .as(BodyCodec.string())
                .send(res -> {
                    if (res.failed() || res.result() == null) {
                        System.out.printf("url:%s发送失败!%s.\n", url, res.cause().getMessage());
                    } else {
                        final Document document = Jsoup.parse(res.result().body());
                        final Elements elements = document.getElementsByTag("img");
                        if (!elements.isEmpty()) {
                            final List<String> urls = new ArrayList<>(16);
                            for (Element element : elements) {
                                urls.add(element.attr("src"));
                            }
                            final JsonObject jsonRes = new JsonObject()
                                    .put("name", name)
                                    .put("urls", urls);
                            vertx.eventBus().send(Messages.COMIC_DOWNLOAD, jsonRes);
                        } else {
                            if (name.equals("第848话 再见")) {
                                System.out.println(document);
                            }
                            System.out.printf("%s[%s]为其他网站链接，不可用\n", Thread.currentThread().getName(), name);
                        }
                    }
                });
    }
}
