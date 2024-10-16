package me.caseload.knockbacksync.stats;

import java.util.concurrent.Callable;

public class SingleLineChart extends CustomChart {

    private final Callable<Integer> callable;

    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public SingleLineChart(String chartId, Callable<Integer> callable) {
        super(chartId);
        this.callable = callable;
    }

    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
        int value = callable.call();
        if (value == 0) {
            // Null = skip the chart
            return null;
        }
        return new JsonObjectBuilder().appendField("value", value).build();
    }
}
