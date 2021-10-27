package com.bedu.librerias;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bedu.librerias.items.BarChartItem;
import com.bedu.librerias.items.ChartItem;
import com.bedu.librerias.items.LineChartItem;
import com.bedu.librerias.items.PieChartItem;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    private int resTextColor;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        resTextColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                getResources().getColor(R.color.text_color, getTheme())
                : getResources().getColor(R.color.text_color);

        ListView listView = findViewById(R.id.listView);
        ArrayList<ChartItem> list = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            if (i % 3 == 0) {
                list.add(new LineChartItem(generateDataLine(i + 1), getApplicationContext()) {
                    @Override
                    public int getItemType() {
                        return ChartItem.TYPE_LINE_CHART;
                    }
                });
            } else if (i % 3 == 1) {
                list.add(new BarChartItem(generateDataBar(i + 1), getApplicationContext()) {
                    @Override
                    public int getItemType() {
                        return ChartItem.TYPE_BARCHART;
                    }
                });
            } else {
                list.add(new PieChartItem(generateDataPie(), getApplicationContext()) {
                    @Override
                    public int getItemType() {
                        return ChartItem.TYPE_PIE_CHART;
                    }
                });
            }
        }

        ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(), list);
        listView.setAdapter(cda);
    }

    private static class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            ChartItem ci = getItem(position);
            return ci != null ? ci.getItemType() : 0;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }
    }

    private LineData generateDataLine(int cnt) {

        ArrayList<Entry> values1 = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            values1.add(new Entry(i, (int) (Math.random() * 65) + 40));
        }

        LineDataSet d1 = new LineDataSet(values1, "New DataSet " + cnt + ", (1)");
        d1.setLineWidth(2.5f);
        d1.setCircleRadius(4.5f);
        d1.setHighLightColor(resTextColor);
        d1.setValueTextColor(resTextColor);
        d1.setDrawValues(false);

        ArrayList<Entry> values2 = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            values2.add(new Entry(i, values1.get(i).getY() - 30));
        }

        LineDataSet d2 = new LineDataSet(values2, "New DataSet " + cnt + ", (2)");
        d2.setLineWidth(2.5f);
        d2.setCircleRadius(4.5f);
        d2.setHighLightColor(resTextColor);
        d2.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        d2.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);
        d1.setValueTextColor(resTextColor);
        d2.setDrawValues(false);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(d1);
        sets.add(d2);

        return new LineData(sets);
    }

    private BarData generateDataBar(int cnt) {
        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, (int) (Math.random() * 70) + 30));
        }

        BarDataSet d = new BarDataSet(entries, "New DataSet " + cnt);
        d.setColors(ColorTemplate.MATERIAL_COLORS);
        d.setHighLightAlpha(255);
        d.setValueTextColor(resTextColor);

        BarData cd = new BarData(d);
        cd.setBarWidth(0.9f);
        cd.setValueTextColor(resTextColor);
        return cd;
    }

    private PieData generateDataPie() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            entries.add(new PieEntry((float) ((Math.random() * 70) + 30), "Quarter " + (i + 1)));
        }

        PieDataSet d = new PieDataSet(entries, "");
        d.setSliceSpace(2f);
        d.setValueTextColor(resTextColor);
        d.setColors(ColorTemplate.MATERIAL_COLORS);

        return new PieData(d);
    }

}
