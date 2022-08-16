package com.example.electricitips


import android.media.MediaPlayer
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RecyclerViewAdapter (private var arrayList: ArrayList<Appliance>, val context: Fragment) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private var mDelete = MediaPlayer.create(context.requireContext(), R.raw.delete)
    private lateinit var database: DatabaseReference
    private var localArray = arrayList

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var img = itemView.findViewById<ImageView>(R.id.card_img)
        private var modelCode = itemView.findViewById<TextView>(R.id.card_code)
        private var name = itemView.findViewById<TextView>(R.id.card_name)
        private var type = itemView.findViewById<TextView>(R.id.card_type)
        private var rating = itemView.findViewById<TextView>(R.id.card_rating)
        private var duration = itemView.findViewById<TextView>(R.id.card_duration)
        private var frequency = itemView.findViewById<TextView>(R.id.card_frequency)
        private var linearlayout = itemView.findViewById<LinearLayout>(R.id.card_linearlayout)
        private var cardBackground = itemView.findViewById<CardView>(R.id.dashboard_cardview)

        fun bindItems(appliance: Appliance){
            if(appliance.imgId == R.drawable.empty){
                img.setImageResource(appliance.imgId)
                name.text = appliance.name
                modelCode.visibility = View.GONE
                type.visibility = View.GONE
                rating.text = "N/A"
                duration.text = "N/A"
                frequency.visibility = View.GONE
            }
            else{
                img.setImageResource(appliance.imgId)
                name.text = appliance.name
                modelCode.text = appliance.modelCode
                type.text = appliance.type
                rating.text = appliance.rating.toString()
                duration.text = appliance.duration.toString()
                frequency.text = appliance.frequency
                linearlayout.visibility = View.GONE
            }

            when(appliance.type){
                "Entertainment" -> cardBackground.setCardBackgroundColor(ContextCompat.getColor(cardBackground.context, R.color.theme_red))
                "Lighting" -> cardBackground.setCardBackgroundColor(ContextCompat.getColor(cardBackground.context, R.color.theme_green))
                "Cooling" -> cardBackground.setCardBackgroundColor(ContextCompat.getColor(cardBackground.context, R.color.theme_blue))
                "Kitchen Appliance" -> cardBackground.setCardBackgroundColor(ContextCompat.getColor(cardBackground.context, R.color.theme_pink))
                "Household Appliance" -> cardBackground.setCardBackgroundColor(ContextCompat.getColor(cardBackground.context, R.color.theme_yellow))
                "Others" -> cardBackground.setCardBackgroundColor(ContextCompat.getColor(cardBackground.context, R.color.theme_orange))
                else -> cardBackground.setCardBackgroundColor(ContextCompat.getColor(cardBackground.context, R.color.orignal_grey))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview_items, parent, false)
        // initialize firebase

        database = Firebase.database("https://electricitips-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return localArray.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(localArray[position])

        val collapseBtn = holder.itemView.findViewById<Button>(R.id.card_collapseBtn)
        collapseBtn.setOnClickListener {
            val holderLL = holder.itemView.findViewById<LinearLayout>(R.id.card_linearlayout)
            if(holderLL.visibility != View.VISIBLE){
                holderLL.visibility = View.VISIBLE
                collapseBtn.setBackgroundResource(R.drawable.collapse)
            }
            else{
                holderLL.visibility = View.GONE
                collapseBtn.setBackgroundResource(R.drawable.expand)
            }
        }

        holder.itemView.setOnLongClickListener {
            if(holder.itemView.findViewById<TextView>(R.id.card_rating).text != "N/A"){
                AlertDialog.Builder(context.requireContext())
                    .setMessage("Proceed to delete item?")
                    .setPositiveButton("OK") { _, _ ->
                        mDelete.start()
                        val deleteQuery = localArray[position].modelCode
                        try {
                            // delete item from database
                            val query = database.child("appliances").orderByChild("code").equalTo(deleteQuery)
                            query.addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        for (item in snapshot.children) {
                                            item.ref.removeValue()
                                        }
                                    }
                                    Toast.makeText(context.requireContext(),"Item deleted!",Toast.LENGTH_SHORT).show()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context.requireContext(),"$error",Toast.LENGTH_SHORT).show()
                                }

                            })
                            localArray.removeAt(position)

                            if (localArray.size == 0) {
                                localArray.add(
                                    Appliance(
                                        R.drawable.empty,
                                        "No items to show",
                                        "",
                                        "",
                                        0.0,
                                        0.0,
                                        ""
                                    )
                                )
                            }
                            notifyDataSetChanged()
                        }
                        catch (e: Exception){
                            Toast.makeText(context.requireContext(),"$e",Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                    }
                    .show()
            }
            true
        }




    }
}