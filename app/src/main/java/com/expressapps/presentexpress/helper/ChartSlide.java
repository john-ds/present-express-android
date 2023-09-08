package com.expressapps.presentexpress.helper;

import androidx.annotation.NonNull;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;

public class ChartSlide extends Slide implements Cloneable {
    @Element(name = "item", required = false)
    public ChartItem chartData = new ChartItem();

    @ElementList(entry = "data", required = false, inline = true)
    public ArrayList<LabelValueItem> legacyData = new ArrayList<>();

    @Element(name = "charttype", required = false)
    public String legacyChartType = "";

    @Element(name = "values", required = false)
    public boolean legacyShowValues = false;

    @Element(name = "theme", required = false)
    public String legacyTheme = "";

    @Element(name = "xlabel", required = false)
    public String legacyXLabel = "";

    @Element(name = "ylabel", required = false)
    public String legacyYLabel = "";

    @Element(name = "title", required = false)
    public String legacyTitle = "";

    @Override
    public SlideType getSlideType() {
        return SlideType.CHART;
    }

    @Override
    public String getFileName() {
        return name + "-prender.png";
    }

    @NonNull
    @Override
    public ChartSlide clone() {
        ChartSlide clone = (ChartSlide) super.clone();
        clone.chartData = clone.chartData.clone();

        clone.legacyData = new ArrayList<>();
        for(LabelValueItem p : legacyData)
            clone.legacyData.add(p.clone());

        return clone;
    }
}

class ChartItem implements Cloneable {
    @Element(name = "type", required = false)
    public int type = 0;

    @ElementList(name = "labels", entry = "data", required = false)
    public ArrayList<String> labels = new ArrayList<>();

    @ElementList(name = "series", entry = "item", required = false)
    public ArrayList<SeriesItem> series = new ArrayList<>();

    @Element(name = "title", required = false)
    public String title = "";

    @Element(name = "xtitle", required = false)
    public String axisXTitle = "";

    @Element(name = "ytitle", required = false)
    public String axisYTitle = "";

    @Element(name = "format", required = false)
    public int axisFormatType = 0;

    @Element(name = "vgridlines", required = false)
    public boolean verticalGridLines = true;

    @Element(name = "hgridlines", required = false)
    public boolean horizontalGridLines = true;

    @Element(name = "legendpos", required = false)
    public int legendPlacement = 0;

    @Element(name = "fontname", required = false)
    public String fontName = "Calibri";

    @Element(name = "fontsize", required = false)
    public int fontSize = 14;

    @Element(name = "fontcolor", required = false)
    public String fontColour = "0,0,0";

    @Element(name = "theme", required = false)
    public int colourTheme = 0;

    @Element(name = "width", required = false)
    public int width = 400;

    @Element(name = "height", required = false)
    public int height = 400;

    @NonNull
    @Override
    public ChartItem clone() {
        try {
            ChartItem clone = (ChartItem) super.clone();
            clone.labels = new ArrayList<>(clone.labels);

            clone.series = new ArrayList<>();
            for(SeriesItem p : series)
                clone.series.add(p.clone());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

class LabelValueItem implements Cloneable {
    @Element(name = "label", required = false)
    public String label = "";

    @Element(name = "value", required = false)
    public double value = 0.0;

    @NonNull
    @Override
    public LabelValueItem clone() {
        try {
            return (LabelValueItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

class SeriesItem implements Cloneable {
    @Element(name = "type", required = false)
    public int type = 0;

    @Element(name = "name", required = false)
    public String name = "";

    @ElementList(name = "values", entry = "data", required = false)
    public ArrayList<Double> values = new ArrayList<>();

    @Element(name = "showvlabels", required = false)
    public boolean showValueLabels = false;

    @Element(name = "vlabelspos", required = false)
    public int dataLabelsPlacement = 3;

    @Element(name = "thickness", required = false)
    public int strokeThickness = 4;

    @Element(name = "smooth", required = false)
    public boolean smoothLines = true;

    @Element(name = "scatterfilled", required = false)
    public boolean scatterFilled = true;

    @Element(name = "polarfilled", required = false)
    public boolean polarFilled = true;

    @Element(name = "doughnut", required = false)
    public boolean doughnutChart = false;

    @NonNull
    @Override
    public SeriesItem clone() {
        try {
            SeriesItem clone = (SeriesItem) super.clone();
            clone.values = new ArrayList<>(clone.values);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}