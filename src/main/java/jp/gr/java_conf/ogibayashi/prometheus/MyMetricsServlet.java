package jp.gr.java_conf.ogibayashi.prometheus;

import io.prometheus.client.CollectorRegistry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class MyMetricsServlet extends HttpServlet {
    private MyCollectorRegistry registry;

    public MyMetricsServlet() {
        this(MyCollectorRegistry.defaultRegistry);
    }

    public MyMetricsServlet(MyCollectorRegistry registry) {
        this.registry = registry;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(200);
        resp.setContentType("text/plain; version=0.0.4; charset=utf-8");
        Writer writer = resp.getWriter();
        MyTextFormat.write004(writer, this.registry.metricFamilySamples());
        writer.flush();
        writer.close();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }
}
