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

import java.util.HashSet;
import java.util.Set;


public class HomeVerticle extends AbstractVerticle {

    private WebClient homeClient;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        System.out.println(Thread.currentThread().getName() + " home verticle 部署成功!");

        vertx.eventBus().consumer(Messages.HOME_REQUEST, this::doOnePieceCnHandler);

        final WebClientOptions options = new WebClientOptions()
                .setConnectTimeout(60000)
//                .setFollowRedirects(false)
                .setIdleTimeout(60000)
                .setKeepAlive(true);

        homeClient = WebClient.create(vertx, options);
        startPromise.complete();
    }


    private void doOnePieceCnHandler(Message<JsonObject> message) {

        System.out.println("开始请求首页数据...");

        final String userAgent = config().getString("user_agent");
        final String accept = config().getString("accept");
        final String dnt = config().getString("dnt");
        final String acceptLanguage = config().getString("accept_language");
        final String host = config().getString("host");
        final String cookie = config().getString("cookie");

        homeClient.get("one-piece.cn", "/comic")
                .putHeader("User-Agent", userAgent)
                .putHeader("Accept", accept)
                .putHeader("DNT", dnt)
                .putHeader("cookie", cookie)
                .putHeader("Accept-Language", acceptLanguage)
                .putHeader("Host", host)
                .as(BodyCodec.string())
                .send(res -> {
                    if (res.failed() || res.result() == null) {
                        System.out.printf("发送首页数据失败%s.", res.cause().getMessage());
                    } else {
                        System.out.println("请求首页数据成功，开始解析...");
                        final String body = res.result().body();
                        final Document document = Jsoup.parse(body);
                        final Elements elements = document.getElementsByClass("chapter");
                        if (!elements.isEmpty()) {
                            System.out.println("首页数据解析成功，开始传输...");
                            final Set<String> set = new HashSet<>(1024);
                            for (Element divElem : elements) {
                                final Elements aElements = divElem.getElementsByTag("a");
                                for (Element aElem : aElements) {
                                    final String url = aElem.attr("href");
                                    final String name = aElem.text();
                                    if (!set.contains(name)) {
                                        final JsonObject resObject = new JsonObject()
                                                .put("url", url)
                                                .put("name", name);
                                        vertx.eventBus().send(Messages.COMIC_REQUEST, resObject);
                                        set.add(name);
                                        try {
                                            Thread.sleep(20);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } else {
                            System.out.println("解析数据失败");
                            System.out.println(body);
                        }
                    }
                });
    }
}
