package com.bedu.librerias.items

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import com.bedu.librerias.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

abstract class PieChartItem(cd: ChartData<*>?, c: Context) : ChartItem(cd!!) {

    private val mTf: Typeface = Typeface.createFromAsset(c.assets, "OpenSans-Regular.ttf")
    private val mCenterText: SpannableString
    private val resTextColor: Int

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, c: Context?): View? {
        var convertView: View? = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            convertView = LayoutInflater.from(c).inflate(
                R.layout.list_item_piechart, null
            )
            holder.chart = convertView.findViewById(R.id.chart)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.chart!!.description.isEnabled = false
        holder.chart!!.holeRadius = 52f
        holder.chart!!.transparentCircleRadius = 57f
        holder.chart!!.centerText = mCenterText
        holder.chart!!.setHoleColor(resTextColor)
        holder.chart!!.setCenterTextTypeface(mTf)
        holder.chart!!.setCenterTextSize(9f)
        holder.chart!!.setUsePercentValues(true)
        holder.chart!!.setExtraOffsets(5f, 10f, 50f, 10f)

        mChartData.setValueFormatter(PercentFormatter())
        mChartData.setValueTypeface(mTf)
        mChartData.setValueTextSize(11f)
        mChartData.setValueTextColor(resTextColor)

        holder.chart!!.data = mChartData as PieData

        val l = holder.chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.textColor = resTextColor
        l.setDrawInside(false)
        l.yEntrySpace = 0f
        l.yOffset = 0f
        holder.chart!!.animateY(900)

        return convertView
    }

    private fun generateCenterText(): SpannableString {
        val s = SpannableString("MPAndroidChart\nBedu\nAdvanced")
        s.setSpan(RelativeSizeSpan(1.6f), 0, 14, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.VORDIPLOM_COLORS[0]), 0, 14, 0)
        s.setSpan(RelativeSizeSpan(1.0f), 14, 20, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 14, 20, 0)
        s.setSpan(RelativeSizeSpan(1.4f), 20, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), 20, s.length, 0)
        return s
    }

    private class ViewHolder {
        var chart: PieChart? = null
    }

    init {
        mCenterText = generateCenterText()
        resTextColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) c.resources
            .getColor(R.color.text_color, c.theme) else c.resources.getColor(R.color.text_color)
    }
}
