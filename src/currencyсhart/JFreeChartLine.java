
package currencyсhart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Stroke;
import java.text.NumberFormat;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Day;

public class JFreeChartLine extends /*JFrame*/ JPanel {
        
    public JFreeChartLine(String [][] mArray, int is_visible_points, int is_sred_value_valut) {
        
        TimeSeriesCollection dataset = new TimeSeriesCollection();        
        // Определяем годы
        int m_year_end = Integer.parseInt(mArray[0][mArray[0].length - 1].substring(0, 4));        
        int m_year_temp = Integer.parseInt(mArray[0][0].substring(0, 4));        
        int m_year_etalon = m_year_temp;
        TimeSeries series1 = new TimeSeries(Integer.toString(m_year_temp));
        
        float [][] mArraySred = new float [1][1];                        
        float m_sred_value = 0;
        if (is_sred_value_valut == 1) {
            // определяем сколько годов            
            int m_sred_num = 0;    
            int m_kol_year = Integer.parseInt(mArray[0][mArray[0].length - 1].substring(0, 4)) - Integer.parseInt(mArray[0][0].substring(0, 4)) + 1;
            mArraySred = new float [2][m_kol_year];
            int m_base_year = Integer.parseInt(mArray[0][0].substring(0, 4));
            // идем по годам
            for (int ii = 0; ii < m_kol_year; ii++) {
                // идем по всему массиву
                m_sred_value = 0;
                m_sred_num = 0;
                for (int iii = 0; iii < mArray[0].length; iii++) {
                    if (mArray[1][iii] != null) {
                        if (Integer.toString(m_base_year).equals(mArray[0][iii].substring(0, 4))) {                            
                            m_sred_value += Float.parseFloat(mArray[1][iii].replace(",", "."));
                            m_sred_num++;
                        }
                    }
                }
                if (m_sred_num == 0) { m_sred_num = 1; }
                m_sred_value = m_sred_value / m_sred_num;
                if (m_sred_value == 0) { m_sred_value = 1; }               
                mArraySred[0][ii] = m_base_year;
                mArraySred[1][ii] = m_sred_value;
                // следующий год
                m_base_year++;
            }                            
        }
        
        for (int ii = 0; ii < mArray[0].length; ii++)
        {              
            m_year_temp = Integer.parseInt(mArray[0][ii].substring(0, 4));            
            // если год меняется, генерируем новый график
            if (m_year_etalon != m_year_temp) {
                // добавляем при каждом изменении, кроме первого
                if (ii > 0) {
                   dataset.addSeries(series1);
                }   
                // переинициализация
                series1 = new TimeSeries(Integer.toString(m_year_temp));
                m_year_etalon = m_year_temp;
            }                       
            // добавление значений
            if (mArray[1][ii] != null) {
                int m_day = Integer.parseInt(mArray[0][ii].substring(6, 8));
                int m_months = Integer.parseInt(mArray[0][ii].substring(4, 6));
                int m_year = Integer.parseInt(mArray[0][ii].substring(0, 4));
                
                // убираем высокосный, чтобы не было ошибок
                if (m_months == 2 && m_day == 29) {
                   m_day = 28; 
                }
                
                if (is_sred_value_valut == 1) {
                    for (int iii = 0; iii < mArraySred[0].length; iii++) {
                        if (mArraySred[0][iii] == m_year) {
                            m_sred_value = mArraySred[1][iii];
                        }
                    }
                    series1.add(new Day(m_day, m_months, m_year_end), Float.parseFloat(mArray[1][ii].replace(",", ".")) / m_sred_value );                                    
                }
                else {
                    series1.add(new Day(m_day, m_months, m_year_end), Float.parseFloat(mArray[1][ii].replace(",", ".")));                                    
                }
            }
        } 
        // последний год
        dataset.addSeries(series1);
    
        JFreeChart chart = createChart(dataset, is_visible_points);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(1200, 700));
        this.add(chartPanel, BorderLayout.CENTER);         
    }
        
    //Create a chart.     
    private JFreeChart createChart(XYDataset dataset, int is_visible_points) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
        null, //"Изменение курса валют", // Chart
        "Дата", // X-Axis Label
        "Курс", // Y-Axis Label
        dataset        
        );
        
        // set chart background
        chart.setBackgroundPaint(Color.white);
                      
        // set a few custom plot features
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);               
        
        // set the plot's axes to display integers
        //TickUnitSource ticks = NumberAxis.createIntegerTickUnits();
        //NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        //domain.setStandardTickUnits(ticks);        
        //NumberAxis range = (NumberAxis) plot.getRangeAxis();
        //range.setStandardTickUnits(ticks);
        
        if (is_visible_points == 1 ) {            
            // render shapes and lines
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
            plot.setRenderer(renderer);
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);

            // set the renderer's stroke
            Stroke stroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
            renderer.setBaseOutlineStroke(stroke);

            // label the points
            NumberFormat format = NumberFormat.getNumberInstance();
            format.setMaximumFractionDigits(2);
            XYItemLabelGenerator generator =
                new StandardXYItemLabelGenerator(
                    StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT,
                    format, format);
            renderer.setBaseItemLabelGenerator(generator);
            renderer.setBaseItemLabelsVisible(true);                            
        }
        
        return chart;
    }

    // Main method
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
            }
         });
    }
}
