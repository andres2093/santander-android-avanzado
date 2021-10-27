package com.bedu.librerias.items

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import com.bedu.librerias.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.ChartData

abstract class BarChartItem(cd: ChartData<*>?, c: Context) : ChartItem(cd!!) {

    private val mTf: Typeface = Typeface.createFromAsset(c.assets, "OpenSans-Regular.ttf")
    private val resTextColor: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) c.resources
        .getColor(R.color.text_color, c.theme) else c.resources.getColor(R.color.text_color)

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, c: Context?): View? {
        var convertView: View? = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            convertView = LayoutInflater.from(c).inflate(R.layout.list_item_barchart, null)
            holder.chart = convertView.findViewById(R.id.chart)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.chart!!.description.isEnabled = false
        holder.chart!!.setDrawGridBackground(false)
        holder.chart!!.setDrawBarShadow(false)

        val xAxis = holder.chart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = mTf
        xAxis.textColor = resTextColor
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        val leftAxis = holder.chart!!.axisLeft
        leftAxis.typeface = mTf
        leftAxis.setLabelCount(5, false)
        leftAxis.textColor = resTextColor
        leftAxis.spaceTop = 20f
        leftAxis.axisMinimum = 0f

        val rightAxis = holder.chart!!.axisRight
        rightAxis.typeface = mTf
        rightAxis.textColor = resTextColor
        rightAxis.setLabelCount(5, false)
        rightAxis.spaceTop = 20f
        rightAxis.axisMinimum = 0f
        mChartData.setValueTypeface(mTf)

        val l = holder.chart!!.legend
        l.textColor = resTextColor
        holder.chart!!.data = mChartData as BarData
        holder.chart!!.setFitBars(true)
        holder.chart!!.animateY(700)

        return convertView
    }

    private class ViewHolder {
        var chart: BarChart? = null
    }
}
