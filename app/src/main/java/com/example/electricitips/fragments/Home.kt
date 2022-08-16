package com.example.electricitips.fragments

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.electricitips.*
import com.example.electricitips.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class Home :  Fragment(R.layout.fragment_home){
    private var binding: FragmentHomeBinding? = null
    private var arrayList = ArrayList<Appliance>()
    private var rateCost = 0.0
    private var max = 0.0
    private var total = 0.0
    lateinit var pieChart : PieChart
    // get calendar
    private lateinit var calendar : Calendar
    // get total number of days of current month
    private var daysOfThisMonth : Int = 0
    // get total number of weeks of current month
    private var weeksOfThisMonth  : Int = 0
    private val entries = ArrayList<PieEntry>()
    private val colorSet = java.util.ArrayList<Int>()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container:ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // inflate layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)

        pieChart = binding!!.monthlyUsagePieChart
        // get calendar
        calendar = Calendar.getInstance()
        // get total number of days of current month
        daysOfThisMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        // get total number of weeks of current month
        weeksOfThisMonth = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)

        // connect to firebase database
        database = Firebase.database("https://electricitips-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val rateListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    rateCost = snapshot.value.toString().toDouble()
                }
                else{
                    database.child("rate").setValue(rateCost)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "loadPost:onCancelled", error.toException())
            }

        }

        val limitListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    max = snapshot.value.toString().toDouble()
                }
                else{
                    database.child("limit").setValue(max)
                }
                binding!!.kwhMonthly.setText(max.toString())

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "loadPost:onCancelled", error.toException())
            }

        }

        val applianceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                arrayList.clear()
                readAppliances(snapshot.value as Map<String, Objects>)
                updateStats()
                pieChart.notifyDataSetChanged()
                pieChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "loadPost:onCancelled", error.toException())
            }

        }

        database.child("rate").addListenerForSingleValueEvent(rateListener)
        database.child("limit").addListenerForSingleValueEvent(limitListener)
        database.child("appliances").addListenerForSingleValueEvent(applianceListener)

        binding!!.kwhMonthly.setText(max.toString())

        binding!!.setKwhMonthly.setOnClickListener(){

            if(binding!!.kwhMonthly.text.toString().trim().isNullOrBlank()){
                binding!!.kwhMonthly.error = "Field is Empty."
            }
            else if(binding!!.kwhMonthly.text.toString() == "0.0"){
                binding!!.kwhMonthly.error = "Cannot be zero."
            }
            else{
                val mSet = MediaPlayer.create(context,R.raw.set)
                max = binding!!.kwhMonthly.text.toString().toDouble()
                database.child("limit").setValue(max)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Maximum Threshold Set! ",Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error on setting limit",Toast.LENGTH_SHORT).show()
                    }

                mSet.start()
                val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding!!.maxInputLayout.windowToken,0)

                val progress = ((total/max)*100).toInt()
                if(progress < 25){
                    binding!!.monthlyLimitText.setTextColor(Color.GREEN)
                }
                else if(progress < 75){
                    binding!!.monthlyLimitText.setTextColor(Color.parseColor("#FFA500"))
                }
                else{
                    binding!!.monthlyLimitText.setTextColor(Color.RED)
                }
                binding!!.usageLimitProgress.progress = progress
                binding!!.usageLimitText.text = "$progress %"
                val formatTotal = String.format("%.2f",total)
                val formatMax = String.format("%.2f",max)
                binding!!.monthlyLimitText.text = "$formatTotal kW / $formatMax kW"
            }

        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try{
            val dbData = database.child("appliances").get()
            readAppliances(dbData as Map<String, Objects>)
        }
        catch (e: Exception){
            Log.e("Home",e.toString())
        }
        updateStats()
    }

    private fun updateStats() {
        var entertainment = 0.0
        var lighting = 0.0
        var cooling = 0.0
        var kitchenApp = 0.0
        var householdApp = 0.0
        var other = 0.0
        var dummyPrice = 0.0


        for (item in arrayList) {

            when (item.frequency) {
                // rating must be divided by 1000 since it needs to be converted from watts to kilowatts
                "Daily" -> dummyPrice =
                    (item.rating / 1000.0) * (item.duration) * daysOfThisMonth
                "Weekly" -> dummyPrice =
                    (item.rating / 1000.0) * (item.duration) * weeksOfThisMonth
                "Monthly" -> dummyPrice =
                    (item.rating / 1000.0) * (item.duration)

            }
            when (item.type) {

                "Entertainment" -> entertainment += dummyPrice
                "Lighting" -> lighting += dummyPrice
                "Cooling" -> cooling += dummyPrice
                "Kitchen Appliance" -> kitchenApp += dummyPrice
                "Household Appliance" -> householdApp += dummyPrice
                "Others" -> other += dummyPrice

            }
        }

        total = entertainment + lighting + cooling + kitchenApp + householdApp + other

        entries.clear()
        colorSet.clear()

        if (total == 0.0) {
            entries.add(PieEntry(100.0F))
            colorSet.add(Color.parseColor("#666666"))
        }
        else {

            if (entertainment > 0) {
                entries.add(PieEntry(entertainment.toFloat()))
                colorSet.add(ContextCompat.getColor(requireContext(), R.color.theme_red))
            }
            if (lighting > 0) {
                entries.add(PieEntry(lighting.toFloat()))
                colorSet.add(ContextCompat.getColor(requireContext(), R.color.theme_green))
            }
            if (cooling > 0) {
                entries.add(PieEntry(cooling.toFloat()))
                colorSet.add(ContextCompat.getColor(requireContext(), R.color.theme_blue))
            }
            if (kitchenApp > 0) {
                entries.add(PieEntry(kitchenApp.toFloat()))
                colorSet.add(ContextCompat.getColor(requireContext(), R.color.theme_pink))
            }
            if (householdApp > 0) {
                entries.add(PieEntry(householdApp.toFloat()))
                colorSet.add(ContextCompat.getColor(requireContext(), R.color.theme_yellow))
            }
            if (other > 0) {
                entries.add(PieEntry(other.toFloat()))
                colorSet.add(ContextCompat.getColor(requireContext(), R.color.theme_orange))
            }

        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colorSet

        val data = PieData(dataSet)

        if (total == 0.0) {

            dataSet.setDrawValues(false)

        } else {

            dataSet.setDrawValues(true)
            dataSet.valueTextColor = Color.WHITE
            dataSet.valueTextSize = 12.0F
            dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        }

        pieChart.data = data
        pieChart.centerTextRadiusPercent = 0f
        pieChart.isDrawHoleEnabled = false
        pieChart.isUsePercentValuesEnabled
        pieChart.legend.isEnabled = false
        pieChart.description.isEnabled = false

        binding!!.dailyEle.text = String.format("%.2f", total / 30) + " kW"
        binding!!.monthlyEle.text = String.format("%.2f", total) + " kW"
        binding!!.dailyCost.text =
            "PHP " + String.format("%.2f", ((total / daysOfThisMonth) * rateCost))
        binding!!.monthlyCost.text = "PHP " + String.format("%.2f", (total * rateCost))

        if (max.equals(0.0)) {
            binding!!.usageLimitProgress.progress = 0
            binding!!.usageLimitText.text = "Not Set"
            binding!!.monthlyLimitText.text = "Not Set"
        } else {
            val progress = ((total / max) * 100).toInt()
            binding!!.usageLimitProgress.progress = progress
            binding!!.usageLimitText.text = "$progress%"
            binding!!.monthlyLimitText.text =
                "${String.format("%.2f", total)} kW / ${String.format("%.2f", max)} kW"
        }
        if ((total / max) * 100 < 25) {
            binding!!.monthlyLimitText.setTextColor(Color.GREEN)
        } else if ((total / max) * 100 < 75) {
            binding!!.monthlyLimitText.setTextColor(Color.parseColor("#FFA500"))
        } else {
            binding!!.monthlyLimitText.setTextColor(Color.RED)
        }
    }

    private fun readAppliances(appliances: Map<String, Objects>) {
        for((key, _) in appliances.entries){

            val singleItem = appliances[key] as Map<String, Objects>

            try{
                val item = Appliance(
                    modelCode = singleItem["code"].toString(),
                    name = singleItem["name"].toString(),
                    imgId = singleItem["imgID"].toString().toInt(),
                    type = singleItem["type"].toString(),
                    frequency = singleItem["frequency"].toString(),
                    rating = singleItem["rating"].toString().toDouble(),
                    duration = singleItem["duration"].toString().toDouble()
                )
                arrayList.add(item)
            }catch (e: Exception){
                Toast.makeText(context,"$e",Toast.LENGTH_SHORT).show()
            }
        }

    }

    // binding must be set to null on fragment destroy to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}