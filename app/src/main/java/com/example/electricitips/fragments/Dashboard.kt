package com.example.electricitips.fragments


import android.content.ContentValues.TAG
import android.content.Context
import android.media.MediaPlayer
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electricitips.*
import com.example.electricitips.R
import com.example.electricitips.databinding.FragmentDashboardBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class Dashboard: Fragment(R.layout.fragment_dashboard) {

    private var binding: FragmentDashboardBinding? = null
    private var arrayList = ArrayList<Appliance>()
    private var filteredArrayList = ArrayList<Appliance>()
    private lateinit var applianceDBHelper: ApplianceDBHelper
    private lateinit var rateDBHelper: RateDBHelper
    private lateinit var database: DatabaseReference

     override fun onCreateView(
         inflater: LayoutInflater,
         container: ViewGroup?,
         savedInstanceState: Bundle?
     ): View? {
         binding = FragmentDashboardBinding.inflate(inflater, container, false)

         // initialize db helpers
         applianceDBHelper = ApplianceDBHelper(requireActivity().applicationContext)
         rateDBHelper = RateDBHelper(requireActivity().applicationContext)

         // connect to firebase database
         database = Firebase.database("https://electricitips-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

         val rateListener = object : ValueEventListener{
             override fun onDataChange(snapshot: DataSnapshot) {
                 val rate = snapshot.value
                 binding!!.inputCostRate.setText(rate.toString())
             }

             override fun onCancelled(error: DatabaseError) {
                 Log.w(TAG, "loadPost:onCancelled", error.toException())
             }

         }

         val applianceListener = object : ValueEventListener{
             override fun onDataChange(snapshot: DataSnapshot) {
                 if(snapshot.exists()){
                     arrayList.clear()
                     readAppliances(snapshot.value as Map<String, Objects>)
                     val cardAdapter = RecyclerViewAdapter(arrayList, requireParentFragment())
                     binding?.dashboardRecyclerview?.adapter = cardAdapter
                 }

             }

             override fun onCancelled(error: DatabaseError) {
                 Log.w(TAG, "loadPost:onCancelled", error.toException())
             }

         }

         database.child("rate").addListenerForSingleValueEvent(rateListener)
         database.child("appliances").addListenerForSingleValueEvent(applianceListener)

         binding!!.setRateBtn.setOnClickListener {
             val mSet = MediaPlayer.create(context,R.raw.set)
             rateDBHelper.deleteCost()

             if(TextUtils.isEmpty(binding!!.inputCostRate.text.toString())){
                 Toast.makeText(context, "Enter Electricity Rate", Toast.LENGTH_LONG).show()
             }
             else{
                 val cost = binding!!.inputCostRate.text.toString().toDouble()
                 mSet.start()
                 database.child("rate").setValue(cost)
                     .addOnSuccessListener {
                     Toast.makeText(context, "Rate successfully set!",Toast.LENGTH_SHORT).show()
                    }
                     .addOnFailureListener {
                         Toast.makeText(context, "Error on setting rate",Toast.LENGTH_SHORT).show()
                     }
                 // hide keyboard layout after set button is pressed
                 val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                 imm.hideSoftInputFromWindow(binding!!.costInputLayout.windowToken,0)
             }

         }
         return binding!!.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arrayList.size == 0){
            arrayList.add(Appliance(R.drawable.empty,"No items to show","","",0.0,0.0,""))
        }
        // setup adapter for card view inside the recycler view
        val cardAdapter = RecyclerViewAdapter(arrayList, this)
        binding?.dashboardRecyclerview?.layoutManager = LinearLayoutManager(context)
        binding?.dashboardRecyclerview?.adapter = cardAdapter

        binding!!.filterEntertainment.setOnClickListener {
            filterByEntertainment()
        }

        binding!!.filterLighting.setOnClickListener {
            filterByLighting()
        }

        binding!!.filterCooling.setOnClickListener {
            filterByCooling()
        }

        binding!!.filterKitchen.setOnClickListener {
            filterByKitchen()
        }

        binding!!.filterHousehold.setOnClickListener {
            filterByHousehold()
        }

        binding!!.filterOthers.setOnClickListener {
            filterByOthers()
        }

    }


    private fun filterByOthers() {
        var cardAdapter: RecyclerViewAdapter? = null
        val checkedIDs = binding!!.chipGroup.checkedChipIds
        //if no chips are selected
        if (!binding!!.filterOthers.isChecked && checkedIDs.size == 0) {
            cardAdapter = RecyclerViewAdapter(arrayList, this)
            filteredArrayList.clear()
        }
        // if this chip is not selected but other chips are
        else if (!binding!!.filterOthers.isChecked && checkedIDs.size >= 1) {
            filteredArrayList.removeIf {
                it.type == "Others"
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        // if this chip is selected
        else if (binding!!.filterOthers.isChecked) {
            for (appliance in arrayList) {
                if (appliance.type == "Others") {
                    filteredArrayList.add(appliance)
                }
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        binding?.dashboardRecyclerview?.adapter = cardAdapter
    }

    private fun filterByHousehold() {
        var cardAdapter: RecyclerViewAdapter? = null
        val checkedIDs = binding!!.chipGroup.checkedChipIds
        //if no chips are selected
        if (!binding!!.filterHousehold.isChecked && checkedIDs.size == 0) {
            cardAdapter = RecyclerViewAdapter(arrayList, this)
            filteredArrayList.clear()
        }
        // if this chip is not selected but other chips are
        else if (!binding!!.filterHousehold.isChecked && checkedIDs.size >= 1) {
            filteredArrayList.removeIf {
                it.type == "Household Appliance"
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        // if this chip is selected
        else if (binding!!.filterHousehold.isChecked) {
            for (appliance in arrayList) {
                if (appliance.type == "Household Appliance") {
                    filteredArrayList.add(appliance)
                }
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        binding?.dashboardRecyclerview?.adapter = cardAdapter
    }

    private fun filterByKitchen() {
        var cardAdapter: RecyclerViewAdapter? = null
        val checkedIDs = binding!!.chipGroup.checkedChipIds
        //if no chips are selected
        if (!binding!!.filterCooling.isChecked && checkedIDs.size == 0) {
            cardAdapter = RecyclerViewAdapter(arrayList, this)
            filteredArrayList.clear()
        }
        // if this chip is not selected but other chips are
        else if (!binding!!.filterKitchen.isChecked && checkedIDs.size >= 1) {
            filteredArrayList.removeIf {
                it.type == "Kitchen Appliance"
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        // if this chip is selected
        else if (binding!!.filterKitchen.isChecked) {
            for (appliance in arrayList) {
                if (appliance.type == "Kitchen Appliance") {
                    filteredArrayList.add(appliance)
                }
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        binding?.dashboardRecyclerview?.adapter = cardAdapter
    }

    private fun filterByCooling() {
        var cardAdapter: RecyclerViewAdapter? = null
        val checkedIDs = binding!!.chipGroup.checkedChipIds
        //if no chips are selected
        if (!binding!!.filterCooling.isChecked && checkedIDs.size == 0) {
            cardAdapter = RecyclerViewAdapter(arrayList, this)
            filteredArrayList.clear()
        }
        // if this chip is not selected but other chips are
        else if (!binding!!.filterCooling.isChecked && checkedIDs.size >= 1) {
            filteredArrayList.removeIf {
                it.type == "Cooling"
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        // if this chip is selected
        else if (binding!!.filterCooling.isChecked) {
            for (appliance in arrayList) {
                if (appliance.type == "Cooling") {
                    filteredArrayList.add(appliance)
                }
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        binding?.dashboardRecyclerview?.adapter = cardAdapter
    }

    private fun filterByLighting() {
        var cardAdapter: RecyclerViewAdapter? = null
        val checkedIDs = binding!!.chipGroup.checkedChipIds
        //if no chips are selected
        if (!binding!!.filterLighting.isChecked && checkedIDs.size == 0) {
            cardAdapter = RecyclerViewAdapter(arrayList, this)
            filteredArrayList.clear()
        }
        // if this chip is not selected but other chips are
        else if (!binding!!.filterLighting.isChecked && checkedIDs.size >= 1) {
            filteredArrayList.removeIf {
                it.type == "Lighting"
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        // if this chip is selected
        else if (binding!!.filterLighting.isChecked) {
            for (appliance in arrayList) {
                if (appliance.type == "Lighting") {
                    filteredArrayList.add(appliance)
                }
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        binding?.dashboardRecyclerview?.adapter = cardAdapter
    }

    private fun filterByEntertainment() {
        var cardAdapter: RecyclerViewAdapter? = null
        val checkedIDs = binding!!.chipGroup.checkedChipIds
        //if no chips are selected
        if (!binding!!.filterEntertainment.isChecked && checkedIDs.size == 0) {
            cardAdapter = RecyclerViewAdapter(arrayList, this)
            filteredArrayList.clear()
        }
        // if this chip is not selected but other chips are
        else if (!binding!!.filterEntertainment.isChecked && checkedIDs.size >= 1) {
            filteredArrayList.removeIf {
                it.type == "Entertainment"
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        // if this chip is selected
        else if (binding!!.filterEntertainment.isChecked) {
            for (appliance in arrayList) {
                if (appliance.type == "Entertainment") {
                    filteredArrayList.add(appliance)
                }
            }
            cardAdapter = RecyclerViewAdapter(filteredArrayList, this)
        }
        binding?.dashboardRecyclerview?.adapter = cardAdapter
    }

    // binding must be set to null on fragment destroy to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}

