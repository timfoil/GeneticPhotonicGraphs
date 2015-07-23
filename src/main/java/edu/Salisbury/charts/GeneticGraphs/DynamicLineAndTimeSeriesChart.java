package edu.Salisbury.charts.GeneticGraphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import edu.salisbury.photonic.core_simulator.Coordinate;
import edu.salisbury.photonic.core_simulator.CoreLog;
import edu.salisbury.photonic.core_simulator.LogReader;
import edu.salisbury.photonic.genetic_algorithm.NodeConfiguration;
import edu.salisbury.photonic.genetic_algorithm.NodeConfigurationPopulation;

/**
 * An example to show how we can create a dynamic chart.
*/
public class DynamicLineAndTimeSeriesChart extends ApplicationFrame
{
	
	/** A Generated serial id. */
	private static final long serialVersionUID = -3537347306850828488L;

	/** The time series data. */
	private XYSeriesCollection dataset;
	private NodeConfigurationPopulation primordialSoup;

    private int genNumber;
    
    /**
     * Starting point for the dynamic graph application.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) 
    {
    	
    	final NodeConfigurationPopulation initialPop = new NodeConfigurationPopulation(
			createLog(), generateBasicSwitchingMap(), 16, 1, 15, 1);
    	initialPop.setNumberOfParents(3);
    	initialPop.setNumberOfAllTimeFittestKept(1);

    	
        final DynamicLineAndTimeSeriesChart geneticAlgorithmChart = 
        	new DynamicLineAndTimeSeriesChart("Generations of Ring Configurations", initialPop);
        
        geneticAlgorithmChart.pack();
        
        RefineryUtilities.centerFrameOnScreen(geneticAlgorithmChart);
        geneticAlgorithmChart.setVisible(true);
        for(int i = 0; i < 100 ;i++)
        {
        	System.out.println("running gen: "+ geneticAlgorithmChart.genNumber);
        	geneticAlgorithmChart.run();
        	//geneticAlgorithmChart.runControlGroup();
        }
    }
    


	/**
     * Constructs a new dynamic chart application.
     *
     * @param title  the frame title.
     */
    public DynamicLineAndTimeSeriesChart(final String title, 
    		final NodeConfigurationPopulation primordialSoup) 
    {
    	super(title);
    	
    	this.primordialSoup = primordialSoup;
    	
    	
       
        dataset = new XYSeriesCollection(new XYSeries("Parent " + 0));
        for(int i = 1 ; i < primordialSoup.getNumberOfParents() ; i++)
        {
	        dataset.addSeries(new XYSeries("Parent " + i));
	    }
        
        final JFreeChart chart = createChart(dataset);

        
        chart.setBackgroundPaint(Color.LIGHT_GRAY); //Sets background color of chart

        //Created JPanel to show graph on screen
        final JPanel content = new JPanel(new BorderLayout());

        //Created Chartpanel for chart area
        final ChartPanel chartPanel = new ChartPanel(chart);

        //Added chartpanel to main panel
        content.add(chartPanel);

        //Sets the size of whole window (JPanel)
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));

        //Puts the whole content on a Frame
        setContentPane(content);

    }

    /**
     * Creates a sample chart.
     *
     * @param dataset  the dataset.
     *
     * @return A sample chart.
     */
    private JFreeChart createChart(final XYDataset dataset) {
    	
        final JFreeChart result = ChartFactory.createXYLineChart("Genetic Generations", 
        		"Generation", "Fitness (number of Cyclies)", dataset, PlotOrientation.VERTICAL, 
        		true, true, false);

        final XYPlot plot = result.getXYPlot();
        
        plot.setBackgroundPaint(new Color(0xffffff));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.lightGray);

        ValueAxis xaxis = plot.getDomainAxis();
        xaxis.setAutoRange(true); //TODO
        
        xaxis.setVerticalTickLabels(true);

        ValueAxis yaxis = plot.getRangeAxis();
        yaxis.setRangeWithMargins(75000, 90000);

        return result;
    }

    public static HashMap<Coordinate, Integer> generateBasicSwitchingMap()
	{
		HashMap<Coordinate, Integer> switchingMap =  new HashMap<Coordinate, Integer>();
		for(int i = 0; i < 8; i++)
		{
		    switchingMap.put(new Coordinate(1,i), i);
		}
		for(int i = 7; i >= 0; i--)
		{
		    switchingMap.put(new Coordinate(2,7-i), 8+i);
		}
		
		return switchingMap;
	}

    public void run()
    {
    	System.out.println("Evaluating configurations");
    	primordialSoup.evaluation();
		List<NodeConfiguration> selectionList = primordialSoup.selection();
		addToDataSets(selectionList);
		System.out.println("Generating next generation of configurations via crossover...");
		primordialSoup.crossover(selectionList);
		primordialSoup.mutation();
		genNumber++;
    }
    
    public void runControlGroup()
    {
    	System.out.println("Evaluating configurations");
    	primordialSoup.generateNewPopulation();
    	primordialSoup.evaluation();
		List<NodeConfiguration> selectionList = primordialSoup.selection();
		addToDataSets(selectionList);
		System.out.println("Generating next generation of configurations via crossover...");
		genNumber++;
    }
    
    public void addToDataSets(List<NodeConfiguration> toAdd)
    {
    	for(int i = 0; i < toAdd.size(); i++)
    	{
    		dataset.getSeries("Parent "+ i).addOrUpdate(new Integer(genNumber), toAdd.get(i).getFitness());
    	}
    }
    
    public static CoreLog createLog()
	{
		return LogReader.readLogIgnoreRepeaters("flow_barnes.log");
	}
    
    public static CoreLog createSmallLog()
	{
		final CoreLog basicLog = LogReader.readLogIgnoreRepeaters("flow_barnes.log");
		return basicLog.subLog(0, 1000);
	}

	/**
	 * @return the genNumber
	 */
	public int getGenNumber()
	{
		return genNumber;
	}
}  