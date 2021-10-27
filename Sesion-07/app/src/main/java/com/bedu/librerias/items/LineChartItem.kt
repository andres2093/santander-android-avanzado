package com.bedu.librerias.items

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import com.bedu.librerias.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.LineData

abstract class LineChartItem(cd: ChartData<*>?, c: Context) : ChartItem(cd!!) {

    private val mTf: Typeface = Typeface.createFromAsset(c.assets, "OpenSans-Regular.ttf")
    private val resTextColor: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) c.resources
        .getColor(R.color.text_color, c.theme) else c.resources.getColor(R.color.text_color)

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, c: Context?): View? {
        var convertView: View? = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            convertView = LayoutInflater.from(c).inflate(R.layout.list_item_linechart, null)
            holder.chart = convertView.findViewById(R.id.chart)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.chart!!.description.isEnabled = false
        holder.chart!!.setDrawGridBackground(false)

        val xAxis = holder.chart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = mTf
        xAxis.textColor = resTextColor
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        val leftAxis = holder.chart!!.axisLeft
        leftAxis.typeface = mTf
        leftAxis.textColor = resTextColor
        leftAxis.setLabelCount(5, false)
        leftAxis.axisMinimum = 0f

        val rightAxis = holder.chart!!.axisRight
        rightAxis.typeface = mTf
        rightAxis.textColor = resTextColor
        rightAxis.setLabelCount(5, false)
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f

        val l = holder.chart!!.legend
        l.textColor = resTextColor
        holder.chart!!.data = mChartData as LineData
        holder.chart!!.animateX(750)

        return convertView
    }

    private class ViewHolder {
        var chart: LineChart? = null
    }
}
