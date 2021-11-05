package com.bedu.navigation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val options = navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }

        val button = view.findViewById<Button>(R.id.navigate_destination_button)
        button?.setOnClickListener {
            findNavController().navigate(R.id.flow_step_one_dest, null, options)
        }

        view.findViewById<Button>(R.id.navigate_action_button)?.setOnClickListener {
//            findNavController().navigate(R.id.next_action, null, options)
            val flowStepNumberArg = 1
            val action = HomeFragmentDirections.nextAction(flowStepNumberArg)
            findNavController().navigate(action)
        }

        val btnLink = view.findViewById<Button>(R.id.btnLink)
        btnLink?.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.google.com/")
            startActivity(openURL)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }
}
