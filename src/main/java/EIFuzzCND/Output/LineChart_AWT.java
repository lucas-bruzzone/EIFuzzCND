package EIFuzzCND.Output;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;


import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class LineChart_AWT extends ApplicationFrame {

    public LineChart_AWT(String applicationTitle , String chartTitle, List<List<Double>> acuracias, List<String> rotuloClassificadores, List<Double> novidades, ArrayList<Integer> novasClasses) throws ParseException {
        super(applicationTitle);
        XYDataset dataset;
        dataset = createDatasetAcuracia(acuracias, rotuloClassificadores);
        String label = "Accuracy";
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                chartTitle,
                "Evaluation moments",label,
                dataset,
                PlotOrientation.VERTICAL,
                true,true,false);


        Font font3 = new Font("SansSerif", Font.PLAIN, 12);
        Font font4 = new Font("SansSerif", Font.PLAIN, 14);
        XYPlot xyplot = (XYPlot) lineChart.getPlot();


        ValueAxis rangeAxis = xyplot.getRangeAxis();
        rangeAxis.setRange(0.0, 50);
        rangeAxis.setLabelPaint(Color.black);
        rangeAxis.setLabelFont(font4);
        rangeAxis.setTickLabelPaint(Color.black);




        final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(1.4F, 0, 2, 0.0F, new float[]{4.0F, 4.0F}, 0.0F);


        final XYPlot plot2 = lineChart.getXYPlot();
        final XYPlot plotAux3 = lineChart.getXYPlot();

        ValueMarker markerSmall;

        for (Integer classe : novasClasses) {
            ValueMarker marker = new ValueMarker(classe);
            marker.setPaint(Color.black);
            marker.setStroke(DEFAULT_GRIDLINE_STROKE);
            plot2.addDomainMarker(marker);

            ValueMarker marker2 = new ValueMarker(classe);
            marker2.setPaint(Color.black);
            marker2.setStroke(DEFAULT_GRIDLINE_STROKE);
            plotAux3.addDomainMarker(marker2);
        }

       for(int i=0; i<novidades.size(); i++) {
            if(novidades.get(i) != 0.0) {
                final XYPlot plotAux = lineChart.getXYPlot();
                ValueMarker marker5;
                marker5 = new ValueMarker(i+1); //pq não temos momento de avaliação 0, começamos do 1
                marker5.setPaint(Color.gray);
                marker5.setStroke(new BasicStroke(1.5f));
                plotAux.addDomainMarker(marker5);
            }
        }


        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);



        XYItemRenderer r = xyplot.getRenderer();
        r.setSeriesStroke(0, new BasicStroke(3.0f));
        r.setSeriesStroke(1, new BasicStroke(3.0f ));
        r.setSeriesStroke(2, new BasicStroke(3.0f ));
        r.setSeriesStroke(3, new BasicStroke(3.0f ));
        r.setSeriesStroke(4, new BasicStroke(3.0f ));

        r.setSeriesPaint(0, new Color(95, 173, 86));
        r.setSeriesPaint(1, new Color(242, 193, 78));
        r.setSeriesPaint(2, new Color(247, 129, 84));
        r.setSeriesPaint(3, new Color(49, 116, 161));
        r.setSeriesPaint(4, new Color(180, 67, 108));



        final CombinedDomainXYPlot plotGraph = new CombinedDomainXYPlot(new NumberAxis("Evaluation moments"));
        ValueAxis plotAxis = xyplot.getRangeAxis();
        plotAxis.setRange(0.0, 105);
        plotGraph.setGap(10.0);

        // add the subplots...
        plotGraph.add(xyplot, 9);
        plotGraph.setOrientation(PlotOrientation.VERTICAL);
        plotGraph.setBackgroundPaint(Color.white);
        plotGraph.getDomainAxis().setLabelFont(font4);
        plotGraph.getDomainAxis().setTickLabelFont(font3);

        final ChartPanel panel = new ChartPanel(new JFreeChart(plotGraph), true, true, true, false, true);
        panel.setPreferredSize(new Dimension(680, 450));
        panel.setBackground(Color.white);
        setContentPane(panel);

    }


    private XYDataset createDatasetAcuracia(List<List<Double>> metricas, List<String> rotulos) {
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < metricas.size(); i++) {
            final XYSeries serie = new XYSeries(rotulos.get(i));
            final List<Double> valoresMetrica = metricas.get(i);
            for (int j = 0; j < valoresMetrica.size(); j++) {
                serie.add(j+1, valoresMetrica.get(j));
            }
            dataset.addSeries(serie);
        }
        return dataset;
    }




}