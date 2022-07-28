package com.example.electricitips.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electricitips.*
import com.example.electricitips.databinding.FragmentTipsBinding


class Tips :  Fragment(R.layout.fragment_tips){

    private lateinit var binding: FragmentTipsBinding
    private var webView: WebView? = null
    private var navController : NavController? = null
    private var arrayArticles = ArrayList<Articles>()

    override fun onCreateView(
        inflater: LayoutInflater, container:ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate layout for this fragment
        binding = FragmentTipsBinding.inflate(inflater, container, false)

        webView = binding.webView
        webView!!.webViewClient = WebViewClient()

        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true

        //onLoadWebsites()
        binding.tipsBtn.setOnClickListener() {
            //val action = TipsDirections.actionTipsToWebView2()
            try{
                //val action = TipsDirections.actionTipsToWebView2()
                //navController = parentFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
                //navController!!.navigate(action)
            }
            catch (e: Exception){
                Toast.makeText(context,"${e.toString()}",Toast.LENGTH_LONG).show()
            }
        }

        arrayArticles.add(Articles(
            R.drawable.article1_img,
            "How to save more on electricity this summer",
            "Kris Crismundo",
            "February 27, 2020",
            "Filipinos expect higher temperature in the coming months, which also means higher demand for power and rising electricity bills."
        ))

        arrayArticles.add(Articles(
            R.drawable.article2_img,
            "Save Energy",
            "ENERGY LITERACY PH ",
            "",
            "People use energy for transportation, cooking, heating and cooling rooms, manufacturing, lighting, entertainment, and many other uses. " +
                    "The choices people make about how they use energy—turning machines off when they're not using them or choosing to buy fuel-efficient vehicles " +
                    "and energy-efficient appliances—affects the environment and people's lives."
        ))

        arrayArticles.add(Articles(
            R.drawable.article3_img,
            "Energy Efficiency",
            "Meralco",
            "",
            "1. Choose the right fan depending on the specific area occupied by people who would need ventilation:\n" +
                    "\ta. For small areas, desk fans are good energy efficient spot coolers\n" +
                    "\tb. For medium-sized areas, stand fans offer great flexibility because of its adjustable height levels\n" +
                    "\tc. For large or spacious rooms, consider using ceiling fans\n" +
                    "2. Set the right fan motor speed that would provide comfortable cooling to the area occupants\n" +
                    "3. Clean the fans regularly and oil the motors per manufacturer advice. Poorly maintained fans " +
                    "(e.g. dust accumulation in fan blades and motor housing) negatively affects the fan’s energy consumption."
        ))

        arrayArticles.add(Articles(
            R.drawable.article4_img,
            "21 tips: no-cost ways to save electricity",
            "BC Hydro",
            "",
            "The best way to start saving on your electricity costs is to get smart with how you use electricity. " +
                    "Make these 21 no-cost changes in your home and you could save \$500 or more a year, depending on a number of factors " +
                    "including the size of your home."
        ))

        arrayArticles.add(Articles(
            R.drawable.article5_img,
            "Understanding the Energy Guide Label (EEF and EER Label) – Philippines",
            "Miguel Mores",
            "July 27, 2020",
            "Energy efficiency is one of the most important factors that you should take into account when buying a new refrigerator or a new air conditioner. " +
                    "The fastest and easiest way to tell if a model is energy efficient is by looking at how high its energy efficiency factor (EEF for refrigerators) " +
                    "or energy efficiency ratio (EER for aircons) is. These info is prominently displayed on its energy guide label."
        ))

        val articlesAdapter = TipsRecyclerAdapter(arrayArticles, this)
        binding.tipsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.tipsRecyclerView.adapter = articlesAdapter
        return binding.root
    }


    private fun onLoadWebsites() {

    /*
        binding.tipsBtn.setOnClickListener() {
            val action = TipsDirections.actionTipsToWebView2()
            findNavController().navigate(action)

            val tips: Array<String> = resources.getStringArray(R.array.tipsWebsites)
            AlertDialog.Builder(requireContext()) //requireContext is used when fragments are involved
                .setTitle("List of Tips")
                .setItems(tips) { _, which ->
                    if (tips[which] == "Tips by DOE") {
                        binding.webView.loadUrl("https://www.pna.gov.ph/articles/1095032")
                    }
                    if (tips[which] == "Tips by Energy Literacy PH") {
                        binding.webView.loadUrl("https://www.energyliteracyph.com/learning-materials/save-energy")
                    }
                    if (tips[which] == "Energy Efficiency Tips by MERALCO") {
                        binding.webView.loadUrl("https://company.meralco.com.ph/faq-about/energy-efficiency")
                    }
                    if (tips[which] == "No-cost Ways to Save Electricity") {
                        binding.webView.loadUrl("https://www.bchydro.com/powersmart/residential/tips-technologies/everyday-electricity-saving-tips.html")
                    }
                    if (tips[which] == "Understanding Energy Labels in Appliances") {
                        binding.webView.loadUrl("https://101appliance.com/understanding-the-energy-label/")
                    }
                }
                .show()
        }
        */
    }


}

